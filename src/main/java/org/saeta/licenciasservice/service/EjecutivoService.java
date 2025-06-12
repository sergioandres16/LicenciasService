package org.saeta.licenciasservice.service;

import org.saeta.licenciasservice.dto.EjecutivoDTO;
import org.saeta.licenciasservice.dto.CreateEjecutivoRequest;
import org.saeta.licenciasservice.dto.UpdateEjecutivoRequest;
import org.saeta.licenciasservice.entity.Ejecutivo;
import org.saeta.licenciasservice.repository.EjecutivoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class EjecutivoService {

    @Autowired
    private EjecutivoRepository ejecutivoRepository;

    public Page<EjecutivoDTO> listarEjecutivos(Pageable pageable) {
        return ejecutivoRepository.findAll(pageable).map(this::convertToDTO);
    }

    public EjecutivoDTO obtenerEjecutivo(Integer id) {
        Ejecutivo ejecutivo = ejecutivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejecutivo no encontrado con ID: " + id));
        return convertToDTO(ejecutivo);
    }

    public EjecutivoDTO crearEjecutivo(CreateEjecutivoRequest request) {
        // Verificar si ya existe uno con la misma abreviatura
        if (ejecutivoRepository.existsByAbreviatura(request.getAbreviatura())) {
            throw new RuntimeException("Ya existe un ejecutivo con la abreviatura: " + request.getAbreviatura());
        }

        Ejecutivo ejecutivo = new Ejecutivo();
        ejecutivo.setNombreEjecutivo(request.getNombreEjecutivo());
        ejecutivo.setAbreviatura(request.getAbreviatura().toUpperCase());
        ejecutivo.setEstado(request.getEstado() != null ? request.getEstado() : "1");

        Ejecutivo saved = ejecutivoRepository.save(ejecutivo);
        return convertToDTO(saved);
    }

    public EjecutivoDTO actualizarEjecutivo(Integer id, UpdateEjecutivoRequest request) {
        Ejecutivo ejecutivo = ejecutivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ejecutivo no encontrado con ID: " + id));

        // Verificar si la nueva abreviatura ya existe en otro registro
        if (request.getAbreviatura() != null && !request.getAbreviatura().isEmpty()) {
            String nuevaAbreviatura = request.getAbreviatura().toUpperCase();
            if (!nuevaAbreviatura.equals(ejecutivo.getAbreviatura()) &&
                    ejecutivoRepository.existsByAbreviaturaAndIdNot(nuevaAbreviatura, id)) {
                throw new RuntimeException("Ya existe otro ejecutivo con la abreviatura: " + nuevaAbreviatura);
            }
            ejecutivo.setAbreviatura(nuevaAbreviatura);
        }

        if (request.getNombreEjecutivo() != null) {
            ejecutivo.setNombreEjecutivo(request.getNombreEjecutivo());
        }

        if (request.getEstado() != null) {
            ejecutivo.setEstado(request.getEstado());
        }

        Ejecutivo updated = ejecutivoRepository.save(ejecutivo);
        return convertToDTO(updated);
    }

    public void eliminarEjecutivo(Integer id) {
        if (!ejecutivoRepository.existsById(id)) {
            throw new RuntimeException("Ejecutivo no encontrado con ID: " + id);
        }
        ejecutivoRepository.deleteById(id);
    }

    public Page<EjecutivoDTO> buscarEjecutivos(String nombre, String abreviatura, String estado, Pageable pageable) {
        Specification<Ejecutivo> spec = Specification.where(null);

        if (nombre != null && !nombre.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nombreEjecutivo")), "%" + nombre.toLowerCase() + "%"));
        }

        if (abreviatura != null && !abreviatura.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("abreviatura")), "%" + abreviatura.toLowerCase() + "%"));
        }

        if (estado != null && !estado.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("estado"), estado));
        }

        return ejecutivoRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    public Page<EjecutivoDTO> buscarEjecutivosConFechas(
            String nombre,
            String abreviatura,
            String estado,
            LocalDateTime fechaInicio,
            LocalDateTime fechaFin,
            Pageable pageable) {

        Specification<Ejecutivo> spec = Specification.where(null);

        if (nombre != null && !nombre.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("nombreEjecutivo")), "%" + nombre.toLowerCase() + "%"));
        }

        if (abreviatura != null && !abreviatura.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("abreviatura")), "%" + abreviatura.toLowerCase() + "%"));
        }

        if (estado != null && !estado.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("estado"), estado));
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

        return ejecutivoRepository.findAll(spec, pageable).map(this::convertToDTO);
    }

    private EjecutivoDTO convertToDTO(Ejecutivo ejecutivo) {
        return EjecutivoDTO.builder()
                .id(ejecutivo.getId())
                .nombreEjecutivo(ejecutivo.getNombreEjecutivo())
                .abreviatura(ejecutivo.getAbreviatura())
                .estado(ejecutivo.getEstado())
                .activo(ejecutivo.isActivo())
                .fechaCreacion(ejecutivo.getFechaCreacion())
                .fechaActualizacion(ejecutivo.getFechaActualizacion())
                .build();
    }
}