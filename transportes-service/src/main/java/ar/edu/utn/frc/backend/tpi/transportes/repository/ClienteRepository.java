package ar.edu.utn.frc.backend.tpi.transportes.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frc.backend.tpi.transportes.entity.Cliente;

/**
 * Repositorio para la entidad Cliente.
 * Proporciona operaciones CRUD y consultas personalizadas.
 */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca un cliente por su email.
     * El email es único en el sistema.
     *
     * @param email email del cliente
     * @return Optional con el cliente si existe
     */
    Optional<Cliente> findByEmail(String email);

    /**
     * Verifica si existe un cliente con el email dado.
     *
     * @param email email a verificar
     * @return true si existe, false si no
     */
    boolean existsByEmail(String email);
}
