package org.saeta.licenciasservice.service;

import org.saeta.licenciasservice.dto.LicenciaDTO;
import org.saeta.licenciasservice.dto.CreateLicenciaRequest;
import org.saeta.licenciasservice.dto.TiempoRestanteDTO;
import org.saeta.licenciasservice.dto.UpdateLicenciaRequest;
import org.saeta.licenciasservice.entity.Licencia;
import org.saeta.licenciasservice.repository.LicenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class LicenciaManagementService {

    @Autowired
    private LicenciaRepository licenciaRepository;

    public Page<LicenciaDTO> listarLicencias(Pageable pageable) {
        return licenciaRepository.findAll(pageable).map(this::convertToDTO);
    }

    public LicenciaDTO obtenerLicencia(Integer id) {
        Licencia licencia = licenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Licencia no encontrada con ID: " + id));
        return convertToDTO(licencia);
    }

    public LicenciaDTO crearLicencia(CreateLicenciaRequest request) {
        // Verificar si la MAC ya existe
        if (licenciaRepository.findByMac(normalizarMac(request.getMac())).isPresent()) {
            throw new RuntimeException("Ya existe una licencia con la MAC: " + request.getMac());
        }

        Licencia licencia = new Licencia();
        licencia.setEmpresa(request.getEmpresa());
        licencia.setMac(normalizarMac(request.getMac()));
        licencia.setEstado(request.getEstado());
        licencia.setObservacion(request.getObservacion());
        licencia.setFechaHora(LocalDateTime.now());

        // Construir string de vigencia
        String vigenciaTexto = construirVigenciaTexto(request.getVigenciaValor(), request.getVigenciaUnidad());
        licencia.setVigencia(vigenciaTexto);

        Licencia saved = licenciaRepository.save(licencia);
        return convertToDTO(saved);
    }

    public LicenciaDTO actualizarLicencia(Integer id, UpdateLicenciaRequest request) {
        Licencia licencia = licenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Licencia no encontrada con ID: " + id));

        // Verificar si la nueva MAC ya existe en otra licencia
        if (request.getMac() != null && !request.getMac().isEmpty()) {
            String macNormalizada = normalizarMac(request.getMac());
            if (!macNormalizada.equals(licencia.getMac())) {
                licenciaRepository.findByMac(macNormalizada).ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new RuntimeException("Ya existe otra licencia con la MAC: " + request.getMac());
                    }
                });
            }
            licencia.setMac(macNormalizada);
        }

        if (request.getEmpresa() != null) {
            licencia.setEmpresa(request.getEmpresa());
        }

        if (request.getEstado() != null) {
            licencia.setEstado(request.getEstado());
        }

        if (request.getObservacion() != null) {
            licencia.setObservacion(request.getObservacion());
        }

        if (request.getVigenciaValor() != null && request.getVigenciaUnidad() != null) {
            String vigenciaTexto = construirVigenciaTexto(request.getVigenciaValor(), request.getVigenciaUnidad());
            licencia.setVigencia(vigenciaTexto);
        }

        Licencia updated = licenciaRepository.save(licencia);
        return convertToDTO(updated);
    }

    public void eliminarLicencia(Integer id) {
        if (!licenciaRepository.existsById(id)) {
            throw new RuntimeException("Licencia no encontrada con ID: " + id);
        }
        licenciaRepository.deleteById(id);
    }

    public Page<LicenciaDTO> buscarLicencias(String empresa, String mac, Pageable pageable) {
        Specification<Licencia> spec = Specification.where(null);

        if (empresa != null && !empresa.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("empresa")), "%" + empresa.toLowerCase() + "%"));
        }

        if (mac != null && !mac.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("mac")), "%" + mac.toLowerCase() + "%"));
        }

        return licenciaRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    private LicenciaDTO convertToDTO(Licencia licencia) {
        LocalDateTime fechaVencimiento = null;
        Long diasRestantes = 0L;
        Long horasRestantes = 0L;
        Long minutosRestantes = 0L;
        TiempoRestanteDTO tiempoRestante = null;
        boolean vencido = false;

        if (licencia.getFechaHora() != null && licencia.getVigencia() != null) {
            Integer minutos = licencia.getVigenciaEnMinutos();
            if (minutos != null && minutos > 0) {
                fechaVencimiento = licencia.getFechaHora().plusMinutes(minutos);
                diasRestantes = licencia.getDiasRestantes();
                horasRestantes = licencia.getHorasRestantes();
                minutosRestantes = licencia.getMinutosRestantes();
                vencido = licencia.hasVencido();

                var tiempo = licencia.getTiempoRestanteDetallado();
                tiempoRestante = TiempoRestanteDTO.builder()
                        .dias(tiempo.getDias())
                        .horas(tiempo.getHoras())
                        .minutos(tiempo.getMinutos())
                        .totalMinutos(tiempo.getTotalMinutos())
                        .build();
            }
        }

        return LicenciaDTO.builder()
                .id(licencia.getId())
                .empresa(licencia.getEmpresa())
                .mac(licencia.getMac())
                .fechaHora(licencia.getFechaHora())
                .estado(licencia.getEstado())
                .observacion(licencia.getObservacion())
                .vigencia(licencia.getVigencia())
                .fechaVencimiento(fechaVencimiento)
                .diasRestantes(diasRestantes)
                .horasRestantes(horasRestantes)
                .minutosRestantes(minutosRestantes)
                .tiempoRestante(tiempoRestante)
                .activo("1".equals(licencia.getEstado()))
                .vencido(vencido)
                .build();
    }

    private String construirVigenciaTexto(Integer valor, String unidad) {
        if (valor == null || valor <= 0) {
            throw new RuntimeException("El valor de vigencia debe ser positivo");
        }

        // Construir el texto con singular/plural correcto
        String unidadTexto;
        switch (unidad.toLowerCase()) {
            case "horas":
                unidadTexto = valor == 1 ? "hora" : "horas";
                break;
            case "dias":
                unidadTexto = valor == 1 ? "día" : "días";
                break;
            case "semanas":
                unidadTexto = valor == 1 ? "semana" : "semanas";
                break;
            case "meses":
                unidadTexto = valor == 1 ? "mes" : "meses";
                break;
            case "anos":
                unidadTexto = valor == 1 ? "año" : "años";
                break;
            default:
                throw new RuntimeException("Unidad de vigencia no válida: " + unidad);
        }

        return valor + " " + unidadTexto;
    }

    private String normalizarMac(String mac) {
        return mac.trim().replace(":", "-").toUpperCase();
    }
}