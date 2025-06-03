package org.saeta.licenciasservice.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UpdateProyectoRequest {
    private String idProducto;
    private String producto;
    private LocalDateTime fechaInicio;
    private String vigencia;

    @Email(message = "El correo del vendedor 1 debe ser válido")
    private String correoVendedor1;

    @Email(message = "El correo del vendedor 2 debe ser válido")
    private String correoVendedor2;

    @Email(message = "El correo del jefe vendedor debe ser válido")
    private String correoJefeVendedor;

    private Boolean activo;
}