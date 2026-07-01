package com.elparche.wallet.controller;

import com.elparche.wallet.dto.TransaccionRequest;
import com.elparche.wallet.dto.TransaccionResponse;
import com.elparche.wallet.dto.WalletResponse;
import com.elparche.wallet.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Wallet", description = "Endpoints para gestión de fichas y transacciones de los jugadores")
public class WalletController {

    private final WalletService walletService;

    @Operation(
            summary = "Consultar saldo",
            description = "Devuelve el saldo actual y el saldo en juego del jugador. Si el jugador no tiene wallet creado, lo crea automáticamente con 1000 fichas.",
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
            summary = "Registrar apuesta",
            description = "Descuenta fichas del saldo del jugador y las mueve a saldo en juego. Falla si el jugador no tiene saldo suficiente.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Apuesta registrada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Saldo insuficiente"),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    @PostMapping("/apostar")
    public ResponseEntity<WalletResponse> apostar(
            @Valid @RequestBody TransaccionRequest request) {
        try {
            return ResponseEntity.ok(walletService.apostar(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(
                    WalletResponse.builder()
                            .mensaje(e.getMessage())
                            .build()
            );
        }
    }

    @Operation(
            summary = "Registrar ganancia",
            description = "Acredita fichas al saldo del jugador cuando gana una partida.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ganancia registrada exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    @PostMapping("/ganar")
    public ResponseEntity<WalletResponse> ganar(
            @Valid @RequestBody TransaccionRequest request) {
        return ResponseEntity.ok(walletService.registrarGanancia(request));
    }

    @Operation(
            summary = "Devolver apuesta",
            description = "Devuelve las fichas apostadas al jugador. Se usa cuando hay un fallo técnico o se cancela la partida.",
            security = @SecurityRequirement(name = "Bearer Authentication")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Apuesta devuelta exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token inválido o expirado")
    })
    @PostMapping("/devolver")
    public ResponseEntity<WalletResponse> devolver(
            @Valid @RequestBody TransaccionRequest request) {
        return ResponseEntity.ok(walletService.devolverApuesta(request));
    }

    @Operation(
            summary = "Historial de transacciones",
            description = "Devuelve todas las transacciones del jugador ordenadas de más reciente a más antigua.",
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
            summary = "Historial por juego",
            description = "Devuelve las transacciones del jugador filtradas por tipo de juego. Ejemplo: BLACKJACK, RULETA.",
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
            summary = "Health check",
            description = "Verifica que el wallet-service esté corriendo correctamente."
    )
    @ApiResponse(responseCode = "200", description = "Servicio funcionando correctamente")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Wallet Service funcionando correctamente");
    }
}