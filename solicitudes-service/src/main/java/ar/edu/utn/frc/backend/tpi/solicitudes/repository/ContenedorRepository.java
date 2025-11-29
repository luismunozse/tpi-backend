package ar.edu.utn.frc.backend.tpi.solicitudes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Contenedor;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.EstadoContenedor;

/**
 * Repositorio para la entidad Contenedor.
 * Proporciona operaciones CRUD y consultas personalizadas.
 */
public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {

    /**
     * Busca un contenedor por su número de serie.
     * El número de serie es único en el sistema.
     *
     * @param numeroSerie número de serie del contenedor
     * @return Optional con el contenedor si existe
     */
    Optional<Contenedor> findByNumeroSerie(String numeroSerie);

    /**
     * Verifica si existe un contenedor con el número de serie dado.
     *
     * @param numeroSerie número de serie a verificar
     * @return true si existe, false si no
     */
    boolean existsByNumeroSerie(String numeroSerie);

    /**
     * Busca todos los contenedores de un cliente específico.
     *
     * @param clienteId identificador del cliente
     * @return lista de contenedores del cliente
     */
    List<Contenedor> findByClienteId(Long clienteId);

    /**
     * Busca todos los contenedores con un estado específico.
     *
     * @param estado estado del contenedor
     * @return lista de contenedores con el estado indicado
     */
    List<Contenedor> findByEstado(EstadoContenedor estado);
}
