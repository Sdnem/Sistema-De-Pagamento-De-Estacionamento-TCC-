from fastapi import FastAPI, HTTPException, status, Header
from pydantic import BaseModel
import mysql.connector
from typing import Optional

app = FastAPI()

# --- MODELOS ---
# Modelo para o usuário (cadastro e login)
class Usuario(BaseModel):
    nome: Optional[str] = None
    email: str
    senha: str

# Modelo para o cartão
class Cartao(BaseModel):
    numero: str
    nome: str
    validade: str
    cvv: str
    usuario_id: int

# --- FUNÇÕES AUXILIARES ---
def get_db_connection():
    try:
        return mysql.connector.connect(
            host="127.0.0.1",
            user="root",
            password="unip123", # Sua senha do MySQL
            database="TccBd"
        )
    except mysql.connector.Error as err:
        print(f"Erro de conexão com o banco: {err}")
        raise HTTPException(status_code=500, detail="Erro interno no servidor - DB connection.")

# --- ROTAS DA API ---

# ROTA DE CADASTRO DE USUÁRIO
@app.post("/usuarios/cadastrar")
def cadastrar_usuario(usuario: Usuario):
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO usuarios (nome, email, senha) VALUES (%s, %s, %s)",
            (usuario.nome, usuario.email, usuario.senha)
        )
        conn.commit()
    except mysql.connector.Error as err:
        raise HTTPException(status_code=400, detail=f"Não foi possível cadastrar: {err}")
    finally:
        cursor.close()
        conn.close()
    return {"status": "sucesso", "usuario_criado": usuario.email}

# ROTA DE LOGIN DE USUÁRIO
@app.post("/usuarios/login")
def login_usuario(usuario: Usuario):
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    cursor.execute(
        "SELECT * FROM usuarios WHERE email = %s AND senha = %s",
        (usuario.email, usuario.senha)
    )
    user_in_db = cursor.fetchone()
    cursor.close()
    conn.close()
    if user_in_db:
        return {"status": "sucesso", "usuario": user_in_db}
    else:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Credenciais inválidas")

# ==========================================================
# NOVA ROTA PARA CADASTRAR CARTÃO (ADICIONADA)
# ==========================================================
@app.post("/cartoes/cadastrar")
def cadastrar_cartao(cartao: Cartao): # O token virá no Header, mas não o usaremos na versão simplificada
    conn = get_db_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "INSERT INTO cartoes (numero, nome, validade, cvv, usuario_id) VALUES (%s, %s, %s, %s, %s)",
            (cartao.numero, cartao.nome, cartao.validade, cartao.cvv, cartao.usuario_id)
        )
        conn.commit()
    except mysql.connector.Error as err:
        # Adicionar um log mais detalhado do erro no servidor
        print(f"Erro ao inserir cartão no banco de dados: {err}")
        raise HTTPException(status_code=400, detail=f"Não foi possível cadastrar o cartão: {err}")
    finally:
        cursor.close()
        conn.close()
        
    return {"status": "sucesso", "mensagem": "Cartão cadastrado."}

# ... (todo o seu código existente, como /usuarios/login e /cartoes/cadastrar, fica aqui em cima) ...

# ==========================================================
#  NOVA ROTA: BUSCAR OS CARTÕES DE UM USUÁRIO ESPECÍFICO
# ==========================================================
@app.get("/cartoes/{usuario_id}")
def get_cartoes_por_usuario(usuario_id: int):
    conn = get_db_connection()
    # Usar 'dictionary=True' é crucial para que o resultado venha no formato JSON que o Android espera.
    cursor = conn.cursor(dictionary=True)
    
    try:
        # 1. Busca no banco todos os cartões onde a coluna 'usuario_id' corresponde ao ID recebido na URL.
        cursor.execute(
            "SELECT id, numero, nome, validade FROM cartoes WHERE usuario_id = %s",
            (usuario_id,) # A vírgula é importante, pois o execute espera uma tupla.
        )
        cartoes = cursor.fetchall()
        
        # 2. (Opcional, mas boa prática de segurança)
        # Modifica o número do cartão para mostrar apenas os últimos 4 dígitos.
        for cartao in cartoes:
            if cartao['numero'] and len(cartao['numero']) > 4:
                cartao['numero'] = f"**** **** **** {cartao['numero'][-4:]}"

    except mysql.connector.Error as err:
        print(f"Erro ao buscar cartões: {err}")
        raise HTTPException(status_code=500, detail="Erro interno do servidor ao buscar cartões.")
    finally:
        cursor.close()
        conn.close()
        
    # 3. Retorna a lista de cartões para o aplicativo Android.
    # Se nenhum cartão for encontrado, retornará uma lista vazia: [].
    return cartoes

# ... (suas outras rotas) ...
from datetime import datetime # Adicione este import no topo do arquivo

class SessaoEntrada(BaseModel):
    usuario_id: int
    # Opcional: você pode enviar o ID do estacionamento vindo do QR Code
    # estacionamento_id: int 

# ==========================================================
#  NOVA ROTA: REGISTRAR ENTRADA NO ESTACIONAMENTO (CHECK-IN)
# ==========================================================
@app.post("/sessoes/checkin")
def registrar_entrada(sessao_entrada: SessaoEntrada):
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    
    # Primeiro, verifica se já não existe uma sessão ativa para este usuário
    cursor.execute(
        "SELECT id FROM sessoes WHERE usuario_id = %s AND status = 'ATIVA'",
        (sessao_entrada.usuario_id,)
    )
    sessao_ativa = cursor.fetchone()

    if sessao_ativa:
        # O usuário já tem uma sessão ativa, não pode iniciar outra.
        # Retornamos os dados da sessão existente.
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT, 
            detail="Usuário já possui uma sessão de estacionamento ativa."
        )

    try:
        horario_agora = datetime.now()
        cursor.execute(
            "INSERT INTO sessoes (usuario_id, horario_entrada, status) VALUES (%s, %s, %s)",
            (sessao_entrada.usuario_id, horario_agora, 'ATIVA')
        )
        conn.commit()
        nova_sessao_id = cursor.lastrowid

    except mysql.connector.Error as err:
        print(f"Erro ao criar sessão de check-in: {err}")
        raise HTTPException(status_code=500, detail="Erro interno ao registrar entrada.")
    finally:
        cursor.close()
        conn.close()
        
    return {
        "status": "sucesso", 
        "mensagem": "Entrada registrada com sucesso!", 
        "sessao_id": nova_sessao_id,
        "horario_entrada": horario_agora.isoformat() # Envia o horário de volta para o app
    }
