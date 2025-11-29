package ar.edu.utn.frc.backend.tpi.transportes.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Ruta es el recorrido estimado que se arma para una solicitud, compuesto por uno o mas tramos.
// Sobre esa ruta se calculan: 
// Distancia total (suma de distancias de tramos)
// Tiempo estimado
// Costo estimado 

// Una solicitud tiene una unica ruta asociada.
// Una ruta tiene varios tramos asociados.
// Cada tramo pertenece a una unica ruta.

@Entity
@Table(name = "rutas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(mappedBy = "ruta")
    private Solicitud solicitud;
    @Builder.Default
    @OneToMany(mappedBy = "ruta", cascade = CascadeType.ALL, orphanRemoval = true) // si un tramo se elimina de la ruta, se elimina de la BD
    private List<Tramo> tramos = new ArrayList<>();
    @Column(name = "distancia_total_km")
    private Double distanciaTotalKm;
    @Column(name = "tiempo_estimado_horas")
    private Double tiempoEstimadoHoras;
    @Column(name = "costo_estimado")
    private Double costoEstimado;
    
}
