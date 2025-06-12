package org.saeta.licenciasservice.repository;

import org.saeta.licenciasservice.entity.TipoCertificado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoCertificadoRepository extends JpaRepository<TipoCertificado, Integer>, JpaSpecificationExecutor<TipoCertificado> {

    /**
     * Busca un tipo de certificado por abreviatura
     */
    Optional<TipoCertificado> findByAbreviatura(String abreviatura);

    /**
     * Busca tipos de certificado por nombre (contiene)
     */
    List<TipoCertificado> findByNombreCertificadoContainingIgnoreCase(String nombreCertificado);

    /**
     * Verifica si existe un tipo de certificado con la abreviatura dada
     */
    boolean existsByAbreviatura(String abreviatura);

    /**
     * Verifica si existe un tipo de certificado con la abreviatura dada, excluyendo un ID
     */
    boolean existsByAbreviaturaAndIdNot(String abreviatura, Integer id);
}