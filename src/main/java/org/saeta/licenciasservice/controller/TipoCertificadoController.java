package org.saeta.licenciasservice.controller;

import org.saeta.licenciasservice.dto.*;
import org.saeta.licenciasservice.service.TipoCertificadoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/administracion/tipo-certificado")
@Tag(name = "Administración - Tipo Certificado", description = "API para gestión de tipos de certificado")
@CrossOrigin(origins = "*")
public class TipoCertificadoController {

    @Autowired
    private TipoCertificadoService tipoCertificadoService;

    @GetMapping
    @Operation(summary = "Listar tipos de certificado", description = "Obtiene todos los tipos de certificado con paginación")
    public ResponseEntity<Page<TipoCertificadoDTO>> listarTiposCertificado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TipoCertificadoDTO> tiposCertificado = tipoCertificadoService.listarTiposCertificado(pageable);

        return ResponseEntity.ok(tiposCertificado);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo de certificado por ID", description = "Obtiene un tipo de certificado específico por su ID")
    public ResponseEntity<?> obtenerTipoCertificado(@PathVariable Integer id) {
        try {
            TipoCertificadoDTO tipoCertificado = tipoCertificadoService.obtenerTipoCertificado(id);
            return ResponseEntity.ok(tipoCertificado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping
    @Operation(summary = "Crear tipo de certificado", description = "Crea un nuevo tipo de certificado")
    public ResponseEntity<?> crearTipoCertificado(@Valid @RequestBody CreateTipoCertificadoRequest request) {
        try {
            TipoCertificadoDTO nuevoTipoCertificado = tipoCertificadoService.crearTipoCertificado(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoTipoCertificado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Bad Request");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de certificado", description = "Actualiza un tipo de certificado existente")
    public ResponseEntity<?> actualizarTipoCertificado(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateTipoCertificadoRequest request) {
        try {
            TipoCertificadoDTO tipoCertificadoActualizado = tipoCertificadoService.actualizarTipoCertificado(id, request);
            return ResponseEntity.ok(tipoCertificadoActualizado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Update Error");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo de certificado", description = "Elimina un tipo de certificado por su ID")
    public ResponseEntity<?> eliminarTipoCertificado(@PathVariable Integer id) {
        try {
            tipoCertificadoService.eliminarTipoCertificado(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Tipo de certificado eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Delete Error");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar tipos de certificado", description = "Busca tipos de certificado por nombre, abreviatura y rango de fechas")
    public ResponseEntity<Page<TipoCertificadoDTO>> buscarTiposCertificado(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String abreviatura,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<TipoCertificadoDTO> tiposCertificado;

        if (fechaInicio != null || fechaFin != null) {
            LocalDateTime fechaInicioTime = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
            LocalDateTime fechaFinTime = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

            tiposCertificado = tipoCertificadoService.buscarTiposCertificadoConFechas(
                    nombre, abreviatura, fechaInicioTime, fechaFinTime, pageable);
        } else {
            tiposCertificado = tipoCertificadoService.buscarTiposCertificado(nombre, abreviatura, pageable);
        }

        return ResponseEntity.ok(tiposCertificado);
    }
}