# Gestor de Cartões - Guia de Configuração e Execução

## Visão Geral

Este projeto é um sistema completo para gerenciamento de cartões de crédito, composto por três partes principais:
1.  **Backend:** Uma API RESTful desenvolvida em Python com o framework Flask, responsável pela lógica de negócio e comunicação com o banco de dados.
2.  **Frontend:** Um aplicativo móvel desenvolvido em Kotlin para Android usando Jetpack Compose.
3.  **Banco de Dados:** Um banco de dados MySQL para armazenar os dados dos usuários e cartões.

Este guia detalha os passos necessários para configurar e executar todas as partes do sistema em um ambiente de desenvolvimento local.

## Pré-requisitos

Antes de começar, garanta que você tenha os seguintes softwares instalados em sua máquina:

* **MySQL Server:** O banco de dados para armazenar os dados.
* **Python 3.x:** A linguagem de programação para o backend.
* **Android Studio:** O ambiente de desenvolvimento para o aplicativo Android.
* **Um editor de código:** Como VS Code, Sublime Text, etc., para editar o código Python.

---

### Passo 1: Configuração do Banco de Dados (MySQL)

O backend precisa de um banco de dados MySQL para funcionar.

1.  **Instale o MySQL Server** no seu computador.
2.  Abra um terminal de cliente MySQL (ou uma ferramenta como DBeaver/HeidiSQL).
3.  **Crie o banco de dados**. O nome utilizado na API é `TccBd`.
    ```sql
    CREATE DATABASE TccBd;
    ```
4.  **Selecione o banco de dados** que você acabou de criar:
    ```sql
    USE TccBd;
    ```
5.  **Crie as tabelas**. Copie e cole os comandos SQL abaixo.

    ```sql
    -- Tabela de Usuários
    CREATE TABLE usuarios (
        id INT NOT NULL AUTO_INCREMENT,
        nome VARCHAR(30),
        email VARCHAR(30) UNIQUE,
        senha_hash VARCHAR(255) NOT NULL,
        PRIMARY KEY (id)
    );

    -- Tabela de Cartões
    CREATE TABLE cartoes (
        id INT NOT NULL AUTO_INCREMENT,
        banco VARCHAR(30),
        nome VARCHAR(30),
        numero INT,
        validade INT,
        cvv INT, 
        usuario_id INT, 
        PRIMARY KEY (id),
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
    );

    -- Tabela de Carros (atualmente não utilizada pela API)
    CREATE TABLE carros (
        id INT NOT NULL AUTO_INCREMENT,
        nome VARCHAR(30),
        placa VARCHAR(7),
        PRIMARY KEY (id)
    );
    ```

### Passo 2: Configuração do Backend (Python/Flask)

O servidor que conecta o aplicativo ao banco de dados.

1.  Navegue até a pasta onde o arquivo `main.py` está localizado.
2.  **Crie um ambiente virtual** (altamente recomendado):
    ```sh
    python -m venv venv
    ```
3.  **Ative o ambiente virtual**:
    * No Windows: `venv\Scripts\activate`
    * No macOS/Linux: `source venv/bin/activate`
4.  **Instale as dependências** necessárias com o pip. As dependências são baseadas nas importações do arquivo `main.py`.
    ```sh
    pip install Flask Flask-Bcrypt mysql-connector-python PyJWT
    ```
5.  **Configure a conexão com o banco de dados**. Abra o arquivo `main.py` e edite o dicionário `db_config` com suas credenciais do MySQL.
    ```python
    db_config = {
        'host': '127.0.0.1',
        'user': 'seu_usuario_mysql',      
        'password': 'sua_senha_mysql',    
        'database': 'TccBd'               # Nome do banco de dados criado no Passo 1
    }
    ```
6.  **Ajuste a porta do servidor**. O aplicativo Android está configurado para se conectar à porta `8000`. O script Python precisa ser executado nessa mesma porta. Altere a **última linha** do arquivo `main.py`:

    * **De:** `app.run(debug=True, host='0.0.0.0')`
    * **Para:** `app.run(debug=True, host='0.0.0.0', port=8000)`

7.  **Execute o servidor**:
    ```sh
    python main.py
    ```
    Se tudo estiver correto, você verá uma mensagem indicando que o servidor está rodando em `http://0.0.0.0:8000/`. Deixe este terminal aberto.

### Passo 3: Configuração do Frontend (Android)

Agora, vamos configurar o aplicativo para se comunicar com o backend.

1.  **Abra o projeto** na pasta `MyApplication` com o Android Studio.
2.  O Android Studio irá sincronizar o projeto usando o Gradle. As dependências como Retrofit, Gson e Compose serão baixadas automaticamente.
3.  **Verifique a URL da API**. Os arquivos `RetrofitClient.kt` e `RetrofitInstance.kt` já estão configurados com o endereço IP `10.0.2.2`, que é o endereço especial que o emulador Android usa para se conectar ao `localhost` da sua máquina, e a porta `8000`. Nenhuma alteração é necessária aqui.
4.  **Execute o aplicativo**.
    * Selecione um emulador Android na lista de dispositivos.
    * Clique no botão "Run 'app'" (ícone de play verde).
    * O Android Studio irá compilar, instalar e iniciar o aplicativo no emulador.

### Como Testar a Aplicação

Com tudo rodando, siga este fluxo para testar:

1.  **Garanta que o servidor MySQL esteja em execução.**
2.  **Garanta que o servidor Python (backend) esteja rodando** no seu terminal.
3.  **Inicie o aplicativo Android** no emulador.
4.  **Cadastre um novo usuário** (utilizando a rota `usuarios/cadastrar` ).
5.  **Faça login** com o usuário criado (utilizando a rota `usuarios/login` ).
6.  Navegue até a tela **"Meus Cartões"**.
7.  Clique em **"Adicionar Cartão"**, preencha os dados e salve (isso acionará a rota `POST /cartoes` ).
8.  O novo cartão deve aparecer na lista (acionando a rota `GET /cartoes` ).
9.  Clique em **"Remover"** para testar a funcionalidade de exclusão (acionando a rota `DELETE /cartoes/{cartaoId}` ).
