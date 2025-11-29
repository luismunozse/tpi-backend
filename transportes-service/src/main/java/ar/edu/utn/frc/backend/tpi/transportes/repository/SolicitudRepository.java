package ar.edu.utn.frc.backend.tpi.transportes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frc.backend.tpi.transportes.entity.Solicitud;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {
    
}
