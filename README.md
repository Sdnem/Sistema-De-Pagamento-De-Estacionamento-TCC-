# Sistema de Pagamento de Estacionamento - Projeto de TCC

![Capa do Projeto](https://imgur.com/a/bEgjudr)

## 📖 Sobre o Projeto

Este é um projeto de Conclusão de Curso (TCC) que simula um sistema completo de pagamento de estacionamento. A solução é composta por um **aplicativo móvel em Android (Kotlin/Jetpack Compose)** para a experiência do usuário e um **servidor backend em Python (FastAPI)** que gerencia toda a lógica de negócio, autenticação e integração com pagamentos.

O fluxo principal permite que um usuário se cadastre, faça login, e ao escanear um QR Code de "entrada", inicie uma sessão de estacionamento. O aplicativo então exibe o tempo decorrido e o custo atualizado em tempo real. Para finalizar, o usuário pode realizar o pagamento via **Pix**, utilizando uma integração real com a API da **OpenPix (Woovi)**, e receber a confirmação para deixar o estacionamento.

O projeto foi construído com foco em boas práticas de arquitetura, como a separação de responsabilidades (frontend, backend) e a utilização de um padrão MVVM no lado do Android.

---

## ✨ Funcionalidades Principais

*   **Autenticação de Usuário:** Sistema completo de cadastro e login com gerenciamento de sessão via tokens JWT.
*   **Início de Sessão via QR Code:** Simulação de uma cancela de entrada onde o usuário escaneia um QR Code para iniciar sua contagem de tempo.
*   **Monitoramento em Tempo Real:** A tela principal do app exibe o tempo decorrido e o custo da sessão, com o valor sendo atualizado periodicamente a partir do servidor.
*   **Integração de Pagamento Real (Pix):**
    *   Geração de cobranças Pix dinâmicas através de uma integração backend-to-backend com a API da OpenPix.
    *   Exibição do QR Code Pix e do código "copia e cola" no aplicativo.
    *   Polling para verificação automática do status do pagamento.
*   **Finalização de Sessão:** Após a confirmação do pagamento, a sessão é marcada como finalizada no banco de dados.

---

## 🛠️ Tecnologias Utilizadas

Este projeto é um monorepo que contém duas aplicações principais:

### 📱 **Frontend (Aplicativo Android)**
*   **Linguagem:** [Kotlin](https://kotlinlang.org/)
*   **UI:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
*   **Arquitetura:** MVVM (Model-View-ViewModel) com `ViewModel` e `State`
*   **Navegação:** [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
*   **Comunicação com API:** [Retrofit](https://square.github.io/retrofit/) & [Gson](https://github.com/google/gson)
*   **Assincronismo:** Kotlin Coroutines & `LaunchedEffect`
*   **Leitura de QR Code:** [CameraX](https://developer.android.com/training/camerax) & [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)

### ⚙️ **Backend (Servidor API)**
*   **Linguagem:** [Python 3](https://www.python.org/)
*   **Framework:** [FastAPI](https://fastapi.tiangolo.com/)
*   **Banco de Dados:** [SQLite](https://www.sqlite.org/index.html) (usando [SQLAlchemy ORM](https://www.sqlalchemy.org/))
*   **Autenticação:** JWT (JSON Web Tokens) com `python-jose` e `passlib`.
*   **Integração de Pagamento:** Biblioteca oficial da [OpenPix/Woovi](https://developers.woovi.com/).
*   **Validação de Dados:** [Pydantic](https://docs.pydantic.dev/latest/)

---

## 🚀 Como Executar o Projeto

Para executar a solução completa, você precisará rodar o backend e o frontend simultaneamente.

### Pré-requisitos
*   [Python 3.8+](https://www.python.org/downloads/)
*   [Android Studio](https://developer.android.com/studio) (versão recente)
*   Um celular Android físico (recomendado para testar a câmera) ou um emulador.
*   Uma conta de desenvolvedor na [plataforma OpenPix](https://openpix.com.br/) para obter seu `App ID`.

### 1. Configurando o Backend

# 1. Navegue até a pasta do backend
cd api-backend-python

# 2. Crie e ative um ambiente virtual (venv)
# Este comando cria uma pasta 'venv' com uma instalação isolada do Python
python -m venv venv

# Para ativar o ambiente no Windows (PowerShell):
./venv/Scripts/Activate.ps1

# Para ativar no macOS/Linux:
source venv/bin/activate

# 3. Com o ambiente ativado, instale as dependências do projeto
# O pip lerá o arquivo requirements.txt e instalará tudo o que o backend precisa
pip install -r requirements.txt

# 4. Configure sua chave da API da OpenPix
# Abra o arquivo 'main.py' no seu editor de código e substitua 
# o texto "SEU_APP_ID_AQUI" pela sua chave real da OpenPix/Woovi.

# 5. Execute o servidor de desenvolvimento
# A flag --host 0.0.0.0 é crucial para permitir que seu celular (em outra máquina na rede)
# consiga se comunicar com o servidor.
uvicorn main:app --host 0.0.0.0 --reload

O servidor estará rodando em `http://[SEU_IP_LOCAL]:8000`.

### 2. Configurando o Frontend

1.  Abra a pasta do projeto no Android Studio.
2.  **Importante:** Encontre o endereço IP da sua máquina na sua rede local (ex: `192.168.1.10`).
3.  No Android Studio, vá até o arquivo `remote/RetrofitClient.kt` e altere a `BASE_URL` para o endereço IP do seu backend.

## 🗃️ Modelo do Banco de Dados

A persistência dos dados é gerenciada por um banco de dados **SQLite**, ideal para a prototipagem e complexidade deste projeto. A interação com o banco é feita através do **SQLAlchemy ORM**, que mapeia classes Python para tabelas do banco, permitindo uma manipulação segura e organizada dos dados.

O esquema foi projetado para suportar o fluxo completo, desde o cadastro de usuários e seus métodos de pagamento até o registro detalhado de cada sessão de estacionamento.

### Tabela `usuarios`
Armazena as informações de cada usuário cadastrado no sistema.

| Coluna          | Tipo            | Descrição                                                     |
| :-------------- | :-------------- | :------------------------------------------------------------ |
| `id`            | Integer (PK)    | Identificador único do usuário.                               |
| `nome`          | String          | Nome completo do usuário.                                     |
| `email`         | String (Unique) | E-mail usado para login, deve ser único.                      |
| `hashed_password` | String          | A senha do usuário, armazenada de forma segura (hash).        |
| `sessoes`       | Relationship    | Relação com as sessões de estacionamento deste usuário.     |
| `cartoes`       | Relationship    | Relação com os cartões de crédito cadastrados por este usuário. |

<br>

### Tabela `cartoes`
Guarda as informações dos cartões de crédito/débito que os usuários salvam para futuros pagamentos.

**Nota de Segurança:** Em um sistema real, informações sensíveis como o número completo do cartão e o CVV **nunca** são armazenadas diretamente. Geralmente, armazena-se apenas um *token* gerado por um gateway de pagamento (como Stripe, Adyen, etc.), os últimos 4 dígitos e a data de validade para fins de exibição ao usuário. Este modelo reflete essa prática.

| Coluna         | Tipo         | Descrição                                                         |
| :------------- | :----------- | :---------------------------------------------------------------- |
| `id`           | Integer (PK) | Identificador único do cartão.                                    |
| `usuario_id`   | Integer (FK) | Chave estrangeira que referencia o `id` da tabela `usuarios`.       |
| `ultimos_4`    | String       | Os últimos 4 dígitos do número do cartão para identificação.        |
| `bandeira`     | String       | A bandeira do cartão (ex: "Visa", "Mastercard").                  |
| `validade`     | String       | Data de validade do cartão (ex: "12/28").                         |
| `token_gateway`| String       | Token seguro que representa o cartão no gateway de pagamento.   |

<br>

### Tabela `sessoes_estacionamento`
Registra cada sessão de estacionamento iniciada por um usuário.

| Coluna              | Tipo            | Descrição                                                                         |
| :------------------ | :-------------- | :-------------------------------------------------------------------------------- |
| `id`                | Integer (PK)    | Identificador único da sessão.                                                    |
| `usuario_id`        | Integer (FK)    | Chave estrangeira que referencia o `id` da tabela `usuarios`.                     |
| `horario_entrada`   | DateTime        | Data e hora exatas em que a sessão foi iniciada.                                  |
| `horario_saida`     | DateTime (Null) | Data e hora em que a sessão foi finalizada (fica nulo enquanto ativa).            |
| `valor_pago`        | Float (Null)    | Valor final pago pelo usuário ao final da sessão.                                 |
| `metodo_pagamento`  | String          | Método usado (ex: "Pix", "Cartao"). Se for cartão, pode referenciar o `id` da tabela `cartoes`. |
| `correlation_id`    | String (Null)   | ID da cobrança gerado pela OpenPix ou outro gateway, para reconciliação.         |

### 📝 Nota Sobre a Criação do Banco de Dados

**Você não precisa criar o banco de dados manualmente!**

O projeto está configurado para usar **SQLite**, um banco de dados leve que armazena tudo em um único arquivo. A biblioteca **SQLAlchemy ORM**, que gerencia nosso banco de dados, foi programada para criar o arquivo e todas as tabelas necessárias automaticamente na primeira vez que o servidor é iniciado.

#### Como Funciona?

1.  **Definição dos Modelos:** No arquivo `models.py` (ou onde você definiu seus modelos), as classes Python como `Usuario` e `SessaoEstacionamento` representam as tabelas do banco de dados.
2.  **Criação Automática:** No arquivo principal `main.py`, existe uma linha de código que instrui o SQLAlchemy a verificar se as tabelas existem e, caso não existam, a criá-las. O código responsável por isso é:

    