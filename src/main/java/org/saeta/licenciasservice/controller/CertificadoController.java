package org.saeta.licenciasservice.controller;

import org.saeta.licenciasservice.dto.*;
import org.saeta.licenciasservice.service.CertificadoService;
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
@RequestMapping("/api/v1/certificados")
@Tag(name = "Gestión de Certificados", description = "API para gestión de certificados digitales con alertas de vencimiento")
@CrossOrigin(origins = "*")
@Slf4j
public class CertificadoController {

    @Autowired
    private CertificadoService certificadoService;

    @GetMapping
    @Operation(summary = "Listar certificados", description = "Obtiene todos los certificados con paginación")
    public ResponseEntity<Page<CertificadoDTO>> listarCertificados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CertificadoDTO> certificados = certificadoService.listarCertificados(pageable);

        return ResponseEntity.ok(certificados);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener certificado por ID", description = "Obtiene un certificado específico por su ID")
    public ResponseEntity<?> obtenerCertificado(@PathVariable Integer id) {
        try {
            CertificadoDTO certificado = certificadoService.obtenerCertificado(id);
            return ResponseEntity.ok(certificado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PostMapping
    @Operation(summary = "Crear certificado", description = "Crea un nuevo certificado")
    public ResponseEntity<?> crearCertificado(@Valid @RequestBody CreateCertificadoRequest request) {
        try {
            CertificadoDTO nuevoCertificado = certificadoService.crearCertificado(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCertificado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Bad Request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar certificado", description = "Actualiza un certificado existente")
    public ResponseEntity<?> actualizarCertificado(
            @PathVariable Integer id,
            @Valid @RequestBody UpdateCertificadoRequest request) {
        try {
            CertificadoDTO certificadoActualizado = certificadoService.actualizarCertificado(id, request);
            return ResponseEntity.ok(certificadoActualizado);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar certificado", description = "Elimina un certificado por ID")
    public ResponseEntity<?> eliminarCertificado(@PathVariable Integer id) {
        try {
            certificadoService.eliminarCertificado(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Certificado eliminado exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("error", "Not Found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar certificados", description = "Busca certificados con filtros")
    public ResponseEntity<Page<CertificadoDTO>> buscarCertificados(
            @RequestParam(required = false) Integer ejecutivoId,
            @RequestParam(required = false) String razonSocial,
            @RequestParam(required = false) String nombres,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CertificadoDTO> certificados = certificadoService.buscarCertificados(
                ejecutivoId, razonSocial, nombres, estado, pageable);

        return ResponseEntity.ok(certificados);
    }

    @PostMapping("/cargar-excel")
    @Operation(summary = "Cargar certificados desde Excel",
            description = "Carga múltiples certificados desde un archivo Excel")
    public ResponseEntity<?> cargarDesdeExcel(
            @Parameter(description = "Archivo Excel con certificados", required = true)
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "El archivo está vacío");
            error.put("error", "Bad Request");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        try {
            List<CertificadoDTO> certificadosCargados = certificadoService.cargarDesdeExcel(file);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Certificados cargados exitosamente");
            response.put("certificadosCargados", certificadosCargados.size());
            response.put("certificados", certificadosCargados);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error procesando el archivo: " + e.getMessage());
            error.put("error", "Processing Error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/descargar-plantilla")
    @Operation(summary = "Descargar plantilla Excel",
            description = "Descarga una plantilla Excel para carga de certificados")
    public ResponseEntity<byte[]> descargarPlantilla() {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Certificados");

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Fecha Emisión", "Fecha V.", "Ejecutivo", "TIPO", "Nombres",
                    "Primer Apellido", "Segundo Apellido", "N° Documento", "Departamento",
                    "Cargo", "correo electronico", "Razon social", "N° RUC", "Dirección",
                    "Código postal", "Telefono", "CorreoEjecutivo1", "CorreoEjecutivo2",
                    "CorreoEjecutivo3", "VIGENCIA"
            };

            // Estilo para encabezados
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Crear filas de ejemplo
            Row row1 = sheet.createRow(1);
            row1.createCell(0).setCellValue("15/08/2022");
            row1.createCell(1).setCellValue("15/08/2023");
            row1.createCell(2).setCellValue("NOEMI");
            row1.createCell(3).setCellValue("PERSONA JURIDICA");
            row1.createCell(4).setCellValue("YESSY TIFFANY");
            row1.createCell(5).setCellValue("BLANCO");
            row1.createCell(6).setCellValue("VILCHEZ");
            row1.createCell(7).setCellValue("43011561");
            row1.createCell(8).setCellValue("GERENCIA GENERAL");
            row1.createCell(9).setCellValue("GERENTE GENERAL");
            row1.createCell(10).setCellValue("constructora.macb@gmail.com");
            row1.createCell(11).setCellValue("MACB INGENIERIA Y CONSTRUCCION SOCIEDAD ANONIMA CERRADA");
            row1.createCell(12).setCellValue("20603995695");
            row1.createCell(13).setCellValue("Cal. Cassinelli Nro. 348 - Chiclayo");
            row1.createCell(14).setCellValue("14000");
            row1.createCell(15).setCellValue("972 923 630");
            row1.createCell(16).setCellValue("ejecutivo1@empresa.com");
            row1.createCell(17).setCellValue("ejecutivo2@empresa.com");
            row1.createCell(18).setCellValue("");
            row1.createCell(19).setCellValue("365");

            Row row2 = sheet.createRow(2);
            row2.createCell(0).setCellValue("15/08/2022");
            row2.createCell(1).setCellValue("15/08/2022");
            row2.createCell(2).setCellValue("NOEMI");
            row2.createCell(3).setCellValue("PERSONA NATURAL");
            row2.createCell(4).setCellValue("YESSY TIFFANY");
            row2.createCell(5).setCellValue("BLANCO");
            row2.createCell(6).setCellValue("VILCHEZ");
            row2.createCell(7).setCellValue("43011561");
            row2.createCell(8).setCellValue("");
            row2.createCell(9).setCellValue("");
            row2.createCell(10).setCellValue("");
            row2.createCell(11).setCellValue("");
            row2.createCell(12).setCellValue("");
            row2.createCell(13).setCellValue("");
            row2.createCell(14).setCellValue("");
            row2.createCell(15).setCellValue("");
            row2.createCell(16).setCellValue("");
            row2.createCell(17).setCellValue("");
            row2.createCell(18).setCellValue("");
            row2.createCell(19).setCellValue("");

            // Ajustar anchos de columna
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Escribir el archivo
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            HttpHeaders headersResponse = new HttpHeaders();
            headersResponse.add("Content-Disposition", "attachment; filename=plantilla_certificados.xlsx");
            headersResponse.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            return ResponseEntity.ok()
                    .headers(headersResponse)
                    .body(outputStream.toByteArray());

        } catch (Exception e) {
            log.error("Error generando plantilla: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/enviar-alertas")
    @Operation(summary = "Enviar alertas de vencimiento",
            description = "Envía correos de alerta para certificados próximos a vencer")
    public ResponseEntity<?> enviarAlertas() {
        try {
            certificadoService.enviarAlertasVencimiento();

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
    @Operation(summary = "Actualizar vigencias",
            description = "Actualiza manualmente el estado de vigencia de todos los certificados")
    public ResponseEntity<?> actualizarVigencias() {
        try {
            certificadoService.actualizarVigencias();

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
}