package com.clinicavillegas.app.appointment.models;

import com.clinicavillegas.app.audit.AudityEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Duration;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "tratamientos")
public class Tratamiento extends AudityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String descripcion;

    @Column(nullable = false)
    private BigDecimal costo;

    @Column(nullable = false)
    private Duration duracion;

    @Column(nullable = false)
    private boolean estado;

    private String imagenURL;

    @ManyToOne
    @JoinColumn(name = "tipo_tratamiento_id")
    private TipoTratamiento tipoTratamiento;

}