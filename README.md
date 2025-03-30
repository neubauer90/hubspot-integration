# HubSpot Integration

Este projeto é uma aplicação Spring Boot que integra com a API do HubSpot para criar contatos via requisições HTTP e processar eventos de webhooks. Ele implementa autenticação OAuth, controle de limites de requisições e tratamento centralizado de exceções. O objetivo é fornecer uma interface REST simples para interagir com o HubSpot enquanto respeita os limites da API.

## Funcionalidades
- **Criação de Contatos:** Endpoint POST `/contacts` para criar contatos no HubSpot.
- **Autenticação OAuth:** Fluxo de autorização para obter tokens de acesso.
- **Rate Limiting:** Limites locais de 11 requisições por segundo e 25.000 por dia.
- **Tratamento de Erros:** Respostas HTTP consistentes para limites excedidos e erros da API.

## Pré-requisitos
Antes de executar a aplicação, é necessário ter os seguintes itens instalados:

- **Java Development Kit (JDK) 17 ou superior**
    - Verifique com: `java -version`
    - Baixe em: https://www.oracle.com/java/technologies/javase-jdk17-downloads.html
    - Ou instale via `sdkman`: `sdk install java 17.0.10-tem`

- **Maven**
    - Verifique com: `mvn -version`
    - Baixe em: https://maven.apache.org/download.cgi
    - Ou instale via gerenciador: `brew install maven` (macOS) ou `sudo apt install maven` (Ubuntu)

- **Git**
    - Verifique com: `git --version`
    - Baixe em: https://git-scm.com/downloads

- **Conta de Desenvolvedor no HubSpot**
    - Crie uma conta em https://developers.hubspot.com para obter `client-id`, `client-secret`, adicionar escopos e configurar webhooks.

- **Ferramentas Adicionais**
    - **cURL**: Para testes manuais (`sudo apt install curl`).
    - **Postman**: Para testes via interface gráfica.
    - **Ngrok**: Para expor webhooks localmente (baixe em https://ngrok.com/download).

## Configuração Inicial

### 1. Clonar o Repositório
Clone o projeto do GitHub para sua máquina local:

```bash
git clone https://github.com/neubauer90/hubspot-integration.git
cd hubspot-integration
```
### 2. Configurar Credenciais do HubSpot
A aplicação usa autenticação OAuth e requer credenciais do HubSpot.

- **Crie um aplicativo no HubSpot:**
    - Acesse https://developers.hubspot.com e faça login
    - Vá para "Apps" > "Create App" e o preencha com um nome de sua escolha.
    - Na aba "Auth":
        - Anote o `Client ID`
        - Anote o `Client Secret`
        - Adicione os escopos: `crm.objects.contacts.write`, `crm.objects.contacts.read` e `oauth`
        - Defina o `Redirect URI` para: `http://localhost:8080/oauth/callback`
    - Salve o aplicativo

- **Configure o `application.properties`:**
  Em `src/main/resources/application.properties` substitua `<SEU_CLIENT_ID>` e `<SEU_CLIENT_SECRET>` pelos valores anotados no passo anterior.

## Autenticação OAuth

Para interagir com o HubSpot, a aplicação precisa de um token OAuth.

- **Inicie a aplicação:**
    - Compile e execute a partir da raíz do projeto:
      ```bash
      mvn clean install
      mvn spring-boot:run
      ```
- Está disponível em: http://localhost:8080


- **Obtenha a URL de autorização:**
- Acesse no navegador: http://localhost:8080/oauth/authorize
- Você será redirecionado ao HubSpot para login
- Após o login será exibida uma tela dizendo que o seu aplicativo será conectado ao Hubspot
- Escolhe uma conta de teste do desenvolvedor criada e clique em `Escolher conta` para autorizar o aplicativo
- Se bem-sucedido será redirecionado para: `http://localhost:8080/oauth/callback?code=<CODIGO>` e verá na tela a seguinte mensagem: `Autenticação realizada com sucesso`

* Obs: A partir daí o token ficará em memória para uso nas requisições.

## Executando a Aplicação

Com a autenticação pronta, é possível utilizar a aplicação.

- **Teste manual com cURL:**
    - Crie um contato através do terminal com o seguinte comando:
      ```bash
      curl -X POST "http://localhost:8080/contacts" \
      -H "Content-Type: application/json" \
      -d '{"firstname": "Mateus", "lastname": "Neubauer", "email": "mateus.neubauer@gmail.com"}'
Resposta esperada: `Contato criado com sucesso`

## Configurando Webhooks

O endpoint `/webhook` processa eventos `contact.creation` e imprime no console.

- **Crie uma URL com Ngrok:**
- Crie uma conta em https://dashboard.ngrok.com/signup
- Instale e adquira seu authtoken em https://dashboard.ngrok.com/get-started/your-authtoken
- Após isso, se autentique com o comando (substituindo `$YOUR_AUTHTOKEN` pelo token adquirido no passo anterior
- ```bash 
  ngrok config add-authtoken $YOUR_AUTHTOKEN
  ```
- Execute para criar o túnel:
- ```bash 
  ngrok http 8080
  ```
- Em `Forwarding` será exibida a URL gerada.
-
- **Configure no HubSpot:**
    - Acesse https://developers.hubspot.com e abra seu aplicativo
    - Na aba "Webhooks":
        - Adicione uma URL de destino: A URL gerada no passo anterior adicionando `/webhook` ao final. Exemplo: `https://abc123.ngrok-free.app/webhook`
        - Clique em `Criar Assinatura` e selecione o tipo de objeto `Contato` e o evento `Criado` para que a monitoração possa ser feita com sucesso.
        - Selecione a assinatura recém criada e clique em `Ativar'.
    - Crie um contato e verifique no console o funcionamento do endpoint com a resposta esperada: Resposta esperada: `Recebido evento de criação de contato: {detalhes do evento}`

## Resolução de Problemas

- **Erro 500 "Token de autenticação não disponível":**
    - Reexecute o fluxo OAuth:
      http://localhost:8080/oauth/authorize


- **Erro 429 "Limite excedido":**
- Por segundo: Espere 1 segundo
- Diário: Tente novamente no dia seguinte


- **Webhook não recebido:**
- Verifique a URL configurada no HubSpot
- Confirme que o Ngrok está ativo e a porta 8080 está acessível


- **Aplicação não inicia:**
- Verifique: `java -version` e `mvn -version`
- Logs: `mvn spring-boot:run -X`