package org.saeta.licenciasservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para crear certificado")
public class CreateCertificadoRequest {

    @NotNull(message = "La fecha de emisión es requerida")
    @Schema(description = "Fecha de emisión", example = "2025-01-01T10:00:00")
    private LocalDateTime fechaEmision;

    @NotNull(message = "La fecha de vencimiento es requerida")
    @Schema(description = "Fecha de vencimiento", example = "2026-01-01T10:00:00")
    private LocalDateTime fechaVencimiento;

    @NotNull(message = "El ejecutivo es requerido")
    @Schema(description = "ID del ejecutivo", example = "1")
    private Integer ejecutivoId;

    @NotBlank(message = "El tipo de certificado es requerido")
    @Schema(description = "Tipo de certificado", example = "PERSONA JURIDICA")
    private String tipoCertificado;

    @NotBlank(message = "Los nombres son requeridos")
    @Schema(description = "Nombres del titular", example = "CARLOS EDUARDO")
    private String nombres;

    @NotBlank(message = "El primer apellido es requerido")
    @Schema(description = "Primer apellido", example = "LARA")
    private String primerApellido;

    @Schema(description = "Segundo apellido", example = "SEVILLA")
    private String segundoApellido;

    @NotBlank(message = "El número de documento es requerido")
    @Schema(description = "Número de documento", example = "73270116")
    private String numeroDocumento;

    @Schema(description = "Departamento", example = "GERENCIA GENERAL")
    private String departamento;

    @Schema(description = "Cargo", example = "GERENTE GENERAL")
    private String cargo;

    @Email(message = "El correo electrónico debe ser válido")
    @Schema(description = "Correo electrónico", example = "correo@empresa.com")
    private String correoElectronico;

    @Schema(description = "Razón social", example = "EMPRESA S.A.C.")
    private String razonSocial;

    @Schema(description = "Número RUC", example = "20123456789")
    private String numeroRuc;

    @Schema(description = "Dirección", example = "Av. Principal 123")
    private String direccion;

    @Schema(description = "Código postal", example = "15001")
    private String codigoPostal;

    @Schema(description = "Teléfono", example = "987654321")
    private String telefono;

    @Email(message = "El correo ejecutivo 1 debe ser válido")
    @Schema(description = "Correo ejecutivo 1", example = "ejecutivo1@empresa.com")
    private String correoEjecutivo1;

    @Email(message = "El correo ejecutivo 2 debe ser válido")
    @Schema(description = "Correo ejecutivo 2", example = "ejecutivo2@empresa.com")
    private String correoEjecutivo2;

    @Email(message = "El correo ejecutivo 3 debe ser válido")
    @Schema(description = "Correo ejecutivo 3", example = "ejecutivo3@empresa.com")
    private String correoEjecutivo3;
}