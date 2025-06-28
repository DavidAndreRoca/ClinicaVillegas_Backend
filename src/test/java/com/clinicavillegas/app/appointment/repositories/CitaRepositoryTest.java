package com.clinicavillegas.app.appointment.repositories;

import com.clinicavillegas.app.JpaTestConfig;
import com.clinicavillegas.app.appointment.models.*;
import com.clinicavillegas.app.user.models.Rol;
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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class CitaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CitaRepository citaRepository;

    private Usuario usuario;
    private Dentista dentista;
    private Tratamiento tratamiento;
    private TipoDocumento tipoDocumento;

    private Cita cita;

    @BeforeEach
    void setUp() {
        tipoDocumento = entityManager.persist(
                TipoDocumento.builder().nombre("DNI").acronimo("DNI").estado(true).build()
        );

        usuario = entityManager.persist(
                Usuario.builder()
                        .nombres("Luis")
                        .apellidoPaterno("Ramos")
                        .apellidoMaterno("Pérez")
                        .rol(Rol.DENTISTA)
                        .contrasena("jbyuy$vty%4f")
                        .correo("lorem@gmail.com")
                        .telefono("987654321")
                        .numeroIdentidad("12345678")
                        .tipoDocumento(tipoDocumento)
                        .sexo(Sexo.MASCULINO)
                        .fechaNacimiento(LocalDate.of(1990, 1, 1))
                        .estado(true)
                        .build()
        );

        dentista = entityManager.persist(
                Dentista.builder()
                        .usuario(usuario)
                        .nColegiatura("COD123")
                        .especializacion("Cirugía dental")
                        .estado(true)
                        .build()
        );

        tratamiento = entityManager.persist(
                Tratamiento.builder()
                        .nombre("Limpieza Dental")
                        .costo(BigDecimal.valueOf(80.00))
                        .duracion(Duration.ofMinutes(30))
                        .estado(true)
                        .build()
        );

        cita = entityManager.persist(
                Cita.builder()
                        .estado("Pendiente")
                        .monto(BigDecimal.valueOf(80.00))
                        .fecha(LocalDate.now())
                        .hora(LocalTime.of(10, 30))
                        .nombres("Luis")
                        .apellidoPaterno("Ramos")
                        .apellidoMaterno("Pérez")
                        .tipoDocumento(tipoDocumento)
                        .numeroIdentidad("12345678")
                        .sexo(Sexo.MASCULINO)
                        .fechaNacimiento(LocalDate.of(1990, 1, 1))
                        .usuario(usuario)
                        .tratamiento(tratamiento)
                        .dentista(dentista)
                        .build()
        );
    }

    @Test
    void testGuardarCita() {
        Cita nuevaCita = Cita.builder()
                .estado("Atendida")
                .monto(BigDecimal.valueOf(100))
                .fecha(LocalDate.now())
                .hora(LocalTime.of(9, 0))
                .nombres("Ana")
                .apellidoPaterno("Lopez")
                .apellidoMaterno("Martinez")
                .tipoDocumento(tipoDocumento)
                .numeroIdentidad("87654321")
                .sexo(Sexo.FEMENINO)
                .fechaNacimiento(LocalDate.of(1995, 5, 10))
                .usuario(usuario)
                .tratamiento(tratamiento)
                .dentista(dentista)
                .build();

        Cita guardada = citaRepository.save(nuevaCita);

        assertNotNull(guardada.getId());
        assertEquals("Ana", guardada.getNombres());
    }

    @Test
    void testBuscarPorUsuario() {
        List<Cita> citas = citaRepository.findByUsuario(usuario);

        assertThat(citas).hasSize(1);
        assertEquals(cita.getId(), citas.getFirst().getId());
    }

    @Test
    void testBuscarPorDentista() {
        List<Cita> citas = citaRepository.findByDentista(dentista);

        assertThat(citas).hasSize(1);
        assertEquals(cita.getDentista().getId(), dentista.getId());
    }

    @Test
    void testBuscarPorFecha() {
        List<Cita> citas = citaRepository.findByFecha(LocalDate.now());

        assertThat(citas).hasSize(1);
        assertEquals(LocalDate.now(), citas.getFirst().getFecha());
    }
}