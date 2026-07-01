package com.elparche.wallet.service;

import com.elparche.wallet.dto.TransaccionRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisStreamConsumer
        implements StreamListener<String, MapRecord<String, String, String>> {

    private final WalletService walletService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            String tipo = message.getValue().get("tipo");
            String username = message.getValue().get("username");
            String monto = message.getValue().get("monto");
            String salaId = message.getValue().get("salaId");
            String juegoTipo = message.getValue().get("juegoTipo");

            log.info("Evento recibido — tipo: {}, username: {}, monto: {}",
                    tipo, username, monto);

            TransaccionRequest request = new TransaccionRequest();
            request.setUsername(username);
            request.setMonto(Double.parseDouble(monto));
            request.setSalaId(salaId);
            request.setJuegoTipo(juegoTipo);

            switch (tipo) {
                case "APUESTA" -> walletService.apostar(request);
                case "GANANCIA" -> walletService.registrarGanancia(request);
                case "DEVOLUCION" -> walletService.devolverApuesta(request);
                default -> log.warn("Tipo de transaccion desconocido: {}", tipo);
            }

            redisTemplate.opsForStream().acknowledge(
                    "wallet.transacciones",
                    "wallet-group",
                    message.getId()
            );

            log.info("Evento procesado y confirmado — id: {}", message.getId());

        } catch (Exception e) {
            log.error("Error procesando evento de wallet: {}", e.getMessage());
        }
    }
}