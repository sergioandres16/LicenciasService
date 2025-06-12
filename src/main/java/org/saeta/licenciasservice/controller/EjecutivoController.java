package org.saeta.licenciasservice.controller;

import org.saeta.licenciasservice.dto.*;
import org.saeta.licenciasservice.service.EjecutivoService;
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
@RequestMapping("/api/v1/administracion/ejecutivo")
@Tag(name = "Administración - Ejecutivo", description = "API para gestión de ejecutivos")
@CrossOrigin(origins = "*")
public class EjecutivoController {

    @Autowired
    private EjecutivoService ejecutivoService;

    @GetMapping
    @Operation(summary = "Listar ejecutivos", description = "Obtiene todos los ejecutivos con paginación")
    public ResponseEntity<Page<EjecutivoDTO>> listarEjecutivos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EjecutivoDTO> ejecutivos = ejecutivoService.listarEjecutivos(pageable);

        return ResponseEntity.ok(ejecutivos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ejecutivo por ID", description = "Obtiene un ejecutivo específico por su ID")
    public ResponseEntity<?> obtenerEjecutivo(@PathVariable Integer id) {
        try {
            EjecutivoDTO ejecutivo = ejecutivoService.obtenerEjecutivo(id);
            return ResponseEntity.ok(ejecutivo);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping
    @Operation(summary = "Crear ejecutivo", description = "Crea un nuevo ejecutivo")
    public ResponseEntity<?> crearEjecutivo(@Valid @RequestBody CreateEjecutivoRequest request) {
        try {
            EjecutivoDTO nuevoEjecutivo = ejecutivoService.crearEjecutivo(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoEjecutivo);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Bad Request");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ejecutivo", description = "Actualiza un ejecutivo existente")
    public ResponseEntity<?> actualizarEjecutivo(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateEjecutivoRequest request) {
        try {
            EjecutivoDTO ejecutivoActualizado = ejecutivoService.actualizarEjecutivo(id, request);
            return ResponseEntity.ok(ejecutivoActualizado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Update Error");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar ejecutivo", description = "Elimina un ejecutivo por su ID")
    public ResponseEntity<?> eliminarEjecutivo(@PathVariable Integer id) {
        try {
            ejecutivoService.eliminarEjecutivo(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Ejecutivo eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Delete Error");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar ejecutivos", description = "Busca ejecutivos por nombre, abreviatura, estado y rango de fechas")
    public ResponseEntity<Page<EjecutivoDTO>> buscarEjecutivos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String abreviatura,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<EjecutivoDTO> ejecutivos;

        if (fechaInicio != null || fechaFin != null) {
            LocalDateTime fechaInicioTime = fechaInicio != null ? fechaInicio.atStartOfDay() : null;
            LocalDateTime fechaFinTime = fechaFin != null ? fechaFin.atTime(23, 59, 59) : null;

            ejecutivos = ejecutivoService.buscarEjecutivosConFechas(
                    nombre, abreviatura, estado, fechaInicioTime, fechaFinTime, pageable);
        } else {
            ejecutivos = ejecutivoService.buscarEjecutivos(nombre, abreviatura, estado, pageable);
        }

        return ResponseEntity.ok(ejecutivos);
    }
}