package ar.edu.utn.frc.backend.tpi.transportes.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "solicitudes")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente; //Cliente que realiza la solicitud
    @OneToOne(optional = false)
    @JoinColumn(name = "contenedor_id", nullable = false, unique = true)
    private Contenedor contenedor; //Contenedor a trasladar
    @OneToOne
    @JoinColumn(name = "ruta_id")
    private Ruta ruta; // Ruta asignada a la solicitud puede ser nula si no fue asignada aún
    @Column(name = "origen_direccion", nullable = false)
    private String origenDireccion;
    @Column(name = "origen_latitud")
    private Double origenLatitud;
    @Column(name = "origen_longitud")
    private Double origenLongitud;
    @Column(name = "destino_direccion", nullable = false)
    private String destinoDireccion;
    @Column(name = "destino_latitud")
    private Double destinoLatitud;
    @Column(name = "destino_longitud")
    private Double destinoLongitud;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado; // PENDIENTE, PROGRAMADA, EN_TRANSITO, FINALIZADA
    @Column(name = "costo_estimado")
    private Double costoEstimado; // Calculado al momento de crear la solicitud
    @Column(name = "costo_final")
    private Double costoFinal; // Calculado al finalizar la solicitud
    @Column(name = "tiempo_estimado_horas")
    private Double tiempoEstimadoHoras; // Calculado al momento de crear la solicitud
    @Column(name = "tiempo_real_horas")
    private Double tiempoRealHoras; // Calculado al finalizar la solicitud
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;// Fecha y hora en que se creó la solicitud

    
}
