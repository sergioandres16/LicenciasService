package org.saeta.licenciasservice.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.saeta.licenciasservice.dto.*;
import org.saeta.licenciasservice.entity.Proyecto;
import org.saeta.licenciasservice.repository.ProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ProyectoService {

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // Formateadores de fecha
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    };

    /**
     * Obtiene todos los proyectos con paginación
     */
    public Page<ProyectoDTO> listarProyectos(Pageable pageable) {
        return proyectoRepository.findAll(pageable).map(this::convertToDTO);
    }

    /**
     * Obtiene un proyecto por ID
     */
    public ProyectoDTO obtenerProyecto(Integer id) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + id));
        return convertToDTO(proyecto);
    }

    /**
     * Crea un nuevo proyecto
     */
    public ProyectoDTO crearProyecto(CreateProyectoRequest request) {
        // Verificar si ya existe un proyecto con el mismo ID de producto
        if (proyectoRepository.existsByIdProducto(request.getIdProducto())) {
            throw new RuntimeException("Ya existe un proyecto con el ID de producto: " + request.getIdProducto());
        }

        Proyecto proyecto = new Proyecto();
        proyecto.setIdProducto(request.getIdProducto());
        proyecto.setProducto(request.getProducto());
        proyecto.setFechaInicio(request.getFechaInicio());
        proyecto.setVigencia(request.getVigencia());
        proyecto.setCorreoVendedor1(request.getCorreoVendedor1());
        proyecto.setCorreoVendedor2(request.getCorreoVendedor2());
        proyecto.setCorreoJefeVendedor(request.getCorreoJefeVendedor());
        proyecto.setActivo(true);

        // El @PrePersist calculará la vigencia restante
        Proyecto saved = proyectoRepository.save(proyecto);
        return convertToDTO(saved);
    }

    /**
     * Actualiza un proyecto existente
     */
    public ProyectoDTO actualizarProyecto(Integer id, UpdateProyectoRequest request) {
        Proyecto proyecto = proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con ID: " + id));

        // Verificar si el nuevo ID de producto ya existe en otro proyecto
        if (request.getIdProducto() != null && !request.getIdProducto().equals(proyecto.getIdProducto())) {
            Optional<Proyecto> existente = proyectoRepository.findByIdProducto(request.getIdProducto());
            if (existente.isPresent() && !existente.get().getId().equals(id)) {
                throw new RuntimeException("Ya existe otro proyecto con el ID de producto: " + request.getIdProducto());
            }
            proyecto.setIdProducto(request.getIdProducto());
        }

        if (request.getProducto() != null) {
            proyecto.setProducto(request.getProducto());
        }

        if (request.getFechaInicio() != null) {
            proyecto.setFechaInicio(request.getFechaInicio());
        }

        if (request.getVigencia() != null) {
            proyecto.setVigencia(request.getVigencia());
            // Resetear alertas si se cambia la vigencia
            proyecto.setAlerta30Enviada(false);
            proyecto.setAlerta60Enviada(false);
        }

        if (request.getCorreoVendedor1() != null) {
            proyecto.setCorreoVendedor1(request.getCorreoVendedor1());
        }

        if (request.getCorreoVendedor2() != null) {
            proyecto.setCorreoVendedor2(request.getCorreoVendedor2());
        }

        if (request.getCorreoJefeVendedor() != null) {
            proyecto.setCorreoJefeVendedor(request.getCorreoJefeVendedor());
        }

        if (request.getActivo() != null) {
            proyecto.setActivo(request.getActivo());
        }

        // El @PreUpdate calculará la vigencia restante
        Proyecto updated = proyectoRepository.save(proyecto);
        return convertToDTO(updated);
    }

    /**
     * Elimina un proyecto
     */
    public void eliminarProyecto(Integer id) {
        if (!proyectoRepository.existsById(id)) {
            throw new RuntimeException("Proyecto no encontrado con ID: " + id);
        }
        proyectoRepository.deleteById(id);
    }

    /**
     * Busca proyectos con filtros
     */
    public Page<ProyectoDTO> buscarProyectos(String idProducto, String producto, String correo, Integer vigenciaMin, Pageable pageable) {
        Specification<Proyecto> spec = Specification.where(null);

        if (idProducto != null && !idProducto.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("idProducto")), "%" + idProducto.toLowerCase() + "%"));
        }

        if (producto != null && !producto.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("producto")), "%" + producto.toLowerCase() + "%"));
        }

        if (correo != null && !correo.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("correoVendedor1")), "%" + correo.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("correoVendedor2")), "%" + correo.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("correoJefeVendedor")), "%" + correo.toLowerCase() + "%")
                    ));
        }

        if (vigenciaMin != null && vigenciaMin > 0) {
            spec = spec.and((root, query, cb) ->
                    cb.and(
                            cb.isTrue(root.get("activo")),
                            cb.greaterThanOrEqualTo(root.get("vigenciaRestante"), vigenciaMin)
                    ));
        }

        return proyectoRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    /**
     * Procesa un archivo Excel y carga los proyectos
     */
    public List<ProyectoDTO> cargarProyectosDesdeExcel(MultipartFile file) throws IOException {
        List<ProyectoDTO> proyectosCargados = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Saltar la fila de encabezados
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            int rowNum = 1;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowNum++;

                try {
                    ProyectoExcelDTO excelDTO = procesarFilaExcel(row, rowNum);

                    if (excelDTO.isValido()) {
                        // Verificar si ya existe
                        if (proyectoRepository.existsByIdProducto(excelDTO.getIdProducto())) {
                            log.warn("Proyecto con ID {} ya existe, actualizando...", excelDTO.getIdProducto());
                            // Actualizar existente
                            Optional<Proyecto> existente = proyectoRepository.findByIdProducto(excelDTO.getIdProducto());
                            if (existente.isPresent()) {
                                Proyecto proyecto = existente.get();
                                actualizarProyectoDesdeExcel(proyecto, excelDTO);
                                Proyecto updated = proyectoRepository.save(proyecto);
                                proyectosCargados.add(convertToDTO(updated));
                            }
                        } else {
                            // Crear nuevo
                            Proyecto nuevoProyecto = crearProyectoDesdeExcel(excelDTO);
                            Proyecto saved = proyectoRepository.save(nuevoProyecto);
                            proyectosCargados.add(convertToDTO(saved));
                        }
                    } else {
                        errores.add("Fila " + rowNum + ": " + excelDTO.getError());
                    }
                } catch (Exception e) {
                    log.error("Error procesando fila {}: {}", rowNum, e.getMessage());
                    errores.add("Fila " + rowNum + ": " + e.getMessage());
                }
            }
        }

        if (!errores.isEmpty()) {
            log.warn("Se encontraron {} errores durante la carga", errores.size());
            // Podrías lanzar una excepción personalizada con los errores
        }

        return proyectosCargados;
    }

    /**
     * Envía alertas por correo para proyectos próximos a vencer
     */
    public void enviarAlertasVencimiento() {
        // Alertas de 30 días
        List<Proyecto> proyectos30 = proyectoRepository.findProyectosParaAlerta30();
        for (Proyecto proyecto : proyectos30) {
            if (enviarCorreoAlerta(proyecto, 30)) {
                proyecto.setAlerta30Enviada(true);
                proyectoRepository.save(proyecto);
            }
        }

        // Alertas de 60 días
        List<Proyecto> proyectos60 = proyectoRepository.findProyectosParaAlerta60();
        for (Proyecto proyecto : proyectos60) {
            if (enviarCorreoAlerta(proyecto, 60)) {
                proyecto.setAlerta60Enviada(true);
                proyectoRepository.save(proyecto);
            }
        }
    }

    /**
     * Actualiza la vigencia restante de todos los proyectos activos
     */
    public void actualizarVigenciaRestante() {
        List<Proyecto> proyectosActivos = proyectoRepository.findByActivoTrue();

        for (Proyecto proyecto : proyectosActivos) {
            proyecto.actualizarVigenciaRestante();

            // Si el proyecto está vencido, desactivarlo
            if (proyecto.isVencido()) {
                proyecto.setActivo(false);
                log.info("Proyecto {} desactivado por vencimiento", proyecto.getIdProducto());
            }

            proyectoRepository.save(proyecto);
        }
    }

    /**
     * Procesa una fila del Excel
     */
    private ProyectoExcelDTO procesarFilaExcel(Row row, int rowNum) {
        ProyectoExcelDTO dto = new ProyectoExcelDTO();

        try {
            // ID Producto
            dto.setIdProducto(getCellValueAsString(row.getCell(0)));
            if (dto.getIdProducto() == null || dto.getIdProducto().trim().isEmpty()) {
                dto.setValido(false);
                dto.setError("ID de producto vacío");
                return dto;
            }

            // Producto
            dto.setProducto(getCellValueAsString(row.getCell(1)));
            if (dto.getProducto() == null || dto.getProducto().trim().isEmpty()) {
                dto.setValido(false);
                dto.setError("Nombre de producto vacío");
                return dto;
            }

            // Fecha Inicio
            dto.setFechaInicio(getCellValueAsString(row.getCell(2)));
            if (dto.getFechaInicio() == null || dto.getFechaInicio().trim().isEmpty()) {
                dto.setValido(false);
                dto.setError("Fecha de inicio vacía");
                return dto;
            }

            // Vigencia
            dto.setVigencia(getCellValueAsString(row.getCell(3)));
            if (dto.getVigencia() == null || dto.getVigencia().trim().isEmpty()) {
                dto.setValido(false);
                dto.setError("Vigencia vacía");
                return dto;
            }

            // Vigencia Restante (opcional, se calculará)
            Cell vigenciaRestanteCell = row.getCell(4);
            if (vigenciaRestanteCell != null && vigenciaRestanteCell.getCellType() == CellType.NUMERIC) {
                dto.setVigenciaRestante((int) vigenciaRestanteCell.getNumericCellValue());
            }

            // Correo Vendedor 1
            dto.setCorreoVendedor1(getCellValueAsString(row.getCell(5)));
            if (dto.getCorreoVendedor1() == null || dto.getCorreoVendedor1().trim().isEmpty()) {
                dto.setValido(false);
                dto.setError("Correo vendedor 1 vacío");
                return dto;
            }

            // Correo Vendedor 2 (opcional)
            dto.setCorreoVendedor2(getCellValueAsString(row.getCell(6)));

            // Correo Jefe Vendedor (opcional)
            dto.setCorreoJefeVendedor(getCellValueAsString(row.getCell(7)));

        } catch (Exception e) {
            dto.setValido(false);
            dto.setError("Error procesando fila: " + e.getMessage());
        }

        return dto;
    }

    /**
     * Obtiene el valor de una celda como String
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                } else {
                    return String.valueOf((long) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    /**
     * Crea un proyecto desde los datos del Excel
     */
    private Proyecto crearProyectoDesdeExcel(ProyectoExcelDTO dto) {
        Proyecto proyecto = new Proyecto();
        proyecto.setIdProducto(dto.getIdProducto());
        proyecto.setProducto(dto.getProducto());
        proyecto.setFechaInicio(parsearFecha(dto.getFechaInicio()));
        proyecto.setVigencia(dto.getVigencia());
        proyecto.setCorreoVendedor1(dto.getCorreoVendedor1());
        proyecto.setCorreoVendedor2(dto.getCorreoVendedor2());
        proyecto.setCorreoJefeVendedor(dto.getCorreoJefeVendedor());
        proyecto.setActivo(true);

        return proyecto;
    }

    /**
     * Actualiza un proyecto existente con datos del Excel
     */
    private void actualizarProyectoDesdeExcel(Proyecto proyecto, ProyectoExcelDTO dto) {
        proyecto.setProducto(dto.getProducto());
        proyecto.setFechaInicio(parsearFecha(dto.getFechaInicio()));
        proyecto.setVigencia(dto.getVigencia());
        proyecto.setCorreoVendedor1(dto.getCorreoVendedor1());
        proyecto.setCorreoVendedor2(dto.getCorreoVendedor2());
        proyecto.setCorreoJefeVendedor(dto.getCorreoJefeVendedor());
        // Resetear alertas si se actualiza
        proyecto.setAlerta30Enviada(false);
        proyecto.setAlerta60Enviada(false);
    }

    /**
     * Parsea una fecha desde String
     */
    private LocalDateTime parsearFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return LocalDateTime.now();
        }

        // Intentar parsear con diferentes formatos
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                // Si el formato no incluye hora, agregar 00:00:00
                if (!fechaStr.contains(":")) {
                    return LocalDateTime.parse(fechaStr + " 00:00:00",
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                }
                return LocalDateTime.parse(fechaStr, formatter);
            } catch (DateTimeParseException e) {
                // Intentar con el siguiente formato
            }
        }

        // Si ningún formato funciona, usar fecha actual
        log.warn("No se pudo parsear la fecha: {}, usando fecha actual", fechaStr);
        return LocalDateTime.now();
    }

    /**
     * Envía correo de alerta
     */
    private boolean enviarCorreoAlerta(Proyecto proyecto, int dias) {
        if (mailSender == null) {
            log.warn("JavaMailSender no configurado, no se pueden enviar correos");
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("alertas@sistema-licencias.com");

            // Destinatarios
            List<String> destinatarios = new ArrayList<>();
            destinatarios.add(proyecto.getCorreoVendedor1());
            if (proyecto.getCorreoVendedor2() != null && !proyecto.getCorreoVendedor2().isEmpty()) {
                destinatarios.add(proyecto.getCorreoVendedor2());
            }
            if (proyecto.getCorreoJefeVendedor() != null && !proyecto.getCorreoJefeVendedor().isEmpty()) {
                destinatarios.add(proyecto.getCorreoJefeVendedor());
            }

            message.setTo(destinatarios.toArray(new String[0]));
            message.setSubject("⚠️ Alerta de Vencimiento - Proyecto " + proyecto.getIdProducto());

            String texto = String.format(
                    "Estimado(a),\n\n" +
                            "Le informamos que el proyecto %s (%s) está próximo a vencer.\n\n" +
                            "Detalles del proyecto:\n" +
                            "- ID Producto: %s\n" +
                            "- Producto: %s\n" +
                            "- Fecha de inicio: %s\n" +
                            "- Vigencia: %s\n" +
                            "- Días restantes: %d\n" +
                            "- Fecha estimada de vencimiento: %s\n\n" +
                            "Por favor, tome las acciones necesarias para renovar o dar seguimiento a este proyecto.\n\n" +
                            "Saludos cordiales,\n" +
                            "Sistema de Gestión de Proyectos",
                    proyecto.getIdProducto(),
                    proyecto.getProducto(),
                    proyecto.getIdProducto(),
                    proyecto.getProducto(),
                    proyecto.getFechaInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    proyecto.getVigencia(),
                    proyecto.getVigenciaRestante(),
                    proyecto.calcularFechaVencimiento() != null ?
                            proyecto.calcularFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A"
            );

            message.setText(texto);
            mailSender.send(message);

            log.info("Correo de alerta de {} días enviado para proyecto {}", dias, proyecto.getIdProducto());
            return true;

        } catch (Exception e) {
            log.error("Error enviando correo de alerta para proyecto {}: {}",
                    proyecto.getIdProducto(), e.getMessage());
            return false;
        }
    }

    /**
     * Convierte entidad a DTO
     */
    private ProyectoDTO convertToDTO(Proyecto proyecto) {
        String estado = "ACTIVO";
        if (proyecto.isVencido()) {
            estado = "VENCIDO";
        } else if (proyecto.isProximoAVencer30()) {
            estado = "PROXIMO_A_VENCER";
        }

        return ProyectoDTO.builder()
                .id(proyecto.getId())
                .idProducto(proyecto.getIdProducto())
                .producto(proyecto.getProducto())
                .fechaInicio(proyecto.getFechaInicio())
                .vigencia(proyecto.getVigencia())
                .vigenciaRestante(proyecto.getVigenciaRestante())
                .correoVendedor1(proyecto.getCorreoVendedor1())
                .correoVendedor2(proyecto.getCorreoVendedor2())
                .correoJefeVendedor(proyecto.getCorreoJefeVendedor())
                .fechaCarga(proyecto.getFechaCarga())
                .fechaActualizacion(proyecto.getFechaActualizacion())
                .activo(proyecto.getActivo())
                .alerta30Enviada(proyecto.getAlerta30Enviada())
                .alerta60Enviada(proyecto.getAlerta60Enviada())
                .estado(estado)
                .fechaVencimiento(proyecto.calcularFechaVencimiento())
                .build();
    }
}