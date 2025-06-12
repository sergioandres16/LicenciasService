package org.saeta.licenciasservice.service;

import org.saeta.licenciasservice.dto.TipoCertificadoDTO;
import org.saeta.licenciasservice.dto.CreateTipoCertificadoRequest;
import org.saeta.licenciasservice.dto.UpdateTipoCertificadoRequest;
import org.saeta.licenciasservice.entity.TipoCertificado;
import org.saeta.licenciasservice.repository.TipoCertificadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class TipoCertificadoService {

    @Autowired
    private TipoCertificadoRepository tipoCertificadoRepository;

    public Page<TipoCertificadoDTO> listarTiposCertificado(Pageable pageable) {
        return tipoCertificadoRepository.findAll(pageable).map(this::convertToDTO);
    }

    public TipoCertificadoDTO obtenerTipoCertificado(Integer id) {
        TipoCertificado tipoCertificado = tipoCertificadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de certificado no encontrado con ID: " + id));
        return convertToDTO(tipoCertificado);
    }

    public TipoCertificadoDTO crearTipoCertificado(CreateTipoCertificadoRequest request) {
        // Verificar si ya existe uno con la misma abreviatura
        if (tipoCertificadoRepository.existsByAbreviatura(request.getAbreviatura())) {
            throw new RuntimeException("Ya existe un tipo de certificado con la abreviatura: " + request.getAbreviatura());
        }

        TipoCertificado tipoCertificado = new TipoCertificado();
        tipoCertificado.setNombreCertificado(request.getNombreCertificado());
        tipoCertificado.setAbreviatura(request.getAbreviatura().toUpperCase());

        TipoCertificado saved = tipoCertificadoRepository.save(tipoCertificado);
        return convertToDTO(saved);
    }

    public TipoCertificadoDTO actualizarTipoCertificado(Integer id, UpdateTipoCertificadoRequest request) {
        TipoCertificado tipoCertificado = tipoCertificadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de certificado no encontrado con ID: " + id));

        // Verificar si la nueva abreviatura ya existe en otro registro
        if (request.getAbreviatura() != null && !request.getAbreviatura().isEmpty()) {
            String nuevaAbreviatura = request.getAbreviatura().toUpperCase();
            if (!nuevaAbreviatura.equals(tipoCertificado.getAbreviatura()) &&
                    tipoCertificadoRepository.existsByAbreviaturaAndIdNot(nuevaAbreviatura, id)) {
                throw new RuntimeException("Ya existe otro tipo de certificado con la abreviatura: " + nuevaAbreviatura);
            }
            tipoCertificado.setAbreviatura(nuevaAbreviatura);
        }

        if (request.getNombreCertificado() != null) {
            tipoCertificado.setNombreCertificado(request.getNombreCertificado());
        }

        TipoCertificado updated = tipoCertificadoRepository.save(tipoCertificado);
        return convertToDTO(updated);
    }

    public void eliminarTipoCertificado(Integer id) {
        if (!tipoCertificadoRepository.existsById(id)) {
            throw new RuntimeException("Tipo de certificado no encontrado con ID: " + id);
        }
        tipoCertificadoRepository.deleteById(id);
    }

    public Page<TipoCertificadoDTO> buscarTiposCertificado(String nombre, String abreviatura, Pageable pageable) {
        Specification<TipoCertificado> spec = Specification.where(null);

        if (nombre != null && !nombre.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nombreCertificado")), "%" + nombre.toLowerCase() + "%"));
        }

        if (abreviatura != null && !abreviatura.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("abreviatura")), "%" + abreviatura.toLowerCase() + "%"));
        }

        return tipoCertificadoRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    public Page<TipoCertificadoDTO> buscarTiposCertificadoConFechas(
            String nombre,
            String abreviatura,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable) {

        Specification<TipoCertificado> spec = Specification.where(null);

        if (nombre != null && !nombre.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nombreCertificado")), "%" + nombre.toLowerCase() + "%"));
        }

        if (abreviatura != null && !abreviatura.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("abreviatura")), "%" + abreviatura.toLowerCase() + "%"));
        }

        if (fechaInicio != null && fechaFin != null) {
            spec = spec.and((root, query, cb) ->
                    cb.between(root.get("fechaCreacion"), fechaInicio, fechaFin));
        } else if (fechaInicio != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaInicio));
        } else if (fechaFin != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("fechaCreacion"), fechaFin));
        }

        return tipoCertificadoRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    private TipoCertificadoDTO convertToDTO(TipoCertificado tipoCertificado) {
        return TipoCertificadoDTO.builder()
                .id(tipoCertificado.getId())
                .nombreCertificado(tipoCertificado.getNombreCertificado())
                .abreviatura(tipoCertificado.getAbreviatura())
                .fechaCreacion(tipoCertificado.getFechaCreacion())
                .fechaActualizacion(tipoCertificado.getFechaActualizacion())
                .build();
    }
}