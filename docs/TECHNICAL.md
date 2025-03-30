# Documentação Técnica - HubSpot Integration

## Visão Geral

Este projeto é uma aplicação Spring Boot que integra com a API do HubSpot para criar contatos e processar eventos via webhooks utilizando autenticação OAuth.

## Decisões Técnicas

### Arquitetura e Estrutura
- **Spring Boot**: Escolhido como framework principal por sua facilidade de configuração, suporte nativo a REST e pelo maior domínio da ferramenta comparada a outra opção possível.
- **Clean Architecture**: Adotada para organizar o código em camadas (ex.: controladores, casauos de uso, gateways), separando responsabilidades e dependências facilitando a substituição de componentes e a evolução do projeto. 

### Autenticação OAuth
- **Implementação**: O fluxo OAuth foi implementado com OkHttp para chamadas HTTP e armazenamento em memória do token pela sua simplicidade inicial, atendendo perfeitamente a proposta desejada.
- **Redirect URI Local**: Usado `http://localhost:8080/oauth/callback` como padrão para facilitar os testes locais.

## Controle de Limites de Requisições
- **Implementação**: Limite por segundo definido conforme a restrição do hubspot e limite diário escolhido com base nos planos existentes.
- - **Bucket4j**: Biblioteca escolhida para implementar rate limiting local (11 requisições/segundo e 25.000/dia) por atender perfeitamente a necessidade e ser integrada ao spring.

### Integração com HubSpot
- **OkHttp**: Usado como cliente HTTP para chamadas à API do HubSpot pela sua simplicidade e por atender a necessidade inicial.
- **Try-with-Resources**: Aplicado em chamadas HTTP por garantir o fechamento de recursos reduzindo código e riscos de vazamento.

### Webhooks
- **Endpoint Simples**: `/webhook` apenas imprime eventos no console para validação do recebimento por ser uma boa prova de conceito inicial.

## Motivação para Uso das Bibliotecas

- **Spring Boot**: Framework padrão para aplicações Java modernas, reduzindo complexidade e facilitando o desenvolvimento.
- **OkHttp**: Cliente HTTP leve, ideal para chamadas REST sem sobrecarga de frameworks maiores.
- **Bucket4j**: Solução específica para o rate limiting, mais simples que outras alternativas para este caso.
- **org.json**: Biblioteca simples e leve para manipulação de JSON.
- **Lombok**: Reduz boilerplate em modelos (ex.: getters/setters) deixando o código mais limpo e legível.
- 
## Possíveis Melhorias Futuras

1. **Persistência de Tokens**:
    - **Problema**: Tokens OAuth são armazenados em memória e perdidos ao reiniciar.
    - **Solução**: Implementar `TokenStorageGateway` com banco de dados para persistência.

2. **Processamento de Webhooks**:
    - **Problema**: Eventos são apenas impressos.
    - **Solução**: Criar um serviço mais robusto para salvar os eventos em banco ou disparar ações (ex.: notificar usuários).

3. **Rate Limiting Dinâmico**:
    - **Problema**: Limites fixos podem não refletir os planos do HubSpot.
    - **Solução**: Ajustar `Bucket4j` dinamicamente com base em seus headers (`X-HubSpot-RateLimit-*`).

4. **Testes Automatizados**:
    - **Problema**: Apenas testes manuais.
    - **Solução**: Adicionar testes unitários para uma cobertura maior.

5. **Deploy em Produção**:
    - **Problema**: Configurado para ambiente local (`localhost`).
    - **Solução**: Hospedar em um servidor, o que também removeria a necessidade do uso do ngrok.

6. **Bucket por Usuário**:
    - **Problema**: Limites de requisições são globais, afetando todos os usuários igualmente.
    - **Solução**: Configurar `Bucket4j` para criar buckets por usuário (ex.: baseado em token ou ID), permitindo limites individualizados.
    - 
7. **Gerenciamento de Tokens por Cliente**:
    - **Problema**: Token único na API não suporta múltiplos usuários ou clientes distintos.
    - **Solução**: Associar tokens a clientes específicos, armazenando-os em um serviço seguro (ex.: banco de dados) e usando-os apenas para o cliente correspondente.