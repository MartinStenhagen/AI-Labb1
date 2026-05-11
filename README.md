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

Projektet använder flera resilience-mekanismer via Resilience4j för att hantera fel mot externa AI-tjänster.

Implementerade mekanismer:

* Retry
* Exponential backoff
* Jitter
* Circuit Breaker
* Timeout-hantering
* Concurrency limiting
* Fallback responses
* Global exception handling

Retry används endast för transienta fel såsom:

* 429 Too Many Requests
* 502 Bad Gateway
* 503 Service Unavailable
* 504 Gateway Timeout
* nätverksfel/timeouts

Retry används INTE för permanenta klientfel såsom:

* 400 Bad Request
* 401 Unauthorized
* 403 Forbidden

Vid upprepade temporära fel aktiveras fallback-logik som returnerar ett kontrollerat felmeddelande istället för att krascha applikationen.

Projektet tar även hänsyn till failure patterns i distribuerade system såsom:

* Retry Storm
* Thundering Herd
* Cascading Failures
* Latency Spikes

Motåtgärder:

* exponential backoff
* jitter
* transient error classification
* circuit breaker
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
| `OpenRouterClient`       | Kommunikation med OpenRouter + resilience/fallback |
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

# Konfiguration

## OpenRouter API-nyckel

Projektet kräver en giltig OpenRouter API-nyckel för att kunna startas och använda AI-funktionerna.

Skapa en API-nyckel via:

https://openrouter.ai/

---

## application.properties

```properties
ai.base-url=https://openrouter.ai/api/v1
ai.api-key=${OPENROUTER_API_KEY}
ai.model=z-ai/glm-4.5-air:free
```

---

## Viktigt

Projektet läser INTE automatiskt `.env`-filer.

Miljövariabeln måste därför sättas manuellt i terminalen eller i IntelliJ innan applikationen startas.

Om API-nyckeln saknas kommer applikationen inte kunna anropa OpenRouter.

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
cd AI-Labb1
```

---

## 2. Ange OpenRouter API-nyckel

### Windows PowerShell

```powershell
$env:OPENROUTER_API_KEY="din-api-nyckel"
```

### Windows CMD

```cmd
set OPENROUTER_API_KEY=din-api-nyckel
```

### Linux/macOS

```bash
export OPENROUTER_API_KEY="din-api-nyckel"
```

---

## 3. Verifiera miljövariabeln (valfritt)

### PowerShell

```powershell
echo $env:OPENROUTER_API_KEY
```

### CMD

```cmd
echo %OPENROUTER_API_KEY%
```

### Linux/macOS

```bash
echo $OPENROUTER_API_KEY
```



# IntelliJ-konfiguration

Om projektet körs via IntelliJ behöver miljövariabeln sättas i Run Configuration.

## IntelliJ

1. Open Run Configuration
2. Välj applikationen
3. Gå till "Environment Variables"
4. Lägg till:

```text
OPENROUTER_API_KEY=din-api-nyckel
```

5. Starta applikationen

## 4. Starta applikationen

```bash
mvn spring-boot:run
```

---

## 5. Öppna applikationen

### Webbgränssnitt

```text
http://localhost:8080/chat
```

### Swagger/OpenAPI

```text
http://localhost:8080/swagger-ui.html
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
* retry + fallback
* circuit breaker
* transient vs permanenta fel
* API-fel
* session memory
* personality prompts
* transient felhantering

---