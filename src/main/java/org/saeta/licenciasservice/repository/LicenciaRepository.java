package org.saeta.licenciasservice.repository;

import org.saeta.licenciasservice.entity.Licencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LicenciaRepository extends JpaRepository<Licencia, Integer> {

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
     * Busca licencias vencidas (fecha_hora + vigencia_dias < now)
     * @return lista de licencias vencidas que aún tienen estado '1'
     */
    @Query("SELECT l FROM Licencia l WHERE l.estado = '1' AND " +
            "l.fechaHora IS NOT NULL AND l.vigenciaDias IS NOT NULL AND " +
            "FUNCTION('DATE_ADD', l.fechaHora, l.vigenciaDias, 'DAY') < CURRENT_TIMESTAMP")
    List<Licencia> findLicenciasVencidas();

    /**
     * Actualiza el estado de licencias vencidas a '0'
     * @return número de registros actualizados
     */
    @Modifying
    @Transactional
    @Query("UPDATE Licencia l SET l.estado = '0' WHERE l.estado = '1' AND " +
            "l.fechaHora IS NOT NULL AND l.vigenciaDias IS NOT NULL AND " +
            "FUNCTION('DATE_ADD', l.fechaHora, l.vigenciaDias, 'DAY') < CURRENT_TIMESTAMP")
    int desactivarLicenciasVencidas();

    /**
     * Busca licencias que vencen en los próximos N días
     * @param dias número de días para la alerta
     * @return lista de licencias próximas a vencer
     */
    @Query("SELECT l FROM Licencia l WHERE l.estado = '1' AND " +
            "l.fechaHora IS NOT NULL AND l.vigenciaDias IS NOT NULL AND " +
            "FUNCTION('DATE_ADD', l.fechaHora, l.vigenciaDias, 'DAY') BETWEEN CURRENT_TIMESTAMP AND " +
            "FUNCTION('DATE_ADD', CURRENT_TIMESTAMP, :dias, 'DAY')")
    List<Licencia> findLicenciasProximasAVencer(@Param("dias") int dias);
}