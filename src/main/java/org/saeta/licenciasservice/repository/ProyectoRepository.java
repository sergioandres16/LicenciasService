package org.saeta.licenciasservice.repository;

import org.saeta.licenciasservice.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Integer>, JpaSpecificationExecutor<Proyecto> {

    /**
     * Busca un proyecto por ID de producto
     */
    Optional<Proyecto> findByIdProducto(String idProducto);

    /**
     * Busca todos los proyectos activos
     */
    List<Proyecto> findByActivoTrue();

    /**
     * Busca proyectos por producto (nombre)
     */
    List<Proyecto> findByProductoContainingIgnoreCase(String producto);

    /**
     * Busca proyectos con vigencia restante menor o igual a un número de días
     */
    @Query("SELECT p FROM Proyecto p WHERE p.activo = true AND p.vigenciaRestante <= :dias AND p.vigenciaRestante > 0")
    List<Proyecto> findProyectosProximosAVencer(@Param("dias") int dias);

    /**
     * Busca proyectos que necesitan alerta de 30 días
     */
    @Query("SELECT p FROM Proyecto p WHERE p.activo = true AND p.vigenciaRestante <= 30 AND p.vigenciaRestante > 0 AND p.alerta30Enviada = false")
    List<Proyecto> findProyectosParaAlerta30();

    /**
     * Busca proyectos que necesitan alerta de 60 días
     */
    @Query("SELECT p FROM Proyecto p WHERE p.activo = true AND p.vigenciaRestante <= 60 AND p.vigenciaRestante > 30 AND p.alerta60Enviada = false")
    List<Proyecto> findProyectosParaAlerta60();

    /**
     * Busca proyectos vencidos
     */
    @Query("SELECT p FROM Proyecto p WHERE p.activo = true AND (p.vigenciaRestante IS NULL OR p.vigenciaRestante <= 0)")
    List<Proyecto> findProyectosVencidos();

    /**
     * Busca proyectos por correo del vendedor
     */
    @Query("SELECT p FROM Proyecto p WHERE p.correoVendedor1 = :correo OR p.correoVendedor2 = :correo OR p.correoJefeVendedor = :correo")
    List<Proyecto> findByCorreoVendedor(@Param("correo") String correo);

    /**
     * Cuenta proyectos con vigencia restante mayor o igual a días especificados
     */
    @Query("SELECT COUNT(p) FROM Proyecto p WHERE p.activo = true AND p.vigenciaRestante >= :dias")
    long countProyectosConVigenciaMayorIgual(@Param("dias") int dias);

    /**
     * Verifica si existe un proyecto con el mismo ID de producto
     */
    boolean existsByIdProducto(String idProducto);
}