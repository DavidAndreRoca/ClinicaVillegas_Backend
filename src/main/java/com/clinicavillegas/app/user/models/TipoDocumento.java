package com.clinicavillegas.app.user.models;

import com.clinicavillegas.app.audit.AudityEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity(name = "tipos_documento")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TipoDocumento extends AudityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 15, nullable = false)
    private String acronimo;

    @Column(nullable = false)
    private boolean estado;
}
