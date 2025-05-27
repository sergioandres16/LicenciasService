package org.saeta.licenciasservice.controller;

import org.saeta.licenciasservice.dto.ValidacionRequest;
import org.saeta.licenciasservice.dto.ValidacionResponse;
import org.saeta.licenciasservice.service.LicenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/licencias")
@Tag(name = "Licencias", description = "API para validación de licencias por MAC")
@CrossOrigin(origins = "*")
public class LicenciaController {

    @Autowired
    private LicenciaService licenciaService;

    @GetMapping("/health")
    @Operation(summary = "Health Check", description = "Verificar estado del servicio")
    public String health() {
        return "Servicio de licencias activo";
    }

    @PostMapping("/validar")
    @Operation(summary = "Validar Licencia", description = "Valida una licencia por dirección MAC")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Validación exitosa"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    public ResponseEntity<ValidacionResponse> validarLicencia(
            @Parameter(description = "Datos de validación incluyendo MAC")
            @RequestBody ValidacionRequest request) {

        try {
            ValidacionResponse response = licenciaService.validarLicencia(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ValidacionResponse errorResponse = ValidacionResponse.builder()
                    .valido(false)
                    .mensaje("Error interno del servidor: " + e.getMessage())
                    .estado("ERROR")
                    .codigoError(500)
                    .fechaValidacion(java.time.LocalDateTime.now().toString())
                    .build();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/validar/{mac}")
    @Operation(summary = "Validar Licencia por MAC en URL", description = "Valida una licencia usando MAC en la URL")
    public ResponseEntity<ValidacionResponse> validarLicenciaPorMac(
            @Parameter(description = "Dirección MAC a validar")
            @PathVariable String mac) {

        try {
            ValidacionRequest request = new ValidacionRequest();
            request.setMac(mac);

            ValidacionResponse response = licenciaService.validarLicencia(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ValidacionResponse errorResponse = ValidacionResponse.builder()
                    .valido(false)
                    .mensaje("Error interno del servidor: " + e.getMessage())
                    .estado("ERROR")
                    .codigoError(500)
                    .fechaValidacion(java.time.LocalDateTime.now().toString())
                    .build();
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}