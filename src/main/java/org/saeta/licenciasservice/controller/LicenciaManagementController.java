package org.saeta.licenciasservice.controller;

import org.saeta.licenciasservice.dto.LicenciaDTO;
import org.saeta.licenciasservice.dto.CreateLicenciaRequest;
import org.saeta.licenciasservice.dto.UpdateLicenciaRequest;
import org.saeta.licenciasservice.entity.Licencia;
import org.saeta.licenciasservice.service.LicenciaManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/licencias/management")
@Tag(name = "Gestión de Licencias", description = "API para gestión de licencias (CRUD)")
@CrossOrigin(origins = "*")
public class LicenciaManagementController {

    @Autowired
    private LicenciaManagementService licenciaManagementService;

    @GetMapping
    @Operation(summary = "Listar licencias", description = "Obtiene todas las licencias con paginación")
    public ResponseEntity<Page<LicenciaDTO>> listarLicencias(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LicenciaDTO> licencias = licenciaManagementService.listarLicencias(pageable);

        return ResponseEntity.ok(licencias);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener licencia por ID", description = "Obtiene una licencia específica por su ID")
    public ResponseEntity<?> obtenerLicencia(@PathVariable Integer id) {
        try {
            LicenciaDTO licencia = licenciaManagementService.obtenerLicencia(id);
            return ResponseEntity.ok(licencia);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping
    @Operation(summary = "Crear licencia", description = "Crea una nueva licencia")
    public ResponseEntity<?> crearLicencia(@Valid @RequestBody CreateLicenciaRequest request) {
        try {
            LicenciaDTO nuevaLicencia = licenciaManagementService.crearLicencia(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaLicencia);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Bad Request");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar licencia", description = "Actualiza una licencia existente")
    public ResponseEntity<?> actualizarLicencia(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateLicenciaRequest request) {
        try {
            LicenciaDTO licenciaActualizada = licenciaManagementService.actualizarLicencia(id, request);
            return ResponseEntity.ok(licenciaActualizada);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Update Error");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar licencia", description = "Elimina una licencia por su ID")
    public ResponseEntity<?> eliminarLicencia(@PathVariable Integer id) {
        try {
            licenciaManagementService.eliminarLicencia(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Licencia eliminada exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Delete Error");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar licencias", description = "Busca licencias por empresa o MAC")
    public ResponseEntity<Page<LicenciaDTO>> buscarLicencias(
            @RequestParam(required = false) String empresa,
            @RequestParam(required = false) String mac,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<LicenciaDTO> licencias = licenciaManagementService.buscarLicencias(empresa, mac, pageable);

        return ResponseEntity.ok(licencias);
    }
}