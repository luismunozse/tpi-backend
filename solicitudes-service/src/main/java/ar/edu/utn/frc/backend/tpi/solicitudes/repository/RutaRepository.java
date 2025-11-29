package ar.edu.utn.frc.backend.tpi.solicitudes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Ruta;

public interface RutaRepository extends JpaRepository<Ruta, Long> {
    
}
