# ai-insight-service

A production-style Spring Boot module that integrates with the Anthropic API to generate structured insights (summary, tags, sentiment) from free-text input. Built as a focused example of **resilient, cost-aware LLM integration** in a Java backend — not a toy "call the API and print the result" demo.

## Why this project exists

Most tutorials show how to call an LLM API. Very few show what it takes to call one **safely in a real system**: what happens when the AI service is slow, rate-limited, or down? How do you avoid paying for — and waiting on — the same request twice? How do you keep a single-responsibility architecture when a third-party dependency is involved?

This project answers those questions with working code.

## Architecture

```
Client (iOS / Postman)
        │
        ▼
ContentInsightController   → validates input (@Valid), thin HTTP layer
        │
        ▼
InsightService              → hashes content, checks MongoDB cache, orchestrates
        │
        ├─── cache hit ───► MongoDB (insights collection, indexed by SHA-256 hash)
        │
        └─── cache miss
                │
                ▼
        LlmResilientClient   → @CircuitBreaker @Retry @RateLimiter (Resilience4j)
                │               separate Spring bean — required so the AOP proxy
                │               actually intercepts the call (see Design Decisions)
                ▼
        AnthropicApiClient   → builds the request, calls WebClient, handles timeouts
                │
                ▼
        Anthropic API (external)
```

## Key design decisions

**Resilience via Resilience4j, not ad-hoc try/catch.**
`LlmResilientClient` wraps the LLM call with `@CircuitBreaker`, `@Retry`, and `@RateLimiter`. If the AI service fails repeatedly, the circuit opens and a `fallbackMethod` returns a safe default (`neutral` sentiment, empty tags) instead of a 500 error — the consuming app keeps working.

**Resilience logic lives in its own class — deliberately.**
Resilience4j's annotations rely on Spring AOP proxies. If a method calls another method on `this` from inside the same class, the proxy is bypassed and the annotation is silently ignored. `LlmResilientClient` exists as a separate `@Component` specifically so the call comes from outside the class and passes through the proxy.

**Content-hash caching (MongoDB).**
Every request is hashed (SHA-256) before checking Mongo. Identical input never triggers a second paid API call — this is both a cost control and a latency win.

**Structured output, defensively parsed.**
The LLM is instructed to return JSON matching an exact schema. The response is parsed with a try/catch, because LLMs occasionally wrap JSON in conversational text — that failure mode is handled explicitly rather than assumed away.

**Thin controller, fat nothing.**
The controller only validates and delegates. All business logic (caching, orchestration) lives in `InsightService`; all resilience logic lives in `LlmResilientClient`; all transport logic lives in `AnthropicApiClient`. Each class has exactly one reason to change.

## Tech stack

| Concern | Choice |
|---|---|
| Framework | Spring Boot 4.1.0 (Spring Framework 7) |
| HTTP client | WebClient (reactive, blocking call site) |
| Resilience | Resilience4j (Circuit Breaker, Retry, Rate Limiter) |
| Persistence | MongoDB (Spring Data MongoDB) |
| JSON | Jackson 3 (`tools.jackson.*`) |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5, Mockito |

## Running locally

**1. Start MongoDB:**
```bash
docker-compose up -d
```

**2. Set your API key** (or leave unset to run in mock mode — see `application.yml`):
```bash
export ANTHROPIC_API_KEY=sk-ant-your-key-here
```

**3. Run the application:**
```bash
./mvnw spring-boot:run
```

**4. Call the endpoint:**
```bash
curl -X POST http://localhost:8080/api/v1/insights \
  -H "Content-Type: application/json" \
  -d '{"content": "I love how this skincare routine improved my skin in one week!"}'
```

Expected response:
```json
{
  "summary": "User expresses satisfaction with a skincare routine.",
  "tags": ["skincare", "positive-experience"],
  "sentiment": "positive"
}
```

## Mock mode

Set `llm.api.mock-enabled: true` in `application.yml` to run the full request flow (controller → service → cache → response) without calling the real Anthropic API or needing an API key. Useful for demos and for CI. **Always disabled in production** — the code logs a visible warning whenever it's active.

## Testing

```bash
./mvnw test
```

`LlmResilientClientTest` verifies that when the underlying API client throws, the fallback method returns the expected safe default — the actual behavior the Circuit Breaker relies on.

## Possible extensions

- Swap `AnthropicApiClient` for a second provider behind the same interface (tests Dependency Inversion in practice)
- Async processing with a webhook/polling endpoint for non-blocking insight generation
- Prompt templates stored in MongoDB for runtime configurability without redeployment