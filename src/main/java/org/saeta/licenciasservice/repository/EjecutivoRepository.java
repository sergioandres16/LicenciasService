package org.saeta.licenciasservice.repository;

import org.saeta.licenciasservice.entity.Ejecutivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EjecutivoRepository extends JpaRepository<Ejecutivo, Integer>, JpaSpecificationExecutor<Ejecutivo> {

    /**
     * Busca un ejecutivo por abreviatura
     */
    Optional<Ejecutivo> findByAbreviatura(String abreviatura);

    /**
     * Busca ejecutivos por nombre (contiene)
     */
    List<Ejecutivo> findByNombreEjecutivoContainingIgnoreCase(String nombreEjecutivo);

    /**
     * Busca ejecutivos por estado
     */
    List<Ejecutivo> findByEstado(String estado);

    /**
     * Busca ejecutivos activos
     */
    List<Ejecutivo> findByEstadoOrderByNombreEjecutivoAsc(String estado);

    /**
     * Verifica si existe un ejecutivo con la abreviatura dada
     */
    boolean existsByAbreviatura(String abreviatura);

    /**
     * Verifica si existe un ejecutivo con la abreviatura dada, excluyendo un ID
     */
    boolean existsByAbreviaturaAndIdNot(String abreviatura, Integer id);
}