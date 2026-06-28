# Raízes do Nordeste — Back-End

API REST desenvolvida para apoiar o fluxo operacional da rede de restaurantes “Raízes do Nordeste”. A aplicação contempla cadastro e autenticação de usuários, autorização por perfis, controle de estoque por unidade, gestão de pedidos, registro do canal de origem do pedido, integração simulada com gateway de pagamento e documentação via Swagger/OpenAPI.

## Links e evidências

* Repositório GitHub: `https://github.com/Luc033/RaizesNordeste`
* Swagger UI local: `http://localhost:8080/swagger-ui/index.html`
* Coleção Postman: `Raizes.postman_collection.json`
* Deploy: não aplicado ao escopo do MVP local

A coleção Postman encontra-se no repositório do projeto e contém os cenários utilizados para validação funcional da API, incluindo autenticação, autorização, criação de pedido, validações de regra de negócio, pagamento mock e atualização de status.

## Tecnologias utilizadas

* Java 21
* Spring Boot 4.0.6
* Maven
* Spring Web MVC
* Spring Data JPA
* Spring Security
* Bean Validation
* PostgreSQL Driver
* Flyway Migration
* Lombok
* Spring Boot DevTools
* Springdoc OpenAPI 2.7.0
* Auth0 Java JWT 4.4.0
* Docker
* PostgreSQL 15 Alpine
* Mock.io

## Pré-requisitos

Para executar o projeto localmente, é necessário possuir:

* JDK 21 instalado
* Maven instalado
* Docker instalado
* Postman instalado
* Porta `8080` disponível para a API
* Porta `5433` disponível para o PostgreSQL

## Como executar

### 1. Clonar o repositório

```bash
git clone https://github.com/Luc033/RaizesNordeste
cd RaizesNordeste
```

### 2. Subir o banco de dados PostgreSQL com Docker

Execute o comando abaixo para criar e iniciar o container do PostgreSQL:

```bash
docker run --name postgres-raizes 
  -e POSTGRES_USER=admin 
  -e POSTGRES_PASSWORD=password 
  -e POSTGRES_DB=db_raizes_nordeste 
  -p 5433:5432 
  -d postgres:15-alpine
```

Caso o container já exista, inicie-o com:

```bash
docker start postgres-raizes
```

### 3. Configurar o `application.yaml`

O arquivo `application.yaml` deve estar localizado em:

```text
src/main/resources/application.yaml
```

Configuração utilizada para execução local:

```yaml
spring:
  application:
    name: RaizesNordeste
  datasource:
    url: jdbc:postgresql://0.0.0.0:5433/db_raizes_nordeste?sslmode=disable
    username: admin
    password: password
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    show-sql: true
  flyway:
    enabled: true
    baseline-on-migrate: true

gateway:
  pagamento:
    mock:
      url: "https://api.mocki.io/v2/didt58mc"

logging:
  file:
    name: "raizes-nordeste.log"
```

As credenciais acima são destinadas exclusivamente ao ambiente local de desenvolvimento e testes acadêmicos.

### 4. Executar a aplicação

Na raiz do projeto, execute:

```bash
mvn clean install
mvn spring-boot:run
```

Após a inicialização, a API estará disponível em:

```text
http://localhost:8080
```

A documentação Swagger poderá ser acessada em:

```text
http://localhost:8080/swagger-ui/index.html
```

## Banco de dados e migrations

O projeto utiliza Flyway para criação e versionamento da estrutura do banco de dados. Ao iniciar a aplicação, as migrations são executadas automaticamente, criando as tabelas necessárias e inserindo os dados iniciais definidos nos scripts SQL do projeto.

Esses dados iniciais são utilizados como base para execução dos testes funcionais via Postman, especialmente nos fluxos de unidades, produtos, estoque, pedidos e pagamentos.

## Como executar os testes automatizados

Para executar a suíte de testes automatizados com JUnit, MockMvc e Mockito, utilize:

```bash
mvn test
```

Os testes automatizados validam regras de negócio, tratamento de exceções, fluxo de pedidos, controle de estoque, pagamento mock e comportamento dos controladores da aplicação.

## Como executar os testes no Postman

### 1. Importar a coleção

No Postman, importe o arquivo:

```text
Raizes.postman_collection.json
```

### 2. Configurar o ambiente

Crie ou selecione um ambiente no Postman com a seguinte variável:

```text
localhost = http://localhost:8080
```

A coleção também utiliza variáveis para armazenar tokens JWT durante a execução dos testes:

```text
token_adm
token_cliente
token_cozinha
```

Essas variáveis são preenchidas automaticamente pelos scripts das requisições de login.

### 3. Executar a autenticação inicial

Antes de executar os testes principais, execute a requisição auxiliar:

```text
Login admin
```

Essa requisição autentica o usuário administrador e salva automaticamente o token JWT na variável:

```text
token_adm
```

Esse token é utilizado em requisições administrativas, como a criação do funcionário de cozinha.

### 4. Ordem sugerida dos testes

A execução dos testes principais deve seguir a ordem numérica da coleção Postman:

```text
01 - Falta Aceite Termos
02 - Criar Cliente Válido
03 - Login do cliente
04 - Admin Cria Cozinha
05 - Criar Pedido sem Token
06 - Erro Produto Falso
07 - Erro Falta Estoque
08 - Criar Pedido Sucesso
09 - Pagamento Recusado
10 - Pagamento Aprovado
11 - Cliente muda status
12 - Login do func. cozinha
13 - Cozinha muda status
```

A requisição `03 - Login do cliente` salva automaticamente o token JWT do cliente na variável:

```text
token_cliente
```

A requisição `12 - Login do func. cozinha` salva automaticamente o token JWT do funcionário da cozinha na variável:

```text
token_cozinha
```

Dessa forma, não é necessário copiar manualmente os tokens para cada requisição, desde que o ambiente correto esteja selecionado no Postman.

### 5. Rotas auxiliares da coleção

A coleção também possui pastas auxiliares, como:

```text
Produtos
Unidades
```

Essas requisições servem como apoio para consulta de dados e validação complementar da API, mas os cenários principais do plano de testes estão organizados pelas requisições numeradas de `01` a `13`.

## Observação sobre autenticação no Postman

Para garantir a reprodutibilidade da coleção, recomenda-se que as requisições protegidas utilizem variáveis de ambiente no campo de autenticação Bearer Token.

Exemplo:

```text
Bearer {{token_adm}}
Bearer {{token_cliente}}
Bearer {{token_cozinha}}
```

Assim, os tokens gerados pelas requisições de login serão utilizados automaticamente durante a execução dos testes.

## Fluxo principal validado

O MVP implementado valida o fluxo principal:

```text
Pedido → Pagamento mock → Atualização de status
```

Esse fluxo contempla:

* criação de usuário;
* autenticação JWT;
* controle de acesso por perfis;
* criação de pedido;
* validação de produto;
* validação de estoque;
* registro de pagamento mock;
* tratamento de pagamento aprovado e recusado;
* atualização de status do pedido;
* validação de acesso não autorizado;
* respostas de erro padronizadas.

## Observações finais

O projeto foi desenvolvido para fins acadêmicos, com execução local e persistência em banco PostgreSQL via Docker. A documentação da API pode ser consultada pelo Swagger e os testes funcionais podem ser reproduzidos pela coleção Postman disponibilizada no repositório.
