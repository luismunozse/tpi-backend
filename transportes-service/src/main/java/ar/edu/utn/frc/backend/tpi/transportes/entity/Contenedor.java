package ar.edu.utn.frc.backend.tpi.transportes.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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
@Table(name = "contenedores")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Contenedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String numeroSerie;
    @Column(nullable = false)
    private String tipo;
    @Column(nullable = false)
    private Double peso;
    @Column(nullable = false)
    private Double volumen;
    @Enumerated(EnumType.STRING)
    private EstadoContenedor estado;
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;    
}
