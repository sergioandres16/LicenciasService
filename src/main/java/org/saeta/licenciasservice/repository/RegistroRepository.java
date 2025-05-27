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
}
