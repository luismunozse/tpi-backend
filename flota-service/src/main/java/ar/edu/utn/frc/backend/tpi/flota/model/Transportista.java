package ar.edu.utn.frc.backend.tpi.flota.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Transportista que representa a la persona encargada de manejar los camiones.
 * Un transportista puede tener múltiples camiones asignados.
 */
@Entity
@Table(name = "transportistas")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transportista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String dni;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false, unique = true)
    private String numeroLicencia;

    @Column(nullable = false)
    private String categoriaLicencia; // A, B, C, D, E

    @Column(nullable = false)
    private Boolean activo;

    @Builder.Default
    @OneToMany(mappedBy = "transportista", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Camion> camiones = new ArrayList<>();
}
