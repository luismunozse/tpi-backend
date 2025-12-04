package ar.edu.utn.frc.backend.tpi.flota.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "camiones")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Camion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String dominio; // patente o identificador

    // Relación con Transportista (reemplaza a nombreTransportista y telefono)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id")
    private Transportista transportista;

    // Campos legacy para compatibilidad (deprecated)
    @Deprecated
    @Column(name = "nombre_transportista")
    private String nombreTransportista;

    @Deprecated
    @Column(name = "telefono")
    private String telefono;

    @Column(name = "capacidad_peso_kg", nullable = false)
    private Double capacidadPesoKg;

    @Column(name = "capacidad_volumen_m3", nullable = false)
    private Double capacidadVolumenM3;

    @Column(name = "consumo_combustible_litros_km", nullable = false)
    private Double consumoCombustibleLitrosKm;

    @Column(name = "costo_base_km", nullable = false)
    private Double costoBaseKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCamion estado;
}