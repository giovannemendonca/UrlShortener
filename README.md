# **Serviço de Encurtamento de URLs**

Bem-vindo ao serviço de encurtamento de URLs! Este projeto oferece uma forma simples de gerar e gerenciar URLs encurtadas usando AWS Lambda e S3.

---

## **Como Funciona**

### 1. **Criar Encurtador de URL**
Esta função Lambda gera uma URL curta para uma URL original fornecida e armazena seus metadados (URL original e tempo de expiração) em um bucket do Amazon S3.

- **Entrada**:
  Um objeto JSON com:
    - `originalUrl` (String): A URL original a ser encurtada.
    - `expirationTime` (Número): O tempo de expiração em segundos desde a época Unix.

- **Saída**:
  Um objeto JSON com:
    - `code` (String): O código único da URL encurtada.
    - `shortenedUrl` (String): A URL curta completa.

---

### 2. **Redirecionar URL Encurtada**
Esta função Lambda redireciona os usuários para a URL original quando acessam a URL encurtada. Ela recupera os metadados do bucket do S3 e valida o tempo de expiração.

- **Entrada**:
  O código único como parte do caminho da requisição HTTP.

- **Saída**:
    - **302 (Found)**: Redireciona para a URL original.
    - **410 (Gone)**: Indica que a URL expirou.
    - **404 (Not Found)**: Indica que a URL não existe.

---

## **Estrutura do Repositório**

```plaintext
src/
├── createUrlShortener/               # Código para criar URLs encurtadas
│   ├── Main.java                     # Função Lambda principal
│   ├── dto/
│   │   └── UrlData.java              # Estrutura de dados para metadados da URL
└── redirectUrlShortener/             # Código para redirecionar URLs
    ├── Main.java                     # Função Lambda principal
    ├── dto/
    │   └── UrlData.java              # Estrutura de dados para metadados da URL
    ├── exceptions/                   # Exceções customizadas para tratamento de erros
    │   ├── InvalidDataException.java
    │   └── ResourceNotFoundException.java
    ├── utils/
        └── ResponseUtil.java         # Classe utilitária para gerar respostas


```


## **Tecnologias Utilizadas**

Este projeto utiliza uma série de tecnologias da AWS e outras ferramentas para fornecer a funcionalidade de encurtamento de URLs:

- **AWS Lambda**: Para criação de funções serverless que executam as tarefas de encurtamento e redirecionamento de URLs.
- **Amazon S3**: Para armazenar os metadados das URLs encurtadas, incluindo o URL original e o tempo de expiração.
- **Amazon API Gateway**: Para criar a interface de API que permite acessar as funções Lambda.
- **Java 17**: Linguagem de programação utilizada para implementar as funções Lambda.
- **Jackson**: Biblioteca para manipulação de objetos JSON em Java.
- **AWS SDK para Java**: Utilizado para interagir com o Amazon S3 e outros serviços AWS.




