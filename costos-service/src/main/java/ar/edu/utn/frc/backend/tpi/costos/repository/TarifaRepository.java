package ar.edu.utn.frc.backend.tpi.costos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frc.backend.tpi.costos.model.Tarifa;

public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

}
