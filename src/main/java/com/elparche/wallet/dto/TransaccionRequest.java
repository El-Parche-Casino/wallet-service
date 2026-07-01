package com.elparche.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransaccionRequest {

    @NotBlank(message = "El username es obligatorio")
    private String username;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private Double monto;

    private String descripcion;

    private String salaId;

    private String juegoTipo;
}