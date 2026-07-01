package com.elparche.wallet.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipo;

    @Column(nullable = false)
    private Double monto;

    @Column(nullable = false)
    private Double saldoAntes;

    @Column(nullable = false)
    private Double saldoDespues;

    private String descripcion;

    private String salaId;

    private String juegoTipo;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();

    public enum TipoTransaccion {
        APUESTA,
        GANANCIA,
        DEVOLUCION,
        RECARGA
    }
}