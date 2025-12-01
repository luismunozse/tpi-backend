package ar.edu.utn.frc.backend.tpi.solicitudes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.utn.frc.backend.tpi.solicitudes.entity.Solicitud;
import ar.edu.utn.frc.backend.tpi.solicitudes.entity.EstadoSolicitud;

public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    List<Solicitud> findByClienteId(Long clienteId);

    List<Solicitud> findByEstado(EstadoSolicitud estado);

    List<Solicitud> findByEstadoNot(EstadoSolicitud estado);
}
