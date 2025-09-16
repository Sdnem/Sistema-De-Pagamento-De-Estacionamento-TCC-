o sistema consiste de 3 partes:
-Aplicação
-API
-Banco de Dados (MySql)

**O arquivo Main.py é a API**

Para executar o aplicativo é necessário executar o Android Studio,
criar o banco de dados e configurar a API para acessar o banco.

Tabelas no Banco:

crate table usuarios (
  id int not null auto_increment,
  nome varchar(30),
  email varchar(30),
  senha varchar(30),
  primary key (id)
)

create table cartoes (
  id int not null auto_increment,
  banco varchar(30),
  nome varchar(30),
  numero int,
  validade int,
  cvc int,
  primary key (id)
)

crate table carros (
  id int not null auto_increment,
  nome varchar(30),
  placa varchar(7),
  primary key (id)
)
