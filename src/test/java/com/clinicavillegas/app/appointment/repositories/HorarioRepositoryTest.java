package com.clinicavillegas.app.appointment.repositories;

import com.clinicavillegas.app.JpaTestConfig;
import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.appointment.models.Dia;
import com.clinicavillegas.app.appointment.models.Horario;
import com.clinicavillegas.app.user.models.Sexo;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.models.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class HorarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private HorarioRepository horarioRepository;

    private Dentista dentista;

    @BeforeEach
    void setUp() {
        TipoDocumento tipoDocumento = entityManager.persist(
                TipoDocumento.builder()
                        .nombre("DNI")
                        .acronimo("DNI")
                        .estado(true)
                        .build()
        );

        Usuario usuario = entityManager.persist(
                Usuario.builder()
                        .nombres("Julio")
                        .apellidoPaterno("Sánchez")
                        .apellidoMaterno("Huerta")
                        .contrasena("clave789")
                        .correo("julio.sanchez@gmail.com")
                        .telefono("922333444")
                        .numeroIdentidad("11112222")
                        .tipoDocumento(tipoDocumento)
                        .sexo(Sexo.MASCULINO)
                        .fechaNacimiento(LocalDate.of(1985, 6, 15))
                        .estado(true)
                        .build()
        );

        dentista = entityManager.persist(
                Dentista.builder()
                        .nColegiatura("DENT001")
                        .especializacion("Endodoncia")
                        .estado(true)
                        .usuario(usuario)
                        .build()
        );

        Horario horario = entityManager.persist(
                Horario.builder()
                        .dia(Dia.LUNES)
                        .horaComienzo(LocalTime.of(9, 0))
                        .horaFin(LocalTime.of(13, 0))
                        .dentista(dentista)
                        .build()
        );
    }

    @Test
    void testGuardarHorario() {
        Horario nuevoHorario = Horario.builder()
                .dia(Dia.MARTES)
                .horaComienzo(LocalTime.of(14, 0))
                .horaFin(LocalTime.of(18, 0))
                .dentista(dentista)
                .build();

        Horario guardado = horarioRepository.save(nuevoHorario);

        assertNotNull(guardado.getId());
        assertEquals(Dia.MARTES, guardado.getDia());
        assertEquals(LocalTime.of(14, 0), guardado.getHoraComienzo());
        assertEquals(LocalTime.of(18, 0), guardado.getHoraFin());
    }

    @Test
    void testBuscarPorDentista() {
        List<Horario> horarios = horarioRepository.findByDentista(dentista);

        assertThat(horarios).hasSize(1);
        assertEquals(dentista.getId(), horarios.getFirst().getDentista().getId());
        assertEquals(Dia.LUNES, horarios.getFirst().getDia());
    }
}
