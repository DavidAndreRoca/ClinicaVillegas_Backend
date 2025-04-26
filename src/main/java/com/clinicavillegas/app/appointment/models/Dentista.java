package com.clinicavillegas.app.appointment.models;

import com.clinicavillegas.app.audit.AudityEntity;
import com.clinicavillegas.app.user.models.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "dentistas")
public class Dentista extends AudityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 25, name = "n_colegiatura", nullable = false)
    private String nColegiatura;

    @Column(nullable = false)
    private boolean estado;

    @Column(length = 25, nullable = false)
    private String especializacion;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
}
