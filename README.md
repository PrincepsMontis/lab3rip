# ЛР3 — Профилирование и оптимизация (на основе ЛР2)
**Вариант 9 — Генерация UUID (неоптимальный путь)**

## Идея и архитектура
- **Service A** → отправляет запрос на генерацию UUID (WebClient, Mono/Flux).
- **Service B** → генерирует UUID (намеренно неэффективно в baseline-версии).



## Запуск сервисов
В одном терминале запустите Service B (с JFR):

```bash
cd service-b-server
./mvnw clean package -DskipTests
java -XX:StartFlightRecording=filename=profile.jfr,dumponexit=true,settings=profile \
  -jar target/service-b-server-1.0.0.jar --server.port=8081

```
Во втором терминале запустите Service A: 
```bash
cd service-a-client
./mvnw clean package -DskipTests
java -jar target/service-a-client-1.0.0.jar
```

## Генерация нагрузки
Ниже — два варианта нагрузки: напрямую на B и через A (A вызывает B по WebClient). Эндпоинты заданы контроллерами /api/uuid/* и /client/uuid/*

Нагрузка напрямую на Service B (inefficient)
```bash
for i in $(seq 1 5000); do \
  curl -s "http://localhost:8081/api/uuid/single?mode=inefficient" > /dev/null; \
done
```

Нагрузка напрямую на Service B (optimized)
```bash
for i in $(seq 1 5000); do \
  curl -s "http://localhost:8081/api/uuid/single?mode=optimized" > /dev/null; \
done
```

Нагрузка через Service A (A → B)
```bash
for i in $(seq 1 5000); do \
  curl -s "http://localhost:8082/client/uuid/single" > /dev/null; \
done
```

## Анализ профилей
### Открытие профиля в Java Mission Control (JMC)

После остановки Service B файл profile.jfr появится в директории, где запускалась команда  
Действия:
1. **Открытие файла профиля**:
   - После остановки Service B файл `profile.jfr` появится в текущей директории
   - Запустите JMC: `jmc` или через GUI
   - Меню: File → Open File → выберите `profile.jfr`

## Что было оптимизировано

- Убрано создание SecureRandom на каждый запрос → генератор переиспользуется.
- String.format(...) заменён на более лёгкое формирование строки (например, StringBuilder) для снижения аллокаций.
- Сокращены лишние преобразования/промежуточные строки.
