from fastapi import FastAPI
from fastapi import HTTPException
from pydantic import BaseModel
import mysql.connector

app = FastAPI()

# Modelo para receber os dados do usuário
class Usuario(BaseModel):
    nome: str
    email: str
    senha: str

# Conexão com o MySQL
def get_db_connection():
    return mysql.connector.connect(
        host="127.0.0.1",   # ajuste conforme seu servidor
        user="root",        # usuário do MySQL
        password="unip123", # senha do MySQL
        database="TccBd"
    )

# Rota para cadastrar usuário
@app.post("/usuarios/cadastrar")
def cadastrar_usuario(usuario: Usuario):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute(
        "INSERT INTO usuarios (nome, email, senha) VALUES (%s, %s, %s)",
        (usuario.nome, usuario.email, usuario.senha)
    )
    conn.commit()
    cursor.close()
    conn.close()
    return {"status": "sucesso", "usuario": usuario.nome}

@app.post("/usuarios/login")
def login_usuario(usuario: Usuario):
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)

    cursor.execute(
        "SELECT * FROM usuarios WHERE email = %s AND senha = %s",
        (usuario.email, usuario.senha)
    )
    result = cursor.fetchone()

    cursor.close()
    conn.close()

    if result:
        return {"status": "sucesso", "usuario": result["nome"]}
    else:
        raise HTTPException(status_code=401, detail="Credenciais inválidas")
