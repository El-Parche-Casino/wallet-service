package com.elparche.wallet.config;

import com.elparche.wallet.service.RedisStreamConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;
import java.net.InetAddress;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisStreamConfig {

    private final RedisConnectionFactory redisConnectionFactory;
    private final RedisStreamConsumer redisStreamConsumer;
    private final RedisTemplate<String, String> redisTemplate;

    @Bean
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamListenerContainer() {

        var options = StreamMessageListenerContainer
                .StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(Duration.ofMillis(100))
                .errorHandler(t -> log.error("Error en el listener de Redis Streams, continuando escucha: {}", t.getMessage()))
                .build();

        var container = StreamMessageListenerContainer.create(
                redisConnectionFactory, options);

        crearStreamYGrupoSiNoExisten();


        container.register(
                StreamMessageListenerContainer.StreamReadRequest
                        .builder(StreamOffset.create("wallet.transacciones", ReadOffset.lastConsumed()))
                        .consumer(Consumer.from("wallet-group", obtenerNombreConsumidor()))
                        .autoAcknowledge(false)
                        .cancelOnError(t -> false)
                        .build(),
                redisStreamConsumer
        );

        container.start();
        log.info("Redis Stream Consumer iniciado — escuchando wallet.transacciones");
        return container;
    }

    private void crearStreamYGrupoSiNoExisten() {
        try {
            redisTemplate.opsForStream().createGroup(
                    "wallet.transacciones",
                    ReadOffset.from("0"),
                    "wallet-group"
            );
            log.info("Grupo wallet-group creado en stream wallet.transacciones");
        } catch (Exception e) {
            log.info("El grupo wallet-group ya existe — continuando");
        }
    }

    private String obtenerNombreConsumidor() {
        try {
            return "wallet-" + InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "wallet-" + UUID.randomUUID().toString().substring(0, 8);
        }
    }


}