package com.elparche.wallet.service;

import com.elparche.wallet.dto.TransaccionRequest;
import com.elparche.wallet.dto.TransaccionResponse;
import com.elparche.wallet.dto.WalletResponse;
import com.elparche.wallet.model.Transaccion;
import com.elparche.wallet.model.Wallet;
import com.elparche.wallet.repository.TransaccionRepository;
import com.elparche.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransaccionRepository transaccionRepository;

    public Wallet obtenerOCrearWallet(String username) {
        return walletRepository.findByUsername(username)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder()
                                .username(username)
                                .build()
                ));
    }

    public WalletResponse consultarSaldo(String username) {
        Wallet wallet = obtenerOCrearWallet(username);
        return WalletResponse.builder()
                .username(wallet.getUsername())
                .saldo(wallet.getSaldo())
                .saldoEnJuego(wallet.getSaldoEnJuego())
                .mensaje("Saldo consultado exitosamente")
                .build();
    }

    @Transactional
    public WalletResponse apostar(TransaccionRequest request) {
        Wallet wallet = obtenerOCrearWallet(request.getUsername());

        if (wallet.getSaldo() < request.getMonto()) {
            throw new RuntimeException("Saldo insuficiente. Saldo actual: "
                    + wallet.getSaldo());
        }

        Double saldoAntes = wallet.getSaldo();
        wallet.setSaldo(wallet.getSaldo() - request.getMonto());
        wallet.setSaldoEnJuego(wallet.getSaldoEnJuego() + request.getMonto());
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaccion transaccion = Transaccion.builder()
                .username(request.getUsername())
                .tipo(Transaccion.TipoTransaccion.APUESTA)
                .monto(request.getMonto())
                .saldoAntes(saldoAntes)
                .saldoDespues(wallet.getSaldo())
                .descripcion(request.getDescripcion())
                .salaId(request.getSalaId())
                .juegoTipo(request.getJuegoTipo())
                .build();
        transaccionRepository.save(transaccion);

        return WalletResponse.builder()
                .username(wallet.getUsername())
                .saldo(wallet.getSaldo())
                .saldoEnJuego(wallet.getSaldoEnJuego())
                .mensaje("Apuesta registrada exitosamente")
                .build();
    }

    @Transactional
    public WalletResponse registrarGanancia(TransaccionRequest request) {
        Wallet wallet = obtenerOCrearWallet(request.getUsername());

        Double saldoAntes = wallet.getSaldo();
        wallet.setSaldo(wallet.getSaldo() + request.getMonto());
        wallet.setSaldoEnJuego(Math.max(0, wallet.getSaldoEnJuego() - request.getMonto()));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaccion transaccion = Transaccion.builder()
                .username(request.getUsername())
                .tipo(Transaccion.TipoTransaccion.GANANCIA)
                .monto(request.getMonto())
                .saldoAntes(saldoAntes)
                .saldoDespues(wallet.getSaldo())
                .descripcion(request.getDescripcion())
                .salaId(request.getSalaId())
                .juegoTipo(request.getJuegoTipo())
                .build();
        transaccionRepository.save(transaccion);

        return WalletResponse.builder()
                .username(wallet.getUsername())
                .saldo(wallet.getSaldo())
                .saldoEnJuego(wallet.getSaldoEnJuego())
                .mensaje("Ganancia registrada exitosamente")
                .build();
    }

    @Transactional
    public WalletResponse devolverApuesta(TransaccionRequest request) {
        Wallet wallet = obtenerOCrearWallet(request.getUsername());

        Double saldoAntes = wallet.getSaldo();
        wallet.setSaldo(wallet.getSaldo() + request.getMonto());
        wallet.setSaldoEnJuego(Math.max(0, wallet.getSaldoEnJuego() - request.getMonto()));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        Transaccion transaccion = Transaccion.builder()
                .username(request.getUsername())
                .tipo(Transaccion.TipoTransaccion.DEVOLUCION)
                .monto(request.getMonto())
                .saldoAntes(saldoAntes)
                .saldoDespues(wallet.getSaldo())
                .descripcion("Devolución por fallo técnico o cancelación")
                .salaId(request.getSalaId())
                .juegoTipo(request.getJuegoTipo())
                .build();
        transaccionRepository.save(transaccion);

        return WalletResponse.builder()
                .username(wallet.getUsername())
                .saldo(wallet.getSaldo())
                .saldoEnJuego(wallet.getSaldoEnJuego())
                .mensaje("Apuesta devuelta exitosamente")
                .build();
    }

    public List<TransaccionResponse> obtenerHistorial(String username) {
        return transaccionRepository
                .findByUsernameOrderByFechaHoraDesc(username)
                .stream()
                .map(TransaccionResponse::from)
                .collect(Collectors.toList());
    }

    public List<TransaccionResponse> obtenerHistorialPorJuego(
            String username, String juegoTipo) {
        return transaccionRepository
                .findByUsernameAndJuegoTipoOrderByFechaHoraDesc(username, juegoTipo)
                .stream()
                .map(TransaccionResponse::from)
                .collect(Collectors.toList());
    }
}