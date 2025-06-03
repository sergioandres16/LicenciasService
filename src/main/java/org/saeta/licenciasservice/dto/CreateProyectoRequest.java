package org.saeta.licenciasservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Email;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CreateProyectoRequest {

    @NotBlank(message = "El ID del producto es obligatorio")
    private String idProducto;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String producto;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;

    @NotBlank(message = "La vigencia es obligatoria")
    private String vigencia;

    @NotBlank(message = "El correo del vendedor 1 es obligatorio")
    @Email(message = "El correo del vendedor 1 debe ser válido")
    private String correoVendedor1;

    @Email(message = "El correo del vendedor 2 debe ser válido")
    private String correoVendedor2;

    @Email(message = "El correo del jefe vendedor debe ser válido")
    private String correoJefeVendedor;
}