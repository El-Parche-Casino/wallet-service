package com.elparche.wallet.dto;

import com.elparche.wallet.model.Transaccion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionResponse {

    private Long id;
    private String username;
    private String tipo;
    private Double monto;
    private Double saldoAntes;
    private Double saldoDespues;
    private String descripcion;
    private String salaId;
    private String juegoTipo;
    private LocalDateTime fechaHora;

    public static TransaccionResponse from(Transaccion t) {
        return TransaccionResponse.builder()
                .id(t.getId())
                .username(t.getUsername())
                .tipo(t.getTipo().name())
                .monto(t.getMonto())
                .saldoAntes(t.getSaldoAntes())
                .saldoDespues(t.getSaldoDespues())
                .descripcion(t.getDescripcion())
                .salaId(t.getSalaId())
                .juegoTipo(t.getJuegoTipo())
                .fechaHora(t.getFechaHora())
                .build();
    }
}