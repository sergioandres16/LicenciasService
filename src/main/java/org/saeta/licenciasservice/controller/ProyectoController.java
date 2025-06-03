package org.saeta.licenciasservice.controller;

import org.saeta.licenciasservice.dto.*;
import org.saeta.licenciasservice.service.ProyectoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/proyectos")
@Tag(name = "Gestión de Proyectos", description = "API para gestión de proyectos con alertas de vencimiento")
@CrossOrigin(origins = "*")
@Slf4j
public class ProyectoController {

    @Autowired
    private ProyectoService proyectoService;

    @GetMapping
    @Operation(summary = "Listar proyectos", description = "Obtiene todos los proyectos con paginación")
    public ResponseEntity<Page<ProyectoDTO>> listarProyectos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProyectoDTO> proyectos = proyectoService.listarProyectos(pageable);

        return ResponseEntity.ok(proyectos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener proyecto por ID", description = "Obtiene un proyecto específico por su ID")
    public ResponseEntity<?> obtenerProyecto(@PathVariable Integer id) {
        try {
            ProyectoDTO proyecto = proyectoService.obtenerProyecto(id);
            return ResponseEntity.ok(proyecto);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping
    @Operation(summary = "Crear proyecto", description = "Crea un nuevo proyecto")
    public ResponseEntity<?> crearProyecto(@Valid @RequestBody CreateProyectoRequest request) {
        try {
            ProyectoDTO nuevoProyecto = proyectoService.crearProyecto(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProyecto);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Bad Request");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proyecto", description = "Actualiza un proyecto existente")
    public ResponseEntity<?> actualizarProyecto(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateProyectoRequest request) {
        try {
            ProyectoDTO proyectoActualizado = proyectoService.actualizarProyecto(id, request);
            return ResponseEntity.ok(proyectoActualizado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Update Error");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proyecto", description = "Elimina un proyecto por su ID")
    public ResponseEntity<?> eliminarProyecto(@PathVariable Integer id) {
        try {
            proyectoService.eliminarProyecto(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Proyecto eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Delete Error");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Buscar proyectos", description = "Busca proyectos por diferentes criterios")
    public ResponseEntity<Page<ProyectoDTO>> buscarProyectos(
            @RequestParam(required = false) String idProducto,
            @RequestParam(required = false) String producto,
            @RequestParam(required = false) String correo,
            @RequestParam(required = false) Integer vigenciaMin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<ProyectoDTO> proyectos = proyectoService.buscarProyectos(idProducto, producto, correo, vigenciaMin, pageable);

        return ResponseEntity.ok(proyectos);
    }

    @PostMapping("/cargar-excel")
    @Operation(summary = "Cargar proyectos desde Excel",
            description = "Carga masiva de proyectos desde un archivo Excel")
    public ResponseEntity<?> cargarProyectosDesdeExcel(
            @Parameter(description = "Archivo Excel con los proyectos")
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Por favor seleccione un archivo");
            error.put("error", "File Required");
            return ResponseEntity.badRequest().body(error);
        }

        // Validar tipo de archivo por extensión principalmente
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "El archivo debe ser un Excel (.xlsx o .xls)");
            error.put("error", "Invalid File Type");
            return ResponseEntity.badRequest().body(error);
        }

        try {
            List<ProyectoDTO> proyectosCargados = proyectoService.cargarProyectosDesdeExcel(file);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Archivo procesado exitosamente");
            response.put("proyectosCargados", proyectosCargados.size());
            response.put("proyectos", proyectosCargados);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error procesando el archivo: " + e.getMessage());
            error.put("error", "Processing Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/enviar-alertas")
    @Operation(summary = "Enviar alertas de vencimiento",
            description = "Envía correos de alerta para proyectos próximos a vencer")
    public ResponseEntity<?> enviarAlertas() {
        try {
            proyectoService.enviarAlertasVencimiento();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Proceso de alertas ejecutado exitosamente");
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error enviando alertas: " + e.getMessage());
            error.put("error", "Alert Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/actualizar-vigencias")
    @Operation(summary = "Actualizar vigencias restantes",
            description = "Actualiza manualmente los días de vigencia restante de todos los proyectos")
    public ResponseEntity<?> actualizarVigencias() {
        try {
            proyectoService.actualizarVigenciaRestante();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Vigencias actualizadas exitosamente");
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error actualizando vigencias: " + e.getMessage());
            error.put("error", "Update Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/plantilla-excel")
    @Operation(summary = "Descargar plantilla Excel",
            description = "Descarga una plantilla Excel con el formato correcto para carga masiva")
    public ResponseEntity<byte[]> descargarPlantilla() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Plantilla Proyectos");

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "idProducto", "Producto", "fechaInicio", "vigencia",
                    "vigenciaRestante", "correoVendedor1", "correoVendedor2", "correoJefeVendedor"
            };

            // Estilo para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Agregar filas de ejemplo
            Row ejemplo1 = sheet.createRow(1);
            ejemplo1.createCell(0).setCellValue("PROD-001");
            ejemplo1.createCell(1).setCellValue("Sistema de Gestión (Ejemplo)");
            ejemplo1.createCell(2).setCellValue("01/01/2024");
            ejemplo1.createCell(3).setCellValue("1 año");
            ejemplo1.createCell(4).setCellValue(""); // Se calcula automáticamente
            ejemplo1.createCell(5).setCellValue("vendedor1@empresa.com");
            ejemplo1.createCell(6).setCellValue("vendedor2@empresa.com");
            ejemplo1.createCell(7).setCellValue("jefe@empresa.com");

            Row ejemplo2 = sheet.createRow(2);
            ejemplo2.createCell(0).setCellValue("PROD-002");
            ejemplo2.createCell(1).setCellValue("Software Contable (Ejemplo)");
            ejemplo2.createCell(2).setCellValue("15/03/2024");
            ejemplo2.createCell(3).setCellValue("6 meses");
            ejemplo2.createCell(4).setCellValue("");
            ejemplo2.createCell(5).setCellValue("maria@empresa.com");
            ejemplo2.createCell(6).setCellValue("");
            ejemplo2.createCell(7).setCellValue("supervisor@empresa.com");

            // Ajustar ancho de columnas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convertir a bytes
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            byte[] bytes = outputStream.toByteArray();

            HttpHeaders headersResponse = new HttpHeaders();
            headersResponse.add("Content-Disposition", "attachment; filename=plantilla_proyectos.xlsx");
            headersResponse.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok()
                    .headers(headersResponse)
                    .body(bytes);

        } catch (Exception e) {
            log.error("Error generando plantilla Excel: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}