# Catálogo de Jogos — Arquitetura de Microsserviços e Padrões de Integração

Este projeto consiste na evolução arquitetural da aplicação **`catalogo-jogos`** (originalmente uma API monolítica em Java 21 e Spring Boot 4), dividida em serviços independentes com a aplicação de três padrões clássicos de integração e composição de software: **Proxy**, **Adapter** e **Aggregator**.

---

## 🏛️ 1. Visão Geral da Arquitetura

A solução adota uma arquitetura orientada a serviços onde os clientes externos interagem exclusivamente com um ponto de entrada centralizado (**Gateway** na porta `8080`), permanecendo isolados da topologia de rede e das portas dos serviços de backend internos.

```mermaid
flowchart TD
    Client(["Cliente Externo\n(Postman / cURL / Frontend)"])

    subgraph Entrada ["Ponto de Entrada Principal (Porta 8080)"]
        GW["Gateway Service"]
        Proxy["Padrão PROXY\n(/game)"]
        Aggregator["Padrão AGGREGATOR\n(/game/{id}/completo)"]
        Adapter["Padrão ADAPTER\n(LojaExternaAdapter)"]
    end

    subgraph Backend ["Serviços Internos"]
        ServA["Serviço A: Catálogo de Jogos\n(Porta 8081)\n[H2: catalogodb]"]
        ServB["Serviço B: Avaliações\n(Porta 8082)\n[H2: avaliacoesdb]"]
        ServC["Serviço C: Loja / Sistema Legado\n(Porta 8083)\n[H2: lojadb]"]
    end

    Client -->|CRUD /game| Proxy
    Client -->|GET /game/{id}/completo| Aggregator

    Proxy -->|Encaminhamento Transparente| ServA
    Aggregator -->|1. Consulta Dados do Jogo| ServA
    Aggregator -->|2. Consulta Notas e Resenhas| ServB
    Aggregator -->|3. Consulta Oferta Canônica| Adapter
    Adapter -->|Conversão de Protocolo/Dados Legados| ServC
```

---

## 🚪 2. Organização das Portas e Serviços

Conforme a referência arquitetural estabelecida:

| Componente | Porta | Papel Arquitetural e de Negócio | Banco de Dados |
| :--- | :---: | :--- | :--- |
| **`gateway-service`** | `8080` | **Aplicação / Entrada Principal:** Centraliza o acesso, hospeda o **Proxy** (CRUD transparente), o **Aggregator** (visão 360° do jogo) e o **Adapter** (tradução de dados legados). | Não possui (Stateless) |
| **`servico-a-catalogo`** | `8081` | **Serviço A (Catálogo de Jogos):** Responsável pelo ciclo de vida do jogo (CRUD: título, gênero, plataforma, desenvolvedora, nota base). | H2 (`jdbc:h2:mem:catalogodb`) |
| **`servico-b-avaliacoes`** | `8082` | **Serviço B (Avaliações e Reviews):** Gerencia notas dos usuários, comentários e calcula médias consolidadas por jogo. | H2 (`jdbc:h2:mem:avaliacoesdb`) |
| **`servico-c-loja`** | `8083` | **Serviço C (Loja Externa / Legada):** Simula uma distribuidora ou parceiro externo com formato não padronizado (valores em centavos, chaves em *snake_case*). | H2 (`jdbc:h2:mem:lojadb`) |

---

## 🧩 3. Padrões de Integração Implementados

### 3.1. Padrão Proxy (`gateway-service/proxy`)
- **Objetivo:** Atuar como intermediário transparente entre o cliente externo e o **Serviço A** (porta `8081`).
- **Como funciona:**
  - O cliente envia requisições para `http://localhost:8080/game`.
  - O `GameProxyService` intercepta a requisição e a repassa para `http://localhost:8081/game`.
  - **Ocultamento:** O cliente não precisa conhecer o endereço ou a porta do Serviço A.
  - **Tratamento de Indisponibilidade:** Caso o Serviço A esteja fora do ar, o Proxy intercepta a falha de conexão (`ResourceAccessException`) e retorna status **`503 Service Unavailable`** com resposta estruturada no padrão RFC 7807 (`ProblemDetail`).

### 3.2. Padrão Adapter (`gateway-service/adapter`)
- **Objetivo:** Integrar o ecossistema a uma interface ou estrutura de dados incompatível (Serviço C).
- **Problema resolvido:**
  - O **Serviço C** (Loja Parceira) retorna uma estrutura legada com valores monetários em centavos e nomenclatura heterogênea:
    ```json
    {
      "game_identifier": 1,
      "external_sku": "SKU-TW3-001",
      "vendor_name": "Steam Partner Store",
      "price_amount_cents": 12990,
      "iso_currency": "BRL",
      "inventory_status": "AVAILABLE",
      "deeplink": "https://store.steampowered.com/app/292030"
    }
    ```
- **Solução com Adapter:**
  - Criada a interface de domínio (`Target`): `OfertaLojaPort`.
  - Implementado o `LojaExternaAdapter` que:
    1. Consome a API do Serviço C (`Adaptee`).
    2. Converte centavos (`price_amount_cents = 12990`) para reais (`preco = 129.90`).
    3. Converte a string de estoque (`inventory_status = "AVAILABLE"`) em booleano (`emEstoque = true`).
    4. Mapeia campos para o modelo canônico (`OfertaLojaDTO`).
    5. Isola o domínio da aplicação da implementação do fornecedor externo.

### 3.3. Padrão Aggregator (`gateway-service/aggregator`)
- **Objetivo:** Compor uma resposta unificada a partir de dados dispersos em múltiplos microsserviços.
- **Como funciona:**
  - Endpoint: `GET http://localhost:8080/game/{id}/completo`.
  - O `GameAggregatorService`:
    1. Consulta o **Serviço A** para obter os dados básicos do jogo.
    2. Consulta o **Serviço B** para obter as avaliações e média das notas da comunidade.
    3. Consulta o **Serviço C** (através do **Adapter**) para obter preço e disponibilidade.
    4. Consolida todas as informações em um único objeto `GameCompletoDTO`.
  - **Resiliência e Degradação Graciosa:** Se um serviço secundário (Avaliações ou Loja) falhar ou estiver temporariamente fora do ar, o agregador **não quebra a requisição**; ele entrega os dados do jogo com um indicativo claro no campo `statusIntegracao`.

---

## 🛠️ 4. Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 4.1.x**
  - `spring-boot-starter-webmvc`: Exposição de APIs REST e clientes HTTP.
  - `spring-boot-starter-data-jpa`: Camada de persistência.
  - `spring-boot-h2console` & `com.h2database:h2`: Bancos relacionais em memória isolados por serviço.
- **Spring RestClient:** Comunicação HTTP síncrona moderna entre os serviços.
- **Apache Maven:** Projeto multi-módulo (`catalogo-parent`).
- **JUnit 5 & Mockito:** Testes automatizados unitários e de integração.

---

## 📁 5. Estrutura do Projeto

```
catalogo-jogos/
├── pom.xml                          # POM raiz agregador multi-módulo
├── mvnw / mvnw.cmd                  # Maven Wrapper
├── start-all.sh                     # Script para iniciar todos os 4 serviços
├── stop-all.sh                      # Script para finalizar todos os serviços
│
├── gateway-service/                 # Porta 8080 (Proxy, Aggregator, Adapter)
│   ├── src/main/java/.../gateway/
│   │   ├── adapter/                 # Implementação do padrão Adapter
│   │   ├── aggregator/              # Implementação do padrão Aggregator
│   │   ├── proxy/                   # Implementação do padrão Proxy
│   │   └── config/                  # Configuração do RestClient
│   └── src/test/java/...            # Testes automatizados (Adapter, Proxy, Aggregator)
│
├── servico-a-catalogo/              # Porta 8081 (Serviço A: Catálogo de Jogos)
│   ├── src/main/java/.../catalogojogos/
│   │   ├── controller/              # GameController (/game)
│   │   ├── service/                 # GameService (regras de negócio)
│   │   ├── repository/              # GameRepository (JPA)
│   │   └── entity/                  # Game
│   └── src/test/java/...            # Testes do Serviço A
│
├── servico-b-avaliacoes/            # Porta 8082 (Serviço B: Avaliações)
│   ├── src/main/java/.../avaliacoes/
│   │   ├── controller/              # AvaliacaoController (/avaliacoes)
│   │   ├── service/                 # AvaliacaoService (cálculo de média/resumo)
│   │   ├── repository/              # AvaliacaoRepository
│   │   └── entity/                  # Avaliacao
│   └── src/test/java/...            # Testes do Serviço B
│
└── servico-c-loja/                  # Porta 8083 (Serviço C: Loja / Legado)
    ├── src/main/java/.../loja/
    │   ├── controller/              # LegacyStoreController (/api/v1/legacy-store)
    │   └── entity/                  # LegacyPricingItem
    └── src/test/java/...            # Testes do Serviço C
```

---

## 🚀 6. Como Executar o Projeto

### Pré-requisitos
- **Java 21** instalado (`java -version`).
- Terminal Linux / macOS ou Git Bash no Windows.

### Opção 1: Inicialização Automática (Recomendada)

1. No diretório raiz do projeto, compile e gere os pacotes:
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. Execute o script de inicialização:
   ```bash
   ./start-all.sh
   ```
   *O script iniciará os 4 serviços em background e salvará os logs na pasta `logs/`.*

3. Execute a demonstração interativa automatizada:
   ```bash
   ./demo.sh
   ```
   *Ou utilize o arquivo [requests.http](requests.http) diretamente no IntelliJ IDEA ou VS Code.*

4. Para encerrar todos os serviços:
   ```bash
   ./stop-all.sh
   ```

---

### Opção 2: Inicialização Manual (Terminal a Terminal)

Abra 4 abas ou terminais diferentes e execute cada serviço:

- **Terminal 1 — Serviço A (Porta 8081):**
  ```bash
  cd servico-a-catalogo
  ../mvnw spring-boot:run
  ```

- **Terminal 2 — Serviço B (Porta 8082):**
  ```bash
  cd servico-b-avaliacoes
  ../mvnw spring-boot:run
  ```

- **Terminal 3 — Serviço C (Porta 8083):**
  ```bash
  cd servico-c-loja
  ../mvnw spring-boot:run
  ```

- **Terminal 4 — Gateway (Porta 8080):**
  ```bash
  cd gateway-service
  ../mvnw spring-boot:run
  ```

---

## 🧪 7. Execução dos Testes Automatizados

O projeto conta com testes unitários e de integração cobrindo os serviços, controllers, o Proxy, o Adapter e o Aggregator.

Para rodar todos os testes de todos os módulos:
```bash
./mvnw test
```

---

## 📡 8. Guia de Demonstração e Exemplos de Requisições

> **Importante:** Todas as requisições do cliente são feitas na **porta 8080** (ponto de entrada unificado). O sistema já inicia com jogos, avaliações e preços pré-carregados (IDs 1, 2 e 3).

### 8.1. Demonstração do Padrão PROXY (CRUD Transparente)

#### A. Listar todos os jogos (encaminhado ao Serviço A)
```bash
curl -X GET http://localhost:8080/game
```
**Resposta esperada (`200 OK`):**
```json
[
  {
    "id": 1,
    "titulo": "The Witcher 3: Wild Hunt",
    "genero": "RPG",
    "plataforma": "PC / PS5 / Xbox Series",
    "desenvolvedora": "CD Projekt Red",
    "nota": 9.8
  },
  {
    "id": 2,
    "titulo": "Elden Ring",
    "genero": "Action RPG",
    "plataforma": "PC / PS5 / Xbox Series",
    "desenvolvedora": "FromSoftware",
    "nota": 9.6
  }
]
```

#### B. Buscar jogo por ID
```bash
curl -X GET http://localhost:8080/game/1
```

#### C. Adicionar novo jogo
```bash
curl -X POST http://localhost:8080/game \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "Cyberpunk 2077",
    "genero": "Action RPG",
    "plataforma": "PC / Consoles",
    "desenvolvedora": "CD Projekt Red",
    "nota": 8.9
  }'
```

#### D. Atualizar jogo existente
```bash
curl -X PUT http://localhost:8080/game/1 \
  -H "Content-Type: application/json" \
  -d '{
    "titulo": "The Witcher 3: Wild Hunt (Complete Edition)",
    "genero": "RPG",
    "plataforma": "PC / PS5 / Xbox Series",
    "desenvolvedora": "CD Projekt Red",
    "nota": 9.9
  }'
```

#### E. Deletar jogo
```bash
curl -X DELETE http://localhost:8080/game/1
```

---

### 8.2. Demonstração do Padrão ADAPTER (Tradução de Dados Legados)

Para evidenciar a função do Adapter, observe a diferença entre o formato bruto do Serviço C e o formato adaptado pelo Gateway:

- **Formato Bruto do Serviço C (Porta 8083):**
  ```bash
  curl -X GET http://localhost:8083/api/v1/legacy-store/pricing/1
  ```
  *Retorno bruto:*
  ```json
  {
    "game_identifier": 1,
    "external_sku": "SKU-TW3-001",
    "vendor_name": "Steam Partner Store",
    "price_amount_cents": 12990,
    "iso_currency": "BRL",
    "inventory_status": "AVAILABLE",
    "deeplink": "https://store.steampowered.com/app/292030"
  }
  ```

- **Formato Canônico Adaptado pelo `LojaExternaAdapter`:**
  O Adapter converte automaticamente:
  - `price_amount_cents: 12990` $\rightarrow$ `preco: 129.90` (divisão por 100)
  - `inventory_status: "AVAILABLE"` $\rightarrow$ `emEstoque: true`
  - `vendor_name` $\rightarrow$ `loja`
  - `deeplink` $\rightarrow$ `urlLoja`

---

### 8.3. Demonstração do Padrão AGGREGATOR (Composição de Serviços)

#### Consulta Agregada Completa (Visão 360°)
```bash
curl -X GET http://localhost:8080/game/1/completo
```

**Resposta esperada (`200 OK`):**
```json
{
  "id": 1,
  "titulo": "The Witcher 3: Wild Hunt",
  "genero": "RPG",
  "plataforma": "PC / PS5 / Xbox Series",
  "desenvolvedora": "CD Projekt Red",
  "notaOriginal": 9.8,
  "avaliacoes": {
    "gameId": 1,
    "mediaNota": 9.8,
    "totalAvaliacoes": 2,
    "comentarios": [
      "Obra de arte impecável! Trilha sonora e narrativa incríveis.",
      "Excelente combate e missões secundárias profundas."
    ]
  },
  "melhorOferta": {
    "loja": "Steam Partner Store",
    "preco": 129.90,
    "moeda": "BRL",
    "emEstoque": true,
    "urlLoja": "https://store.steampowered.com/app/292030"
  },
  "statusIntegracao": "INTEGRAÇÃO_COMPLETA_COM_SUCESSO"
}
```

---

### 8.4. Demonstração de Resiliência e Degradação Graciosa

1. Pare o **Serviço B** (`servico-b-avaliacoes` na porta `8082`):
   ```bash
   kill $(cat logs/servico-b.pid)
   ```
2. Faça novamente a requisição ao Aggregator na porta `8080`:
   ```bash
   curl -X GET http://localhost:8080/game/1/completo
   ```
3. **Resultado:** A requisição ainda tem sucesso (`200 OK`), entregando os dados do jogo e a oferta da loja, com o campo `statusIntegracao` informando:
   ```json
   "statusIntegracao": "INTEGRAÇÃO_PARCIAL: Serviço de avaliações temporariamente indisponível ou sem dados"
   ```

---

## 🏛️ 9. Conclusão e Critérios Atendidos

- [x] Aplicação decomposta em 3 serviços de negócio independentes (`servico-a-catalogo`, `servico-b-avaliacoes`, `servico-c-loja`).
- [x] Ponto de entrada centralizado (`gateway-service`) na porta `8080`.
- [x] Portas padronizadas: `8080`, `8081`, `8082`, `8083`.
- [x] Padrão **Proxy** implementado para rotear operações CRUD e ocultar serviços internos.
- [x] Padrão **Adapter** implementado para integrar dados heterogêneos/legados da loja.
- [x] Padrão **Aggregator** implementado para consolidar dados de múltiplos microsserviços com resiliência.
- [x] Operações CRUD originais preservadas e em pleno funcionamento.
- [x] 33 testes automatizados garantindo a integridade dos padrões e das regras de negócio.
