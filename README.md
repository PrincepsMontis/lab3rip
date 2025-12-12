UUID Services (Вариант 9)

Вариант 9 — Генерация UUID (неоптимальный путь)  
Проект состоит из двух Spring WebFlux сервисов: синхронный клиент (Service A) и сервер генерации UUID (Service B).

## Архитектура

- **Service B (server, порт 8081)**  
  Реактивный сервис, который:
  - генерирует одиночный UUID как `Mono<String>`;
  - генерирует поток UUID как `Flux<String>`;
  - логирует каждый запрос через реактивный фильтр (`WebFilter`);
  - реализует намеренно неоптимальный алгоритм генерации UUID с множеством промежуточных шагов.

- **Service A (client, порт 8082)**  
  Клиентский сервис, который:
  - использует `WebClient` для обращений к Service B;
  - обрабатывает ответы как `Mono` и `Flux`;
  - имеет собственный логирующий фильтр входящих/исходящих запросов;
  - содержит тесты, демонстрирующие работу `Mono`, retry‑механизма и таймаутов.

## Вариант 9 — постановка задачи

Вариант 9 — Генерация UUID (неоптимальный путь)  

**Идея:**  
A → запрос на генерацию UUID.  
B → выдаёт UUID.  

**Задача:**  
- Реализовать WebClient запрос.  
- Обработать Mono/Flux.  
- Покрыть логирование через фильтры.  

**(Неоптимальная) логика:** генерирует UUID несколькими шагами: строит строку из Random+timestamp, делает Base64 encode/decode несколько раз, вызывает reflection для доступа к нестандартным методам, и создаёт SecureRandom каждый раз.  

**Что нужно сделать:** реализовать сложный генератор с лишними преобразованиями и рефлексией.  

**Пример:**  
- Каждый запрос создаёт SecureRandom().  
- Генерируются 10 промежуточных строк.  

## Структура проекта

```
uuid-services/
├── service-b-server/           # Сервис генерации UUID (порт 8081)
│   ├── src/
│   │   ├── main/java/com/example/serviceb/
│   │   │   ├── ServiceBApplication.java
│   │   │   ├── controller/
│   │   │   │   └── UuidGeneratorController.java
│   │   │   ├── service/
│   │   │   │   └── InefficientUuidService.java
│   │   │   └── filter/
│   │   │       └── ServerLoggingFilter.java
│   │   └── test/java/com/example/serviceb/
│   │       ├── service/
│   │       │   └── InefficientUuidServiceTest.java
│   │       └── controller/
│   │           └── UuidGeneratorControllerTest.java
│   └── pom.xml
│
└── service-a-client/           # Клиентский сервис (порт 8082)
    ├── src/
    │   ├── main/java/com/example/servicea/
    │   │   ├── ServiceAApplication.java
    │   │   ├── client/
    │   │   │   └── UuidClient.java
    │   │   ├── controller/
    │   │   │   └── ClientController.java
    │   │   ├── config/
    │   │   │   └── WebClientConfig.java
    │   │   └── filter/
    │   │       └── LoggingFilter.java
    │   └── test/java/com/example/servicea/
    │       └── client/
    │           └── UuidClientTest.java
    └── pom.xml
```

## Эндпоинты Service B (порт 8081)

| Метод | Эндпоинт | Описание | Возвращает |
|-------|----------|----------|------------|
| GET | `/api/uuid/single` | Генерирует один UUID | `Mono<String>` |
| GET | `/api/uuid/batch?count=N` | Генерирует поток из N UUID | `Flux<String>` (Server-Sent Events) |
| POST | `/api/uuid/custom` | Генерирует UUID с custom seed | `Mono<String>` |

## Эндпоинты Service A (порт 8082)

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| GET | `/client/uuid/single` | Получает один UUID из Service B |
| GET | `/client/uuid/batch?count=N` | Получает поток UUID из Service B |
| POST | `/client/uuid/custom` | Отправляет seed в Service B |

## Запуск

### Требования
- Java 17+
- Maven 3.6+

### Запуск Service B (сервер)
```bash
cd ~/Documents/uuid-services/service-b-server
mvn spring-boot:run
```

### Запуск Service A (клиент)
```bash
cd ~/Documents/uuid-services/service-a-client
mvn spring-boot:run
```

## Тестирование

### Запуск тестов Service B
```bash
cd ~/Documents/uuid-services/service-b-server
mvn clean test
```

**Результат:**
```
Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Запуск тестов Service A
```bash
cd ~/Documents/uuid-services/service-a-client
mvn clean test
```

**Результат:**
```
Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Проверка работоспособности

### Service B (напрямую)
```bash
# Один UUID
curl http://localhost:8081/api/uuid/single

# Поток из 3 UUID
curl http://localhost:8081/api/uuid/batch?count=3

# Custom UUID
curl -X POST http://localhost:8081/api/uuid/custom \\
  -H "Content-Type: text/plain" \\
  -d "my-seed-123"
```

### Service A (через клиент)
```bash
# Один UUID через клиент
curl http://localhost:8082/client/uuid/single

# Batch запрос
curl http://localhost:8082/client/uuid/batch?count=5

# Custom UUID через клиент
curl -X POST http://localhost:8082/client/uuid/custom \\
  -H "Content-Type: text/plain" \\
  -d "test-seed"
```

## Логирование

В обоих сервисах используется реактивный фильтр (`WebFilter`), который:

- логирует входящие запросы (`[SERVER IN]` / `[CLIENT IN]`);
- логирует исходящие ответы со статусом (`[SERVER OUT]` / `[CLIENT OUT]`).

**Пример логов Service B:**
```
[SERVER IN] GET /api/uuid/single
Начинаем неоптимальную генерацию UUID
Промежуточная строка (обрезанная): 7252328164892-86631|7252333276339-6365...
Encode итерация 1: длина = 264
Decode итерация 5: длина = 197
UUID сгенерирован: b20ea3a1ed8dadb7-ea63928f3b1701c6-0000019b13e42fc2-00000000a22dea38
[SERVER OUT] GET /api/uuid/single - Status: 200
```

## Неоптимальная реализация генерации UUID

Генератор UUID в Service B намеренно реализован неоптимально для демонстрации:

1. **SecureRandom создаётся каждый раз** (должен быть переиспользуемым)
2. **Множественные Base64 encode/decode** (5 итераций туда и обратно)
3. **Использование рефлексии** для доступа к приватным методам
4. **Генерация 10+ промежуточных строк** с конкатенацией
5. **Задержки и лишние вычисления** (~100-150ms на UUID)

Это создаёт нагрузку и позволяет наблюдать реактивную обработку потоков с задержками.

## Тестирование Mono и Flux

### Пример теста Mono
```java
@Test
void testGetSingleUuid_ShouldReturnUuid() {
    String mockUuid = "test-uuid-12345";
    mockWebServer.enqueue(new MockResponse()
            .setBody(mockUuid)
            .addHeader("Content-Type", "text/plain"));

    Mono<String> result = uuidClient.getSingleUuid();

    StepVerifier.create(result)
            .expectNext(mockUuid)
            .verifyComplete();
}
```

### Пример теста Flux
```java
@Test
void testGenerateUuidFlux_ShouldReturnMultipleUuids() {
    int count = 3;
    Flux<String> result = uuidService.generateUuidFlux(count);

    StepVerifier.create(result)
            .expectNextCount(count)
            .verifyComplete();
}
```

### Пример теста Retry
```java
@Test
void testGetSingleUuid_WithError_ShouldRetry() {
    mockWebServer.enqueue(new MockResponse().setResponseCode(500));
    mockWebServer.enqueue(new MockResponse().setResponseCode(500));
    mockWebServer.enqueue(new MockResponse()
            .setBody("uuid-after-retry")
            .addHeader("Content-Type", "text/plain"));

    Mono<String> result = uuidClient.getSingleUuid();

    StepVerifier.create(result)
            .expectNext("uuid-after-retry")
            .verifyComplete();
}
```

## Технологии

- **Spring Boot 3.3.0**
- **Spring WebFlux** — реактивное программирование
- **Project Reactor** — Mono/Flux
- **WebClient** — реактивный HTTP-клиент
- **Lombok** — уменьшение boilerplate кода
- **JUnit 5** — тестирование
- **Reactor Test** — StepVerifier для тестирования Mono/Flux
- **MockWebServer** — мокирование HTTP для изолированных тестов


