package ar.edu.utn.frc.backend.tpi.costos.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tarifas")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    // Costo base por kilómetro (aplicable al cálculo general)
    @Column(nullable = false)
    private Double costoBaseKm;

    // Valor del litro de combustible
    @Column(nullable = false)
    private Double valorCombustibleLitro;

    // Costo de estadía diaria en depósitos
    @Column(nullable = false)
    private Double costoEstadiaDiaria;

    // Velocidad promedio para estimar tiempo (km/h)
    @Column(nullable = false)
    private Double velocidadPromedioKmH;

    // Cargo fijo de gestión
    @Column(nullable = false)
    private Double costoGestionFijo;
}
