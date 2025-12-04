package ar.edu.utn.frc.backend.tpi.flota.repository;

import ar.edu.utn.frc.backend.tpi.flota.model.Transportista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Transportista.
 */
@Repository
public interface TransportistaRepository extends JpaRepository<Transportista, Long> {

    /**
     * Busca un transportista por DNI.
     */
    Optional<Transportista> findByDni(String dni);

    /**
     * Busca un transportista por email.
     */
    Optional<Transportista> findByEmail(String email);

    /**
     * Busca un transportista por número de licencia.
     */
    Optional<Transportista> findByNumeroLicencia(String numeroLicencia);

    /**
     * Verifica si existe un transportista con el DNI dado.
     */
    boolean existsByDni(String dni);

    /**
     * Verifica si existe un transportista con el email dado.
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un transportista con el número de licencia dado.
     */
    boolean existsByNumeroLicencia(String numeroLicencia);

    /**
     * Busca todos los transportistas activos o inactivos.
     */
    List<Transportista> findByActivo(Boolean activo);
}
