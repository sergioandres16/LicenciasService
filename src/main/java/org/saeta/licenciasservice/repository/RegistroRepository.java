package org.saeta.licenciasservice.repository;

import org.saeta.licenciasservice.entity.Registro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repositorio para acceder a la tabla registro
 */
@Repository
public interface RegistroRepository extends JpaRepository<Registro, Integer> {

    /**
     * Busca un registro por dirección MAC
     * @param mac dirección MAC a buscar
     * @return Optional con el registro encontrado
     */
    Optional<Registro> findByMac(String mac);

    /**
     * Busca un registro activo por dirección MAC
     * @param mac dirección MAC a buscar
     * @return Optional con el registro encontrado
     */
    Optional<Registro> findByMacAndEstado(String mac, String estado);

    /**
     * Actualiza la fecha y hora de última validación
     * @param mac dirección MAC
     * @param fechaHora fecha y hora actual
     * @return número de registros actualizados
     */
    @Modifying
    @Transactional
    @Query("UPDATE Registro r SET r.fechaHora = :fechaHora WHERE r.mac = :mac")
    int actualizarFechaHora(@Param("mac") String mac, @Param("fechaHora") LocalDateTime fechaHora);

    /**
     * Verifica si existe una licencia activa para una MAC
     * @param mac dirección MAC
     * @return true si existe y está activa
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Registro r WHERE r.mac = :mac AND r.estado = '1'")
    boolean existeLicenciaActiva(@Param("mac") String mac);
}
