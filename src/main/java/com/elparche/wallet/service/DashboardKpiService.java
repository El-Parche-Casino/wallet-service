package com.elparche.wallet.service;

import com.elparche.wallet.repository.TransaccionRepository;
import com.elparche.wallet.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardKpiService {

    public static final String CANAL_KPIS = "dashboard.kpis";
    private static final String PREFIJO_SESIONES = "dashboard:sesiones:";
    private static final String PREFIJO_SALAS = "dashboard:salas:";

    private final WalletRepository walletRepository;
    private final TransaccionRepository transaccionRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedRate = 5000)
    public void publicarKpis() {
        try {
            Map<String, Object> kpis = new LinkedHashMap<>();
            kpis.put("jugadoresConectados", contarSesiones());
            kpis.put("salasActivas", contarSalasActivas());
            kpis.put("fichasEnCirculacion", walletRepository.totalFichasEnCirculacion());
            kpis.put("transaccionesPorMinuto",
                    transaccionRepository.countByFechaHoraGreaterThanEqual(
                            LocalDateTime.now().minusSeconds(60)));
            kpis.put("timestamp", System.currentTimeMillis());

            redisTemplate.convertAndSend(CANAL_KPIS, objectMapper.writeValueAsString(kpis));
        } catch (Exception e) {
            log.error("Error calculando/publicando KPIs del dashboard: {}", e.getMessage());
        }
    }

    private int contarSesiones() {
        return sumarHeartbeats(PREFIJO_SESIONES + "*");
    }

    private int contarSalasActivas() {
        return sumarHeartbeats(PREFIJO_SALAS + "*");
    }

    private int sumarHeartbeats(String patron) {
        int total = 0;
        Set<String> claves = redisTemplate.keys(patron);
        if (claves == null) return 0;
        for (String clave : claves) {
            String valor = redisTemplate.opsForValue().get(clave);
            if (valor != null) {
                try {
                    total += Integer.parseInt(valor);
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return total;
    }
}
