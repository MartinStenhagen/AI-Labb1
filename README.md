# AI-Labb1

AI-Labb1 är en Spring Boot-baserad AI-integration som kommunicerar med språkmodeller via OpenRouter.

Projektet innehåller:

* REST API för AI-chat
* eget webbgränssnitt byggt med JTE
* stöd för olika AI-personligheter
* sessionsbaserad chat history
* retry/backoff och robust felhantering
* Swagger/OpenAPI-dokumentation
* WireMock-baserade integrationstester
* Docker-stöd

---

# Funktioner

## AI-chat

Användaren kan skicka meddelanden till en AI-modell via:

* REST API
* webbgränssnitt

Svar genereras via OpenRouter API.

---

## Personligheter

Applikationen stödjer flera personligheter:

| Personality | Beskrivning             |
| ----------- | ----------------------- |
| helper      | Hjälpsam och pedagogisk |
| coder       | Programmeringslärare    |
| pirate      | Svarar i piratstil      |

---

## Chat history

Varje session får ett unikt `sessionId`.

Tidigare meddelanden sparas i minnet så att modellen får kontext mellan requests.

---

# Felhantering

Projektet implementerar flera resilience-mekanismer:

* Retry
* Exponential backoff
* Jitter
* Timeout-hantering
* Concurrency limiting
* Global exception handling

Retry används endast för transienta fel:

* 429 Too Many Requests
* 502 Bad Gateway
* 503 Service Unavailable
* 504 Gateway Timeout
* nätverksfel/timeouts

Projektet tar även hänsyn till failure patterns i distribuerade system såsom:

* Retry Storm
* Thundering Herd
* Cascading Failures
* Latency Spikes

Motåtgärder:

* exponential backoff
* jitter
* transient error classification
* concurrency limiting

---

# Arkitektur

| Klass                    | Ansvar                                 |
| ------------------------ | -------------------------------------- |
| `ChatController`         | REST-endpoint                          |
| `ChatService`            | Orkestrerar chatflödet                 |
| `OpenRouterClient`       | Kommunikation med OpenRouter           |
| `PersonalityService`     | Hanterar system prompts/personligheter |
| `ChatMemoryService`      | Sessionshistorik                       |
| `GlobalExceptionHandler` | API-felhantering                       |

---

# API

## POST `/api/v1/chat`

### Request

```json
{
  "personality": "coder",
  "message": "skriv hello world i java",
  "sessionId": "abc123"
}
```

### Response

```json
{
  "answer": "public class HelloWorld { ... }",
  "sessionId": "abc123"
}
```

---

# Swagger/OpenAPI

Swagger UI finns på:

```text
http://localhost:8080/swagger-ui.html
```

---

# Webbgränssnitt

Chat UI finns på:

```text
http://localhost:8080/chat
```

---

# Konfiguration

## Miljövariabler

Projektet använder miljövariabler för känslig information.

### Exempel

```env
OPENROUTER_API_KEY=din-api-nyckel
```

---

## application.properties

```properties
ai.base-url=https://openrouter.ai/api/v1
ai.api-key=${OPENROUTER_API_KEY}
ai.model=z-ai/glm-4.5-air:free
```

---

# Köra projektet lokalt

## 1. Klona projektet

```bash
git clone <repo-url>
```

---

## 2. Sätt miljövariabel

### Windows PowerShell

```powershell
$env:OPENROUTER_API_KEY="din-nyckel"
```

### Linux/macOS

```bash
export OPENROUTER_API_KEY="din-nyckel"
```

---

## 3. Starta projektet

```bash
mvn spring-boot:run
```

---

# Docker

## Build image

```bash
docker build -t ai-labb1 .
```

## Run container

### Windows CMD

```bash
docker run -p 8080:8080 ^
-e OPENROUTER_API_KEY=din-nyckel ^
ai-labb1
```

### Linux/macOS

```bash
docker run -p 8080:8080 \
-e OPENROUTER_API_KEY=din-nyckel \
ai-labb1
```

---

# Testning

Projektet innehåller:

| Testtyp           | Beskrivning   |
| ----------------- | ------------- |
| Unit tests        | Service-logik |
| Controller tests  | MockMvc       |
| Integration tests | WireMock      |

Testerna verifierar bland annat:

* retry-beteende
* API-fel
* session memory
* personality prompts
* transient felhantering

---