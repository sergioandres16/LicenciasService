package org.saeta.licenciasservice.controller;

import org.saeta.licenciasservice.dto.ValidacionRequest;
import org.saeta.licenciasservice.dto.ValidacionResponse;
import org.saeta.licenciasservice.service.LicenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para validación de licencias
 */
@RestController
@RequestMapping("/api/v1/licencias")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Licencias", description = "API de validación de licencias")
@CrossOrigin(origins = "*", maxAge = 3600)
public class LicenciaController {

    private final LicenciaService licenciaService;

    @PostMapping("/validar")
    @Operation(summary = "Validar licencia", description = "Valida una licencia por dirección MAC")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validación exitosa",
                    content = @Content(schema = @Schema(implementation = ValidacionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Petición inválida"),
            @ApiResponse(responseCode = "401", description = "Licencia no autorizada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ValidacionResponse> validarLicencia(
            @Valid @RequestBody ValidacionRequest request) {
        log.info("Petición de validación recibida para MAC: {}", request.getMac());

        ValidacionResponse response = licenciaService.validarLicencia(request);

        HttpStatus status = response.isValido() ? HttpStatus.OK :
                (response.getCodigoError() == 401 ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST);

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/validar/local")
    @Operation(summary = "Validar licencia local",
            description = "Valida la licencia usando las direcciones MAC del sistema actual")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validación exitosa"),
            @ApiResponse(responseCode = "401", description = "Licencia no autorizada"),
            @ApiResponse(responseCode = "500", description = "Error al obtener MAC del sistema")
    })
    public ResponseEntity<ValidacionResponse> validarLicenciaLocal() {
        log.info("Validando licencia local del sistema");

        ValidacionResponse response = licenciaService.validarLicenciaLocal();

        HttpStatus status = response.isValido() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;

        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/mac-addresses")
    @Operation(summary = "Obtener direcciones MAC",
            description = "Obtiene todas las direcciones MAC del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de direcciones MAC"),
            @ApiResponse(responseCode = "500", description = "Error al obtener direcciones MAC")
    })
    public ResponseEntity<List<String>> obtenerDireccionesMac() {
        log.info("Obteniendo direcciones MAC del sistema");

        List<String> macAddresses = licenciaService.obtenerDireccionesMac();

        return ResponseEntity.ok(macAddresses);
    }

    @GetMapping("/health")
    @Operation(summary = "Estado del servicio", description = "Verifica el estado del servicio")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Servicio de licencias activo");
    }

    @GetMapping("/validar/{mac}")
    @Operation(summary = "Validar licencia por MAC en URL",
            description = "Valida una licencia enviando la MAC como parámetro en la URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validación exitosa"),
            @ApiResponse(responseCode = "400", description = "MAC inválida"),
            @ApiResponse(responseCode = "401", description = "Licencia no autorizada")
    })
    public ResponseEntity<ValidacionResponse> validarLicenciaPorMac(
            @Parameter(description = "Dirección MAC a validar")
            @PathVariable String mac) {
        log.info("Validando licencia para MAC: {}", mac);

        ValidacionRequest request = new ValidacionRequest();
        request.setMac(mac);

        ValidacionResponse response = licenciaService.validarLicencia(request);

        HttpStatus status = response.isValido() ? HttpStatus.OK : HttpStatus.UNAUTHORIZED;

        return ResponseEntity.status(status).body(response);
    }
}
