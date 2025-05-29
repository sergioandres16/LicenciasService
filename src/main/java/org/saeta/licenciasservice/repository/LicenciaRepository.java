package org.saeta.licenciasservice.repository;

import org.saeta.licenciasservice.entity.Licencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenciaRepository extends JpaRepository<Licencia, Integer>, JpaSpecificationExecutor<Licencia> {

    /**
     * Busca una licencia por dirección MAC
     * @param mac dirección MAC a buscar
     * @return Optional con la licencia encontrada
     */
    Optional<Licencia> findByMac(String mac);

    /**
     * Busca licencias por estado
     * @param estado estado a buscar ('1' activo, '0' inactivo)
     * @return lista de licencias con el estado especificado
     */
    List<Licencia> findByEstado(String estado);

    /**
     * Busca licencias por empresa
     * @param empresa nombre de la empresa
     * @return lista de licencias de la empresa
     */
    List<Licencia> findByEmpresaContainingIgnoreCase(String empresa);

    /**
     * Busca licencias vencidas
     * Nota: Esta query ahora necesita evaluar el campo vigencia texto
     * Se recomienda usar el servicio para esta lógica
     */
    @Query("SELECT l FROM Licencia l WHERE l.estado = '1' AND l.fechaHora IS NOT NULL")
    List<Licencia> findLicenciasActivas();

    /**
     * Actualiza el estado de una licencia específica
     */
    @Modifying
    @Transactional
    @Query("UPDATE Licencia l SET l.estado = :estado WHERE l.id = :id")
    int actualizarEstado(@Param("id") Integer id, @Param("estado") String estado);
}