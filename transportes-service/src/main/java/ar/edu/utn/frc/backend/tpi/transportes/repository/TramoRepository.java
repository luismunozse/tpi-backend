package ar.edu.utn.frc.backend.tpi.transportes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frc.backend.tpi.transportes.entity.EstadoTramo;
import ar.edu.utn.frc.backend.tpi.transportes.entity.Tramo;

/**
 * Repositorio para la entidad Tramo.
 * Proporciona operaciones CRUD y consultas personalizadas.
 */
public interface TramoRepository extends JpaRepository<Tramo, Long> {

    /**
     * Busca todos los tramos de una ruta específica.
     *
     * @param rutaId identificador de la ruta
     * @return lista de tramos de la ruta
     */
    List<Tramo> findByRutaId(Long rutaId);

    /**
     * Busca todos los tramos con un estado específico.
     *
     * @param estado estado del tramo
     * @return lista de tramos con el estado indicado
     */
    List<Tramo> findByEstado(EstadoTramo estado);

    /**
     * Busca todos los tramos asignados a un camión específico.
     *
     * @param camionId identificador del camión
     * @return lista de tramos asignados al camión
     */
    List<Tramo> findByCamionId(Long camionId);

    /**
     * Busca tramos por ruta y estado.
     *
     * @param rutaId identificador de la ruta
     * @param estado estado del tramo
     * @return lista de tramos que cumplen las condiciones
     */
    List<Tramo> findByRutaIdAndEstado(Long rutaId, EstadoTramo estado);
}
