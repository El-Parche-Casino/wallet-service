package com.elparche.wallet.controller;

import com.elparche.wallet.dto.TransaccionResponse;
import com.elparche.wallet.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallet/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TransaccionRepository transaccionRepository;

    @GetMapping("/transacciones")
    public ResponseEntity<List<TransaccionResponse>> transaccionesRecientes(
            @RequestParam(defaultValue = "50") int limit) {
        int tope = Math.min(Math.max(limit, 1), 200);
        List<TransaccionResponse> transacciones = transaccionRepository
                .findAllByOrderByFechaHoraDesc(PageRequest.of(0, tope))
                .stream()
                .map(TransaccionResponse::from)
                .toList();
        return ResponseEntity.ok(transacciones);
    }
}
