package com.clinicavillegas.app.appointment.models;

import com.clinicavillegas.app.audit.AudityEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "horarios")
public class Horario extends AudityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Dia dia;

    @Column(nullable = false)
    private LocalTime horaComienzo;

    @Column(nullable = false)
    private LocalTime horaFin;

    @ManyToOne
    @JoinColumn(name = "dentista_id")
    private Dentista dentista;

}
