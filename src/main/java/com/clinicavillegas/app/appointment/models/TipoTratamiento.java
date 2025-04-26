package com.clinicavillegas.app.appointment.models;

import com.clinicavillegas.app.audit.AudityEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "tipos_tratamiento")
public class TipoTratamiento extends AudityEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 60, nullable = false)
    private String nombre;

    @Column(nullable = false)
    private boolean estado;

}