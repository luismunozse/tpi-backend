package ar.edu.utn.frc.backend.tpi.flota.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frc.backend.tpi.flota.model.Camion;
import ar.edu.utn.frc.backend.tpi.flota.model.EstadoCamion;

public interface CamionRepository extends JpaRepository<Camion, Long> {

    List<Camion> findByEstado(EstadoCamion estado);
}

