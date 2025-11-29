package ar.edu.utn.frc.backend.tpi.solicitudes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Solicitud;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    
}
