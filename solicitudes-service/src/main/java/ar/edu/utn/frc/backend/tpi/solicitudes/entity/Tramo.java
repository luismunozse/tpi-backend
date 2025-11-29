package ar.edu.utn.frc.backend.tpi.solicitudes.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tramos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;
    @Column(nullable = false)
    private String origen;
    @Column(nullable = false)
    private String destino;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoTramo tipo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTramo estado;
    @Column(name = "costo_estimado")
    private Double costoEstimado;
    @Column(name = "costo_real")
    private Double costoReal;
    @Column(name = "fecha_hora_inicio")
    private LocalDateTime fechaHoraInicio;
    @Column(name = "fecha_hora_fin")
    private LocalDateTime fechaHoraFin;
    @Column(name = "camion_id")
    private Long camionId;
    
}
