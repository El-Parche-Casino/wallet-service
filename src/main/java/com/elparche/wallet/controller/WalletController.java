package com.elparche.wallet.controller;

import com.elparche.wallet.dto.TransaccionResponse;
import com.elparche.wallet.dto.WalletResponse;
import com.elparche.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Endpoints para consulta de fichas e historial de transacciones")
public class WalletController {

    private final WalletService walletService;

    @Operation(
            summary = "Consultar saldo",
            description = "Devuelve el saldo actual y el saldo en juego del jugador. " +
                    "Si el jugador no tiene wallet creado, lo crea automáticamente con 1000 fichas. " +
                    "Las apuestas y ganancias llegan por Redis Streams desde los Room Servers.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saldo consultado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    @GetMapping("/saldo/{username}")
    public ResponseEntity<WalletResponse> consultarSaldo(@PathVariable String username) {
        return ResponseEntity.ok(walletService.consultarSaldo(username));
    }

    @Operation(
            summary = "Historial de transacciones",
            description = "Devuelve todas las transacciones del jugador ordenadas de más reciente a más antigua. " +
                    "Incluye apuestas, ganancias y devoluciones registradas por los Room Servers via Redis Streams.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    @GetMapping("/historial/{username}")
    public ResponseEntity<List<TransaccionResponse>> historial(
            @PathVariable String username) {
        return ResponseEntity.ok(walletService.obtenerHistorial(username));
    }

    @Operation(
            summary = "Historial por tipo de juego",
            description = "Devuelve las transacciones del jugador filtradas por tipo de juego. " +
                    "Valores posibles: BLACKJACK, RULETA, UNO.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Historial obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    @GetMapping("/historial/{username}/{juegoTipo}")
    public ResponseEntity<List<TransaccionResponse>> historialPorJuego(
            @PathVariable String username,
            @PathVariable String juegoTipo) {
        return ResponseEntity.ok(
                walletService.obtenerHistorialPorJuego(username, juegoTipo));
    }

    @Operation(
            summary = "Estado de la recompensa diaria",
            description = "Indica si el jugador puede reclamar la recompensa diaria de 1000 fichas " +
                    "y cuántos segundos faltan si aún no puede.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @GetMapping("/recompensa/{username}")
    public ResponseEntity<?> estadoRecompensa(
            @PathVariable String username,
            org.springframework.security.core.Authentication auth) {
        if (auth == null || !username.equals(auth.getName())) {
            return ResponseEntity.status(403).body(
                    java.util.Map.of("error", "Solo puedes consultar tu propia recompensa"));
        }
        return ResponseEntity.ok(walletService.estadoRecompensa(username));
    }

    @Operation(
            summary = "Reclamar recompensa diaria",
            description = "Acredita 1000 fichas si han pasado 24 horas desde el último reclamo. " +
                    "Si no, responde 400 con el tiempo restante.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @PostMapping("/recompensa/{username}/reclamar")
    public ResponseEntity<?> reclamarRecompensa(
            @PathVariable String username,
            org.springframework.security.core.Authentication auth) {
        if (auth == null || !username.equals(auth.getName())) {
            return ResponseEntity.status(403).body(
                    java.util.Map.of("error", "Solo puedes reclamar tu propia recompensa"));
        }
        try {
            return ResponseEntity.ok(walletService.reclamarRecompensaDiaria(username));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @Operation(
            summary = "Health check",
            description = "Verifica que el wallet-service esté corriendo correctamente."
    )
    @ApiResponse(responseCode = "200", description = "Servicio funcionando correctamente")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Wallet Service funcionando correctamente");
    }
}