package org.saeta.licenciasservice.service;

import org.saeta.licenciasservice.dto.*;
import org.saeta.licenciasservice.entity.Certificado;
import org.saeta.licenciasservice.entity.Ejecutivo;
import org.saeta.licenciasservice.repository.CertificadoRepository;
import org.saeta.licenciasservice.repository.EjecutivoRepository;
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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.persistence.criteria.Predicate;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class CertificadoService {

    @Autowired
    private CertificadoRepository certificadoRepository;

    @Autowired
    private EjecutivoRepository ejecutivoRepository;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // Formateadores de fecha
    private static final DateTimeFormatter[] DATE_FORMATTERS = {
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    };

    /**
     * Obtiene todos los certificados con paginación
     */
    public Page<CertificadoDTO> listarCertificados(Pageable pageable) {
        return certificadoRepository.findAll(pageable).map(this::convertToDTO);
    }

    /**
     * Obtiene un certificado por ID
     */
    public CertificadoDTO obtenerCertificado(Integer id) {
        Certificado certificado = certificadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificado no encontrado con ID: " + id));
        return convertToDTO(certificado);
    }

    /**
     * Crea un nuevo certificado
     */
    public CertificadoDTO crearCertificado(CreateCertificadoRequest request) {
        // Verificar si el ejecutivo existe
        Ejecutivo ejecutivo = ejecutivoRepository.findById(request.getEjecutivoId())
                .orElseThrow(() -> new RuntimeException("Ejecutivo no encontrado con ID: " + request.getEjecutivoId()));

        // Verificar si ya existe un certificado con el mismo documento y tipo
        if (certificadoRepository.existsByNumeroDocumentoAndTipoCertificado(
                request.getNumeroDocumento(), request.getTipoCertificado())) {
            throw new RuntimeException("Ya existe un certificado del mismo tipo para este documento");
        }

        Certificado certificado = new Certificado();
        mapearRequestACertificado(request, certificado);
        certificado.setActivo(true);

        Certificado saved = certificadoRepository.save(certificado);
        return convertToDTO(saved);
    }

    /**
     * Actualiza un certificado existente
     */
    public CertificadoDTO actualizarCertificado(Integer id, UpdateCertificadoRequest request) {
        Certificado certificado = certificadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificado no encontrado con ID: " + id));

        actualizarCertificadoDesdeRequest(certificado, request);

        // Resetear alertas si se cambian las fechas
        if (request.getFechaVencimiento() != null &&
                !request.getFechaVencimiento().equals(certificado.getFechaVencimiento())) {
            certificado.setAlerta10Enviada(false);
            certificado.setAlerta20Enviada(false);
            certificado.setAlerta30Enviada(false);
        }

        Certificado updated = certificadoRepository.save(certificado);
        return convertToDTO(updated);
    }

    /**
     * Elimina un certificado
     */
    public void eliminarCertificado(Integer id) {
        if (!certificadoRepository.existsById(id)) {
            throw new RuntimeException("Certificado no encontrado con ID: " + id);
        }
        certificadoRepository.deleteById(id);
    }

    /**
     * Busca certificados con filtros
     */
    public Page<CertificadoDTO> buscarCertificados(Integer ejecutivoId, String razonSocial,
                                                   String nombres, String estado, Pageable pageable) {
        Specification<Certificado> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (ejecutivoId != null) {
                predicates.add(criteriaBuilder.equal(root.get("ejecutivoId"), ejecutivoId));
            }

            if (razonSocial != null && !razonSocial.isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("razonSocial")),
                        "%" + razonSocial.toLowerCase() + "%"
                ));
            }

            if (nombres != null && !nombres.isEmpty()) {
                Predicate nombresPred = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombres")),
                        "%" + nombres.toLowerCase() + "%"
                );
                Predicate apellidoPred = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("primerApellido")),
                        "%" + nombres.toLowerCase() + "%"
                );
                predicates.add(criteriaBuilder.or(nombresPred, apellidoPred));
            }

            if (estado != null && !estado.isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("estado"), estado));
            }

            predicates.add(criteriaBuilder.equal(root.get("activo"), true));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return certificadoRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    /**
     * Carga certificados desde archivo Excel
     */
    public List<CertificadoDTO> cargarDesdeExcel(MultipartFile file) throws IOException {
        List<CertificadoDTO> certificadosCargados = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Saltar la fila de encabezados
            if (rows.hasNext()) {
                rows.next();
            }

            // Crear un mapa de ejecutivos para búsqueda rápida
            Map<String, Ejecutivo> ejecutivosMap = ejecutivoRepository.findAll().stream()
                    .filter(e -> "1".equals(e.getEstado()))
                    .collect(Collectors.toMap(
                            e -> e.getNombreEjecutivo().toUpperCase(),
                            e -> e,
                            (e1, e2) -> e1
                    ));

            int rowNum = 2;
            while (rows.hasNext()) {
                Row row = rows.next();
                if (isRowEmpty(row)) continue;

                try {
                    CertificadoExcelDTO excelDTO = procesarFilaExcel(row);

                    if (excelDTO.isEsValido()) {
                        // Buscar ejecutivo
                        Ejecutivo ejecutivo = ejecutivosMap.get(excelDTO.getEjecutivo().toUpperCase());
                        if (ejecutivo == null) {
                            errores.add("Fila " + rowNum + ": Ejecutivo no encontrado: " + excelDTO.getEjecutivo());
                            continue;
                        }

                        // Crear certificado
                        Certificado certificado = crearCertificadoDesdeExcel(excelDTO, ejecutivo);

                        // Verificar si ya existe
                        if (!certificadoRepository.existsByNumeroDocumentoAndTipoCertificado(
                                certificado.getNumeroDocumento(), certificado.getTipoCertificado())) {
                            Certificado saved = certificadoRepository.save(certificado);
                            certificadosCargados.add(convertToDTO(saved));
                        } else {
                            log.warn("Certificado duplicado en fila {}: {} - {}",
                                    rowNum, certificado.getNumeroDocumento(), certificado.getTipoCertificado());
                        }
                    } else {
                        errores.add("Fila " + rowNum + ": " + excelDTO.getError());
                    }
                } catch (Exception e) {
                    log.error("Error procesando fila {}: {}", rowNum, e.getMessage());
                    errores.add("Fila " + rowNum + ": " + e.getMessage());
                }
                rowNum++;
            }
        }

        if (!errores.isEmpty()) {
            log.warn("Se encontraron {} errores durante la carga", errores.size());
        }

        return certificadosCargados;
    }

    /**
     * Actualiza el estado de vigencia de todos los certificados
     */
    public void actualizarVigencias() {
        List<Certificado> certificados = certificadoRepository.findCertificadosActivos();

        for (Certificado certificado : certificados) {
            certificado.actualizarVigencia();
            certificadoRepository.save(certificado);
        }

        log.info("Actualizadas {} vigencias de certificados", certificados.size());
    }

    /**
     * Envía alertas de vencimiento
     */
    public void enviarAlertasVencimiento() {
        LocalDateTime ahora = LocalDateTime.now();

        // Alertas de 10 días
        LocalDateTime limite10 = ahora.plusDays(10);
        List<Certificado> certificados10 = certificadoRepository.findCertificadosParaAlerta10(ahora, limite10);
        for (Certificado certificado : certificados10) {
            if (enviarCorreoAlerta(certificado, 10)) {
                certificado.setAlerta10Enviada(true);
                certificadoRepository.save(certificado);
            }
        }

        // Alertas de 20 días
        LocalDateTime limite20 = ahora.plusDays(20);
        List<Certificado> certificados20 = certificadoRepository.findCertificadosParaAlerta20(ahora, limite20);
        for (Certificado certificado : certificados20) {
            if (enviarCorreoAlerta(certificado, 20)) {
                certificado.setAlerta20Enviada(true);
                certificadoRepository.save(certificado);
            }
        }

        // Alertas de 30 días
        LocalDateTime limite30 = ahora.plusDays(30);
        List<Certificado> certificados30 = certificadoRepository.findCertificadosParaAlerta30(ahora, limite30);
        for (Certificado certificado : certificados30) {
            if (enviarCorreoAlerta(certificado, 30)) {
                certificado.setAlerta30Enviada(true);
                certificadoRepository.save(certificado);
            }
        }
    }

    /**
     * Convierte entidad a DTO
     */
    private CertificadoDTO convertToDTO(Certificado certificado) {
        return CertificadoDTO.builder()
                .id(certificado.getId())
                .fechaEmision(certificado.getFechaEmision())
                .fechaVencimiento(certificado.getFechaVencimiento())
                .ejecutivoId(certificado.getEjecutivoId())
                .ejecutivoNombre(certificado.getEjecutivo() != null ?
                        certificado.getEjecutivo().getNombreEjecutivo() : null)
                .tipoCertificado(certificado.getTipoCertificado())
                .nombres(certificado.getNombres())
                .primerApellido(certificado.getPrimerApellido())
                .segundoApellido(certificado.getSegundoApellido())
                .numeroDocumento(certificado.getNumeroDocumento())
                .departamento(certificado.getDepartamento())
                .cargo(certificado.getCargo())
                .correoElectronico(certificado.getCorreoElectronico())
                .razonSocial(certificado.getRazonSocial())
                .numeroRuc(certificado.getNumeroRuc())
                .direccion(certificado.getDireccion())
                .codigoPostal(certificado.getCodigoPostal())
                .telefono(certificado.getTelefono())
                .correoEjecutivo1(certificado.getCorreoEjecutivo1())
                .correoEjecutivo2(certificado.getCorreoEjecutivo2())
                .correoEjecutivo3(certificado.getCorreoEjecutivo3())
                .vigenciaDias(certificado.getVigenciaDias())
                .estado(certificado.getEstado())
                .fechaCarga(certificado.getFechaCarga())
                .fechaActualizacion(certificado.getFechaActualizacion())
                .activo(certificado.getActivo())
                .build();
    }

    /**
     * Mapea request a entidad para creación
     */
    private void mapearRequestACertificado(CreateCertificadoRequest request, Certificado certificado) {
        certificado.setFechaEmision(request.getFechaEmision());
        certificado.setFechaVencimiento(request.getFechaVencimiento());
        certificado.setEjecutivoId(request.getEjecutivoId());
        certificado.setTipoCertificado(request.getTipoCertificado());
        certificado.setNombres(request.getNombres());
        certificado.setPrimerApellido(request.getPrimerApellido());
        certificado.setSegundoApellido(request.getSegundoApellido());
        certificado.setNumeroDocumento(request.getNumeroDocumento());
        certificado.setDepartamento(request.getDepartamento());
        certificado.setCargo(request.getCargo());
        certificado.setCorreoElectronico(request.getCorreoElectronico());
        certificado.setRazonSocial(request.getRazonSocial());
        certificado.setNumeroRuc(request.getNumeroRuc());
        certificado.setDireccion(request.getDireccion());
        certificado.setCodigoPostal(request.getCodigoPostal());
        certificado.setTelefono(request.getTelefono());
        certificado.setCorreoEjecutivo1(request.getCorreoEjecutivo1());
        certificado.setCorreoEjecutivo2(request.getCorreoEjecutivo2());
        certificado.setCorreoEjecutivo3(request.getCorreoEjecutivo3());
    }

    /**
     * Actualiza certificado desde request
     */
    private void actualizarCertificadoDesdeRequest(Certificado certificado, UpdateCertificadoRequest request) {
        if (request.getFechaEmision() != null) {
            certificado.setFechaEmision(request.getFechaEmision());
        }
        if (request.getFechaVencimiento() != null) {
            certificado.setFechaVencimiento(request.getFechaVencimiento());
        }
        if (request.getEjecutivoId() != null) {
            certificado.setEjecutivoId(request.getEjecutivoId());
        }
        if (request.getTipoCertificado() != null) {
            certificado.setTipoCertificado(request.getTipoCertificado());
        }
        if (request.getNombres() != null) {
            certificado.setNombres(request.getNombres());
        }
        if (request.getPrimerApellido() != null) {
            certificado.setPrimerApellido(request.getPrimerApellido());
        }
        if (request.getSegundoApellido() != null) {
            certificado.setSegundoApellido(request.getSegundoApellido());
        }
        if (request.getNumeroDocumento() != null) {
            certificado.setNumeroDocumento(request.getNumeroDocumento());
        }
        if (request.getDepartamento() != null) {
            certificado.setDepartamento(request.getDepartamento());
        }
        if (request.getCargo() != null) {
            certificado.setCargo(request.getCargo());
        }
        if (request.getCorreoElectronico() != null) {
            certificado.setCorreoElectronico(request.getCorreoElectronico());
        }
        if (request.getRazonSocial() != null) {
            certificado.setRazonSocial(request.getRazonSocial());
        }
        if (request.getNumeroRuc() != null) {
            certificado.setNumeroRuc(request.getNumeroRuc());
        }
        if (request.getDireccion() != null) {
            certificado.setDireccion(request.getDireccion());
        }
        if (request.getCodigoPostal() != null) {
            certificado.setCodigoPostal(request.getCodigoPostal());
        }
        if (request.getTelefono() != null) {
            certificado.setTelefono(request.getTelefono());
        }
        if (request.getCorreoEjecutivo1() != null) {
            certificado.setCorreoEjecutivo1(request.getCorreoEjecutivo1());
        }
        if (request.getCorreoEjecutivo2() != null) {
            certificado.setCorreoEjecutivo2(request.getCorreoEjecutivo2());
        }
        if (request.getCorreoEjecutivo3() != null) {
            certificado.setCorreoEjecutivo3(request.getCorreoEjecutivo3());
        }
        if (request.getActivo() != null) {
            certificado.setActivo(request.getActivo());
        }
    }

    /**
     * Procesa una fila del Excel
     */
    private CertificadoExcelDTO procesarFilaExcel(Row row) {
        CertificadoExcelDTO dto = new CertificadoExcelDTO();

        try {
            dto.setFechaEmision(getCellStringValue(row.getCell(0)));
            dto.setFechaVencimiento(getCellStringValue(row.getCell(1)));
            dto.setEjecutivo(getCellStringValue(row.getCell(2)));
            dto.setTipo(getCellStringValue(row.getCell(3)));
            dto.setNombres(getCellStringValue(row.getCell(4)));
            dto.setPrimerApellido(getCellStringValue(row.getCell(5)));
            dto.setSegundoApellido(getCellStringValue(row.getCell(6)));
            dto.setNumeroDocumento(getCellStringValue(row.getCell(7)));
            dto.setDepartamento(getCellStringValue(row.getCell(8)));
            dto.setCargo(getCellStringValue(row.getCell(9)));
            dto.setCorreoElectronico(getCellStringValue(row.getCell(10)));
            dto.setRazonSocial(getCellStringValue(row.getCell(11)));
            dto.setNumeroRuc(getCellStringValue(row.getCell(12)));
            dto.setDireccion(getCellStringValue(row.getCell(13)));
            dto.setCodigoPostal(getCellStringValue(row.getCell(14)));
            dto.setTelefono(getCellStringValue(row.getCell(15)));
            dto.setCorreoEjecutivo1(getCellStringValue(row.getCell(16)));
            dto.setCorreoEjecutivo2(getCellStringValue(row.getCell(17)));
            dto.setCorreoEjecutivo3(getCellStringValue(row.getCell(18)));
            dto.setVigencia(getCellStringValue(row.getCell(19)));

            // Validar campos requeridos
            if (dto.getFechaEmision() == null || dto.getFechaEmision().isEmpty()) {
                dto.setEsValido(false);
                dto.setError("Fecha de emisión es requerida");
            }
            if (dto.getFechaVencimiento() == null || dto.getFechaVencimiento().isEmpty()) {
                dto.setEsValido(false);
                dto.setError("Fecha de vencimiento es requerida");
            }
            if (dto.getEjecutivo() == null || dto.getEjecutivo().isEmpty()) {
                dto.setEsValido(false);
                dto.setError("Ejecutivo es requerido");
            }
            if (dto.getTipo() == null || dto.getTipo().isEmpty()) {
                dto.setEsValido(false);
                dto.setError("Tipo de certificado es requerido");
            }
            if (dto.getNombres() == null || dto.getNombres().isEmpty()) {
                dto.setEsValido(false);
                dto.setError("Nombres son requeridos");
            }
            if (dto.getPrimerApellido() == null || dto.getPrimerApellido().isEmpty()) {
                dto.setEsValido(false);
                dto.setError("Primer apellido es requerido");
            }
            if (dto.getNumeroDocumento() == null || dto.getNumeroDocumento().isEmpty()) {
                dto.setEsValido(false);
                dto.setError("Número de documento es requerido");
            }

        } catch (Exception e) {
            dto.setEsValido(false);
            dto.setError("Error al procesar fila: " + e.getMessage());
        }

        return dto;
    }

    /**
     * Crea certificado desde datos del Excel
     */
    private Certificado crearCertificadoDesdeExcel(CertificadoExcelDTO dto, Ejecutivo ejecutivo) {
        Certificado certificado = new Certificado();

        certificado.setFechaEmision(parsearFecha(dto.getFechaEmision()));
        certificado.setFechaVencimiento(parsearFecha(dto.getFechaVencimiento()));
        certificado.setEjecutivoId(ejecutivo.getId());
        certificado.setTipoCertificado(dto.getTipo());
        certificado.setNombres(dto.getNombres());
        certificado.setPrimerApellido(dto.getPrimerApellido());
        certificado.setSegundoApellido(dto.getSegundoApellido());
        certificado.setNumeroDocumento(dto.getNumeroDocumento());
        certificado.setDepartamento(dto.getDepartamento());
        certificado.setCargo(dto.getCargo());
        certificado.setCorreoElectronico(dto.getCorreoElectronico());
        certificado.setRazonSocial(dto.getRazonSocial());
        certificado.setNumeroRuc(dto.getNumeroRuc());
        certificado.setDireccion(dto.getDireccion());
        certificado.setCodigoPostal(dto.getCodigoPostal());
        certificado.setTelefono(dto.getTelefono());
        certificado.setCorreoEjecutivo1(dto.getCorreoEjecutivo1());
        certificado.setCorreoEjecutivo2(dto.getCorreoEjecutivo2());
        certificado.setCorreoEjecutivo3(dto.getCorreoEjecutivo3());
        certificado.setActivo(true);

        return certificado;
    }

    /**
     * Envía correo de alerta
     */
    private boolean enviarCorreoAlerta(Certificado certificado, int dias) {
        if (mailSender == null) {
            log.warn("JavaMailSender no configurado, no se pueden enviar correos");
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("alertas@sistema-certificados.com");

            // Destinatarios
            List<String> destinatarios = new ArrayList<>();
            if (certificado.getCorreoEjecutivo1() != null && !certificado.getCorreoEjecutivo1().isEmpty()) {
                destinatarios.add(certificado.getCorreoEjecutivo1());
            }
            if (certificado.getCorreoEjecutivo2() != null && !certificado.getCorreoEjecutivo2().isEmpty()) {
                destinatarios.add(certificado.getCorreoEjecutivo2());
            }
            if (certificado.getCorreoEjecutivo3() != null && !certificado.getCorreoEjecutivo3().isEmpty()) {
                destinatarios.add(certificado.getCorreoEjecutivo3());
            }

            if (destinatarios.isEmpty()) {
                log.warn("No hay correos configurados para el certificado ID: {}", certificado.getId());
                return false;
            }

            message.setTo(destinatarios.toArray(new String[0]));
            message.setSubject("⚠️ Alerta de Vencimiento - Certificado " + certificado.getTipoCertificado());

            String texto = String.format(
                    "Estimado(a),\n\n" +
                            "Le informamos que el certificado está próximo a vencer.\n\n" +
                            "Detalles del certificado:\n" +
                            "- Tipo: %s\n" +
                            "- Titular: %s\n" +
                            "- Documento: %s\n" +
                            "- Empresa: %s\n" +
                            "- Fecha de emisión: %s\n" +
                            "- Fecha de vencimiento: %s\n" +
                            "- Días restantes: %d\n\n" +
                            "Por favor, tome las acciones necesarias para renovar este certificado.\n\n" +
                            "Saludos cordiales,\n" +
                            "Sistema de Gestión de Certificados",
                    certificado.getTipoCertificado(),
                    certificado.getNombreCompleto(),
                    certificado.getNumeroDocumento(),
                    certificado.getRazonSocial() != null ? certificado.getRazonSocial() : "N/A",
                    certificado.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    certificado.getFechaVencimiento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    dias
            );

            message.setText(texto);
            mailSender.send(message);

            log.info("Correo de alerta enviado para certificado ID: {} ({} días)", certificado.getId(), dias);
            return true;

        } catch (Exception e) {
            log.error("Error al enviar correo de alerta para certificado ID: {}: {}",
                    certificado.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene el valor de una celda como String
     */
    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    /**
     * Verifica si una fila está vacía
     */
    private boolean isRowEmpty(Row row) {
        if (row == null) return true;

        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    /**
     * Parsea una fecha desde string
     */
    private LocalDateTime parsearFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.isEmpty()) {
            return null;
        }

        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDateTime.parse(fechaStr + " 00:00:00",
                        DateTimeFormatter.ofPattern(formatter.toString() + " HH:mm:ss"));
            } catch (DateTimeParseException e) {
                // Intentar sin hora
                try {
                    return LocalDateTime.parse(fechaStr + "T00:00:00");
                } catch (DateTimeParseException e2) {
                    // Continuar con el siguiente formato
                }
            }
        }

        throw new RuntimeException("No se pudo parsear la fecha: " + fechaStr);
    }
}