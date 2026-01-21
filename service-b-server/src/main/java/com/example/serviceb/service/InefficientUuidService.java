package com.example.serviceb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Random;

@Slf4j
@Service("inefficientUuidService")
public class InefficientUuidService implements UuidGenerator {

    @Override
    public Mono<String> generateOne() {
        return generateUuidMono();
    }

    @Override
    public Flux<String> generateMany(int count) {
        return generateUuidFlux(count);
    }

    // Неоптимальная генерация UUID (Mono)
    public Mono<String> generateUuidMono() {
        return Mono.fromCallable(() -> {
            log.debug("Начинаем неоптимальную генерацию UUID");

            // Неоптимально: создаем SecureRandom при каждом запросе
            SecureRandom secureRandom = new SecureRandom();

            // Генерируем 10 промежуточных строк с задержками
            StringBuilder intermediateString = new StringBuilder();
            for (int i = 0; i < 10; i++) {
                long timestamp = System.nanoTime();
                int randomInt = secureRandom.nextInt(100_000);
                intermediateString.append(timestamp)
                        .append("-")
                        .append(randomInt)
                        .append("|");

                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            String baseString = intermediateString.toString();
            log.debug("Промежуточная строка (обрезанная): {}",
                    baseString.substring(0, Math.min(50, baseString.length())));

            // Многократное Base64 encode
            String encoded = baseString;
            for (int i = 0; i < 5; i++) {
                encoded = Base64.getEncoder().encodeToString(encoded.getBytes());
                log.debug("Encode итерация {}: длина = {}", i + 1, encoded.length());
            }

            // Многократное Base64 decode
            String decoded = encoded;
            for (int i = 0; i < 5; i++) {
                decoded = new String(Base64.getDecoder().decode(decoded));
                log.debug("Decode итерация {}: длина = {}", i + 1, decoded.length());
            }

            // Рефлексия для работы с Random
            String finalUuid = useReflectionToGenerateUuid(decoded);

            log.debug("UUID сгенерирован: {}", finalUuid);
            return finalUuid;
        }).delayElement(Duration.ofMillis(100)); // Искусственная задержка
    }

    // Неоптимальная генерация потока UUID (Flux)
    public Flux<String> generateUuidFlux(int count) {
        return Flux.range(0, count)
                .flatMap(i -> generateUuidMono()
                        .doOnNext(uuid -> log.debug("Flux элемент {}: {}", i, uuid))
                );
    }

    // Рефлексия + лишние преобразования строк
    private String useReflectionToGenerateUuid(String seed) {
        try {
            Class<?> randomClass = Class.forName("java.util.Random");
            Method nextLongMethod = randomClass.getDeclaredMethod("nextLong");

            Random random = new Random(seed.hashCode());
            Long randomValue1 = (Long) nextLongMethod.invoke(random);
            Long randomValue2 = (Long) nextLongMethod.invoke(random);

            String uuid = String.format(
                    "%016x-%016x-%016x-%016x",
                    randomValue1,
                    randomValue2,
                    System.currentTimeMillis(),
                    seed.hashCode()
            );

            String base64Uuid = Base64.getEncoder().encodeToString(uuid.getBytes());
            return new String(Base64.getDecoder().decode(base64Uuid));
        } catch (Exception e) {
            log.error("Ошибка рефлексии при генерации UUID", e);
            return "ERROR-" + System.currentTimeMillis();
        }
    }
}
