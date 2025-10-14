# main.py - VERSÃO FINAL E CORRIGIDA

from fastapi import FastAPI, HTTPException, status, Depends
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel, ConfigDict
import mysql.connector
from typing import Optional, List
from datetime import datetime, timedelta
from passlib.context import CryptContext
from jose import JWTError, jwt

# --- 1. CONFIGURAÇÕES DE SEGURANÇA ---
SECRET_KEY = "sua-chave-secreta-muito-forte-e-dificil-de-adivinhar"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 60 * 24  # Token válido por 1 dia

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/usuarios/login")

app = FastAPI()

# --- 2. MODELOS (Schemas Pydantic) ---

# --- MODELOS PARA AUTENTICAÇÃO E USUÁRIOS ---
class LoginResponse(BaseModel):
    access_token: str
    token_type: str
    user_id: int
    user_name: str
    card_count: int
    active_session_info: Optional[dict] = None

class TokenData(BaseModel):
    user_id: Optional[int] = None

class UsuarioCreate(BaseModel):
    nome: str
    email: str
    senha: str

class UsuarioInDB(BaseModel):
    id: int
    nome: str
    email: str
    senha_hashed: str
    # Config para Pydantic v2: substitui orm_mode
    model_config = ConfigDict(from_attributes=True)

class UsuarioPublic(BaseModel):
    id: int
    nome: str
    email: str
    model_config = ConfigDict(from_attributes=True)

# --- MODELOS PARA CARTÕES ---
class CartaoBase(BaseModel):
    id: int
    numero: str
    nome: str
    validade: str

class CartaoCreate(BaseModel):
    numero: str
    nome: str
    validade: str
    cvv: str

# --- MODELO PARA SESSÃO DE ESTACIONAMENTO ---
class StatusEstacionamentoResponse(BaseModel):
    sessaoAtiva: bool
    id_sessao: Optional[int] = None
    horario_entrada: Optional[str] = None


# --- 3. GERENCIAMENTO DE CONEXÃO COM O BANCO ---
def get_db():
    db = None
    try:
        db = mysql.connector.connect(
            host="127.0.0.1",
            user="root",
            password="unip123", # Sua senha
            database="TccBd"
        )
        yield db
    except mysql.connector.Error as err:
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail=f"Erro de conexão com o banco: {err}")
    finally:
        if db:
            db.close()

# --- 4. FUNÇÕES AUXILIARES DE AUTENTICAÇÃO E SEGURANÇA ---
def verificar_senha(senha_plana: str, senha_hashed: str) -> bool:
    return pwd_context.verify(senha_plana, senha_hashed)

def get_senha_hash(senha: str) -> str:
    return pwd_context.hash(senha)

def criar_token_acesso(data: dict) -> str:
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)

def get_user_from_db(db: mysql.connector.MySQLConnection, email: str) -> Optional[UsuarioInDB]:
    """Busca um usuário no banco pelo email."""
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT id, nome, email, senha FROM usuarios WHERE email = %s", (email,))
    user_data = cursor.fetchone()
    cursor.close()
    if user_data:
        user_data['senha_hashed'] = user_data.pop('senha')
        return UsuarioInDB(**user_data)
    return None

async def get_current_user_id(token: str = Depends(oauth2_scheme)) -> int:
    """Decodifica o token e retorna apenas o ID do usuário."""
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Credenciais inválidas ou token expirado",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        # Altere para buscar 'user_id' que você definiu na criação do token
        user_id: Optional[int] = payload.get("user_id")
        if user_id is None:
            raise credentials_exception
        return user_id
    except (JWTError, ValueError):
        raise credentials_exception


# --- 5. ROTAS DA API ---

@app.post("/usuarios/cadastrar", status_code=status.HTTP_201_CREATED, summary="Registra um novo usuário")
def cadastrar_usuario(usuario: UsuarioCreate, db: mysql.connector.MySQLConnection = Depends(get_db)):
    if len(usuario.senha) < 6:
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="A senha deve ter pelo menos 6 caracteres.")
    if len(usuario.senha.encode('utf-8')) > 72:
        raise HTTPException(status_code=status.HTTP_422_UNPROCESSABLE_ENTITY, detail="A senha excede o limite de tamanho para criptografia.")
    
    senha_hashed = get_senha_hash(usuario.senha)
    cursor = db.cursor()
    try:
        cursor.execute(
            "INSERT INTO usuarios (nome, email, senha) VALUES (%s, %s, %s)",
            (usuario.nome, usuario.email, senha_hashed)
        )
        db.commit()
    except mysql.connector.Error as err:
        if err.errno == 1062: # Erro de entrada duplicada
            raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Este email já está cadastrado.")
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=f"Erro no banco de dados: {err}")
    finally:
        cursor.close()
    
    return {"status": "sucesso", "mensagem": "Usuário criado com sucesso!"}

# ========================================================
# ROTA DE LOGIN ÚNICA E CORRIGIDA
# ========================================================
@app.post("/usuarios/login", response_model=LoginResponse, summary="Autentica um usuário e retorna um token com status de sessão")
def login_usuario(form_data: OAuth2PasswordRequestForm = Depends(), db: mysql.connector.MySQLConnection = Depends(get_db)):
    user_in_db = get_user_from_db(db, form_data.username)

    if not user_in_db or not verificar_senha(form_data.password, user_in_db.senha_hashed):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED, 
            detail="Email ou senha incorretos",
            headers={"WWW-Authenticate": "Bearer"},
        )

    cursor = db.cursor(dictionary=True)

    # Contar os cartões do usuário
    cursor.execute("SELECT COUNT(*) as count FROM cartoes WHERE usuario_id = %s", (user_in_db.id,))
    card_count = cursor.fetchone()['count']

    # Verificar se há sessão de estacionamento ativa
    cursor.execute("SELECT id, horario_entrada FROM sessoes WHERE usuario_id = %s AND status = 'ATIVA'", (user_in_db.id,))
    sessao_ativa = cursor.fetchone()
    
    active_session_info = None
    if sessao_ativa:
        active_session_info = {
            "sessao_id": sessao_ativa['id'],
            "horario_entrada": sessao_ativa['horario_entrada'].isoformat()
        }

    cursor.close()

    # Criar o token de acesso
    access_token = criar_token_acesso(data={"user_id": user_in_db.id})
    
    return LoginResponse(
        access_token=access_token,
        token_type="bearer",
        user_id=user_in_db.id,
        user_name=user_in_db.nome,
        card_count=card_count,
        active_session_info=active_session_info
    )

@app.get("/cartoes", response_model=List[CartaoBase], summary="Lista os cartões do usuário logado")
def get_cartoes_do_usuario(current_user_id: int = Depends(get_current_user_id), db: mysql.connector.MySQLConnection = Depends(get_db)):
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT id, numero, nome, validade FROM cartoes WHERE usuario_id = %s", (current_user_id,))
    cartoes = cursor.fetchall()
    cursor.close()
    
    for cartao in cartoes:
        if cartao.get('numero') and len(cartao['numero']) > 4:
            cartao['numero'] = f"**** **** **** {cartao['numero'][-4:]}"
            
    return cartoes

@app.post("/cartoes/cadastrar", status_code=status.HTTP_201_CREATED, summary="Cadastra um novo cartão para o usuário logado")
def cadastrar_cartao(cartao: CartaoCreate, current_user_id: int = Depends(get_current_user_id), db: mysql.connector.MySQLConnection = Depends(get_db)):
    cursor = db.cursor()
    try:
        cursor.execute(
            "INSERT INTO cartoes (numero, nome, validade, cvv, usuario_id) VALUES (%s, %s, %s, %s, %s)",
            (cartao.numero, cartao.nome, cartao.validade, cartao.cvv, current_user_id)
        )
        db.commit()
    except mysql.connector.Error as err:
        raise HTTPException(status_code=400, detail=f"Não foi possível cadastrar o cartão: {err}")
    finally:
        cursor.close()
        
    return {"status": "sucesso", "mensagem": "Cartão cadastrado."}

@app.post("/sessoes/checkin", status_code=status.HTTP_201_CREATED, summary="Inicia uma nova sessão de estacionamento")
def registrar_entrada(current_user_id: int = Depends(get_current_user_id), db: mysql.connector.MySQLConnection = Depends(get_db)):
    cursor = db.cursor(dictionary=True)
    
    cursor.execute("SELECT id FROM sessoes WHERE usuario_id = %s AND status = 'ATIVA'", (current_user_id,))
    if cursor.fetchone():
        cursor.close()
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Usuário já possui uma sessão de estacionamento ativa.")
    
    horario_agora = datetime.now()
    try:
        cursor.execute(
            "INSERT INTO sessoes (usuario_id, horario_entrada, status) VALUES (%s, %s, %s)",
            (current_user_id, horario_agora, 'ATIVA')
        )
        db.commit()
        nova_sessao_id = cursor.lastrowid
    except mysql.connector.Error as err:
        raise HTTPException(status_code=500, detail="Erro interno ao registrar entrada.")
    finally:
        cursor.close()
        
    return {"status": "sucesso", "sessao_id": nova_sessao_id, "horario_entrada": horario_agora.isoformat()}

@app.post("/sessoes/checkout", summary="Finaliza a sessão ativa e calcula o valor")
def registrar_saida(current_user_id: int = Depends(get_current_user_id), db: mysql.connector.MySQLConnection = Depends(get_db)):
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT id, horario_entrada FROM sessoes WHERE usuario_id = %s AND status = 'ATIVA'", (current_user_id,))
    sessao_ativa = cursor.fetchone()
    
    if not sessao_ativa:
        cursor.close()
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Nenhuma sessão ativa encontrada.")

    sessao_id = sessao_ativa['id']
    horario_entrada = sessao_ativa['horario_entrada']
    horario_saida = datetime.now()
    duracao = horario_saida - horario_entrada
    horas_totais = max(1, (duracao.total_seconds() + 3599) // 3600)
    valor_final = float(horas_totais * 5.0)

    try:
        cursor.execute(
            "UPDATE sessoes SET horario_saida = %s, valor_pago = %s, status = 'FINALIZADA' WHERE id = %s",
            (horario_saida, valor_final, sessao_id)
        )
        db.commit()
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail="Erro interno ao finalizar a sessão.")
    finally:
        cursor.close()

    return {"status": "sucesso", "mensagem": "Sessão finalizada!", "valor_pago": valor_final}
