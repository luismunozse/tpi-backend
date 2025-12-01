package ar.edu.utn.frc.backend.tpi.flota.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    @Column(nullable = false)
    private String nombreTransportista;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private Double capacidadPesoKg;

    @Column(nullable = false)
    private Double capacidadVolumenM3;

    @Column(nullable = false)
    private Double consumoCombustibleLitrosKm;

    @Column(nullable = false)
    private Double costoBaseKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCamion estado;
}