package ar.edu.utn.frc.backend.tpi.flota.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frc.backend.tpi.flota.model.Transportista;

public interface TransportistaRepository extends JpaRepository<Transportista, Long> {

    Optional<Transportista> findByDni(String dni);

    Optional<Transportista> findByEmail(String email);

    Optional<Transportista> findByNumeroLicencia(String numeroLicencia);
}
