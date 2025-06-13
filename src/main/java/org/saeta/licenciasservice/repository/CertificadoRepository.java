package org.saeta.licenciasservice.repository;

import org.saeta.licenciasservice.entity.Certificado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CertificadoRepository extends JpaRepository<Certificado, Integer>, JpaSpecificationExecutor<Certificado> {

    /**
     * Busca certificados por número de documento
     */
    List<Certificado> findByNumeroDocumento(String numeroDocumento);

    /**
     * Busca certificados por ejecutivo
     */
    Page<Certificado> findByEjecutivoId(Integer ejecutivoId, Pageable pageable);

    /**
     * Busca certificados por tipo
     */
    Page<Certificado> findByTipoCertificado(String tipoCertificado, Pageable pageable);

    /**
     * Busca certificados por estado
     */
    Page<Certificado> findByEstado(String estado, Pageable pageable);

    /**
     * Busca certificados activos
     */
    @Query("SELECT c FROM Certificado c WHERE c.activo = true")
    List<Certificado> findCertificadosActivos();

    /**
     * Busca certificados vencidos
     */
    @Query("SELECT c FROM Certificado c WHERE c.fechaVencimiento < :fecha AND c.activo = true")
    List<Certificado> findCertificadosVencidos(@Param("fecha") LocalDateTime fecha);

    /**
     * Busca certificados que vencen en X días
     */
    @Query("SELECT c FROM Certificado c WHERE c.fechaVencimiento BETWEEN :fechaInicio AND :fechaFin AND c.activo = true")
    List<Certificado> findCertificadosPorVencer(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                @Param("fechaFin") LocalDateTime fechaFin);

    /**
     * Busca certificados para alerta de 10 días
     */
    @Query("SELECT c FROM Certificado c WHERE c.activo = true AND c.alerta10Enviada = false " +
            "AND c.fechaVencimiento BETWEEN :ahora AND :limite")
    List<Certificado> findCertificadosParaAlerta10(@Param("ahora") LocalDateTime ahora,
                                                   @Param("limite") LocalDateTime limite);

    /**
     * Busca certificados para alerta de 20 días
     */
    @Query("SELECT c FROM Certificado c WHERE c.activo = true AND c.alerta20Enviada = false " +
            "AND c.fechaVencimiento BETWEEN :ahora AND :limite")
    List<Certificado> findCertificadosParaAlerta20(@Param("ahora") LocalDateTime ahora,
                                                   @Param("limite") LocalDateTime limite);

    /**
     * Busca certificados para alerta de 30 días
     */
    @Query("SELECT c FROM Certificado c WHERE c.activo = true AND c.alerta30Enviada = false " +
            "AND c.fechaVencimiento BETWEEN :ahora AND :limite")
    List<Certificado> findCertificadosParaAlerta30(@Param("ahora") LocalDateTime ahora,
                                                   @Param("limite") LocalDateTime limite);

    /**
     * Verifica si existe un certificado con el mismo documento y tipo
     */
    boolean existsByNumeroDocumentoAndTipoCertificado(String numeroDocumento, String tipoCertificado);

    /**
     * Busca certificados por múltiples criterios
     */
    @Query("SELECT c FROM Certificado c WHERE " +
            "(:ejecutivoId IS NULL OR c.ejecutivoId = :ejecutivoId) AND " +
            "(:razonSocial IS NULL OR LOWER(c.razonSocial) LIKE LOWER(CONCAT('%', :razonSocial, '%'))) AND " +
            "(:nombres IS NULL OR LOWER(CONCAT(c.nombres, ' ', c.primerApellido, ' ', COALESCE(c.segundoApellido, ''))) LIKE LOWER(CONCAT('%', :nombres, '%'))) AND " +
            "(:estado IS NULL OR c.estado = :estado)")
    Page<Certificado> buscarCertificados(@Param("ejecutivoId") Integer ejecutivoId,
                                         @Param("razonSocial") String razonSocial,
                                         @Param("nombres") String nombres,
                                         @Param("estado") String estado,
                                         Pageable pageable);

    /**
     * Cuenta certificados por estado
     */
    @Query("SELECT c.estado, COUNT(c) FROM Certificado c WHERE c.activo = true GROUP BY c.estado")
    List<Object[]> contarPorEstado();
}