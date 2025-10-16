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
# ... (Seus modelos Pydantic não precisam de alteração) ...
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
    model_config = ConfigDict(from_attributes=True)

class CartaoCreate(BaseModel):
    numero: str
    nome: str
    validade: str
    cvv: str
    
class CartaoPublic(BaseModel):
    id: int
    numero: str
    nome: str
    validade: str
    is_default: bool
    bandeira: str

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
# ... (Suas funções auxiliares não precisam de alteração) ...
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
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT id, nome, email, senha FROM usuarios WHERE email = %s", (email,))
    user_data = cursor.fetchone()
    cursor.close()
    if user_data:
        user_data['senha_hashed'] = user_data.pop('senha')
        return UsuarioInDB(**user_data)
    return None

async def get_current_user_id(token: str = Depends(oauth2_scheme)) -> int:
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Credenciais inválidas ou token expirado",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: Optional[int] = payload.get("user_id")
        if user_id is None:
            raise credentials_exception
        return user_id
    except (JWTError, ValueError):
        raise credentials_exception

# --- 5. ROTAS DA API ---

# --- ROTAS DE USUÁRIOS E CARTÕES (sem alterações) ---
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
        if err.errno == 1062:
            raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Este email já está cadastrado.")
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=f"Erro no banco de dados: {err}")
    finally:
        cursor.close()
    
    return {"status": "sucesso", "mensagem": "Usuário criado com sucesso!"}

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
    cursor.execute("SELECT COUNT(*) as count FROM cartoes WHERE usuario_id = %s", (user_in_db.id,))
    card_count = cursor.fetchone()['count']
    cursor.execute("SELECT id, horario_entrada FROM sessoes WHERE usuario_id = %s AND status = 'ATIVA'", (user_in_db.id,))
    sessao_ativa = cursor.fetchone()
    
    active_session_info = None
    if sessao_ativa:
        active_session_info = {
            "sessao_id": sessao_ativa['id'],
            "horario_entrada": sessao_ativa['horario_entrada'].isoformat()
        }
    cursor.close()
    access_token = criar_token_acesso(data={"user_id": user_in_db.id})
    
    return LoginResponse(
        access_token=access_token, token_type="bearer", user_id=user_in_db.id,
        user_name=user_in_db.nome, card_count=card_count, active_session_info=active_session_info
    )

# ... (Rotas de cartões permanecem iguais)
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

@app.get("/cartoes", response_model=List[CartaoPublic], summary="Lista os cartões do usuário logado")
def get_cartoes_do_usuario(current_user_id: int = Depends(get_current_user_id), db: mysql.connector.MySQLConnection = Depends(get_db)):
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT id, numero, nome, validade, is_default FROM cartoes WHERE usuario_id = %s", (current_user_id,))
    cartoes = cursor.fetchall()
    cursor.close()
    
    cartoes_publicos = []
    for cartao in cartoes:
        cartao['is_default'] = bool(cartao.get('is_default', 0))
        if cartao.get('numero') and len(cartao['numero']) > 4:
            cartao['numero'] = f"**** **** **** {cartao['numero'][-4:]}"
        cartao['bandeira'] = "visa" 
        cartoes_publicos.append(CartaoPublic(**cartao))
            
    return cartoes_publicos

@app.post("/cartoes/{cartao_id}/definir-padrao", status_code=status.HTTP_204_NO_CONTENT, summary="Define um cartão como padrão para pagamento")
def definir_cartao_padrao(cartao_id: int, current_user_id: int = Depends(get_current_user_id), db: mysql.connector.MySQLConnection = Depends(get_db)):
    cursor = db.cursor()
    cursor.execute("SELECT id FROM cartoes WHERE id = %s AND usuario_id = %s", (cartao_id, current_user_id))
    if not cursor.fetchone():
        cursor.close()
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Cartão não encontrado ou não pertence a este usuário.")

    try:
        cursor.execute("UPDATE cartoes SET is_default = FALSE WHERE usuario_id = %s", (current_user_id,))
        cursor.execute("UPDATE cartoes SET is_default = TRUE WHERE id = %s AND usuario_id = %s", (cartao_id, current_user_id))
        db.commit()
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=f"Erro no banco de dados ao definir cartão padrão: {err}")
    finally:
        cursor.close()
    return None

@app.delete("/cartoes/{cartao_id}", status_code=status.HTTP_204_NO_CONTENT, summary="Exclui um cartão do usuário logado")
def excluir_cartao(cartao_id: int, current_user_id: int = Depends(get_current_user_id), db: mysql.connector.MySQLConnection = Depends(get_db)):
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT id FROM cartoes WHERE id = %s AND usuario_id = %s", (cartao_id, current_user_id))
    cartao_para_excluir = cursor.fetchone()

    if not cartao_para_excluir:
        cursor.close()
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Cartão não encontrado ou não pertence a este usuário.")

    try:
        cursor.execute("DELETE FROM cartoes WHERE id = %s", (cartao_id,))
        db.commit()
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail=f"Erro no banco de dados ao excluir o cartão: {err}")
    finally:
        cursor.close()
    return None

# --- ROTAS DE SESSÃO (CHECK-IN/CHECKOUT) ---

@app.post("/sessoes/checkin", status_code=status.HTTP_201_CREATED, summary="Inicia uma nova sessão de estacionamento")
def registrar_entrada(current_user_id: int = Depends(get_current_user_id), db: mysql.connector.MySQLConnection = Depends(get_db)):
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT id FROM sessoes WHERE usuario_id = %s AND status = 'ATIVA'", (current_user_id,))
    if cursor.fetchone():
        cursor.close()
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail="Usuário já possui uma sessão de estacionamento ativa.")
    
    horario_agora = datetime.now()
    try:
        cursor.execute("INSERT INTO sessoes (usuario_id, horario_entrada, status) VALUES (%s, %s, %s)", (current_user_id, horario_agora, 'ATIVA'))
        db.commit()
        nova_sessao_id = cursor.lastrowid
    except mysql.connector.Error as err:
        raise HTTPException(status_code=500, detail="Erro interno ao registrar entrada.")
    finally:
        cursor.close()
    return {"status": "sucesso", "sessao_id": nova_sessao_id, "horario_entrada": horario_agora.isoformat()}

# ========================================================
# ROTA DE STATUS DE SESSÃO - ADICIONADA PARA CORRIGIR O BUG
# ========================================================
@app.get("/sessoes/status", summary="Verifica se o usuário tem uma sessão ativa")
def verificar_status_sessao(current_user_id: int = Depends(get_current_user_id), db: mysql.connector.MySQLConnection = Depends(get_db)):
    cursor = db.cursor(dictionary=True)
    cursor.execute(
        "SELECT id AS sessao_id, horario_entrada FROM sessoes WHERE usuario_id = %s AND status = 'ATIVA'",
        (current_user_id,)
    )
    sessao_ativa = cursor.fetchone()
    cursor.close()

    if not sessao_ativa:
        # Retorna 404 para o Android saber que não há sessão e continuar tentando
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Nenhuma sessão ativa encontrada.")

    # Se encontrar, retorna os dados da sessão
    sessao_ativa['horario_entrada'] = sessao_ativa['horario_entrada'].isoformat()
    return sessao_ativa

@app.get("/sessoes/checkout/preview", summary="Prevê o valor do checkout sem finalizar a sessão")
def prever_valor_saida(current_user_id: int = Depends(get_current_user_id), db: mysql.connector.MySQLConnection = Depends(get_db)):
    cursor = db.cursor(dictionary=True)
    cursor.execute("SELECT id, horario_entrada FROM sessoes WHERE usuario_id = %s AND status = 'ATIVA'", (current_user_id,))
    sessao_ativa = cursor.fetchone()
    cursor.close()

    if not sessao_ativa:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Nenhuma sessão ativa encontrada.")

    horario_entrada = sessao_ativa['horario_entrada']
    horario_agora = datetime.now()
    duracao = horario_agora - horario_entrada
    
    horas_totais = max(1, (duracao.total_seconds() + 3599) // 3600)
    valor_previsto = float(horas_totais * 5.0)
    
    return {"valor_previsto": valor_previsto}

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
        cursor.execute("UPDATE sessoes SET horario_saida = %s, valor_pago = %s, status = 'FINALIZADA' WHERE id = %s", (horario_saida, valor_final, sessao_id))
        db.commit()
    except mysql.connector.Error as err:
        db.rollback()
        raise HTTPException(status_code=500, detail="Erro interno ao finalizar a sessão.")
    finally:
        cursor.close()

    return {"status": "sucesso", "mensagem": "Sessão finalizada!", "valor_pago": valor_final}

# ========================================================
# ROTA SIMULADA - HORÁRIOS DE PICO (GOOGLE API)
# ========================================================
@app.get("/estabelecimento/horarios-pico", summary="SIMULAÇÃO da API do Google para horários de pico")
def get_horarios_pico():
    """
    Em um projeto real, esta rota usaria a chave da Google Places API
    para buscar os dados de um lugar específico (o estacionamento).
    Para este TCC, retornamos dados fixos e realistas.
    """
    agora = datetime.now()
    hora_atual = agora.hour
    
    # Simula a lotação atual com base na hora
    lotacao_atual = 0
    if 7 <= hora_atual < 10:  # Manhã
        lotacao_atual = 65
    elif 11 <= hora_atual < 14: # Meio-dia
        lotacao_atual = 90
    elif 17 <= hora_atual < 19: # Fim de tarde
        lotacao_atual = 80
    elif 20 <= hora_atual < 22: # Noite
        lotacao_atual = 50
    else:
        lotacao_atual = 25 # Madrugada/outros

    # Simula o status ("Pouco movimentado", "Movimentado", etc.)
    status_movimento = "Normal"
    if lotacao_atual >= 85:
        status_movimento = "Muito movimentado"
    elif lotacao_atual >= 60:
        status_movimento = "Movimentado"
    elif lotacao_atual < 30:
        status_movimento = "Pouco movimentado"
        
    return {
        "place_id": "CH_ESTACIONAMENTO_TCC", # ID Fictício do Lugar
        "status_movimento_atual": status_movimento,
        "lotacao_percentual_atual": lotacao_atual,
        "dados_semana": [
            {"dia": "Dom", "picos": [20, 30, 40, 50, 60, 50, 40]},
            {"dia": "Seg", "picos": [50, 70, 90, 85, 95, 80, 60]},
            {"dia": "Ter", "picos": [55, 75, 92, 88, 98, 82, 65]},
            {"dia": "Qua", "picos": [52, 72, 91, 86, 96, 81, 62]},
            {"dia": "Qui", "picos": [58, 78, 95, 90, 99, 85, 68]},
            {"dia": "Sex", "picos": [60, 80, 98, 95, 100, 90, 75]},
            {"dia": "Sáb", "picos": [40, 50, 70, 80, 90, 85, 70]},
        ]
    }