package com.clinicavillegas.app.appointment.repositories;

import com.clinicavillegas.app.JpaTestConfig;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.models.Tratamiento;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class TratamientoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TratamientoRepository tratamientoRepository;

    private TipoTratamiento tipoTratamiento;
    private Tratamiento tratamiento;

    @BeforeEach
    void setUp() {
        tipoTratamiento = entityManager.persist(
                TipoTratamiento.builder()
                        .nombre("Limpieza Dental")
                        .estado(true)
                        .build()
        );

        tratamiento = entityManager.persist(
                Tratamiento.builder()
                        .nombre("Blanqueamiento Dental")
                        .descripcion("Procedimiento para aclarar el tono de los dientes.")
                        .costo(new BigDecimal("300.00"))
                        .duracion(Duration.ofHours(1))
                        .estado(true)
                        .imagenURL("https://example.com/blanqueamiento.jpg")
                        .tipoTratamiento(tipoTratamiento)
                        .build()
        );
    }

    @Test
    void testGuardarTratamiento() {
        Tratamiento nuevoTratamiento = Tratamiento.builder()
                .nombre("Extracción Simple")
                .descripcion("Extracción de una pieza dental no compleja.")
                .costo(new BigDecimal("150.00"))
                .duracion(Duration.ofMinutes(30))
                .estado(true)
                .imagenURL("https://example.com/extraccion.jpg")
                .tipoTratamiento(tipoTratamiento)
                .build();

        Tratamiento guardado = tratamientoRepository.save(nuevoTratamiento);

        assertNotNull(guardado.getId());
        assertEquals("Extracción Simple", guardado.getNombre());
        assertEquals(new BigDecimal("150.00"), guardado.getCosto());
        assertEquals(Duration.ofMinutes(30), guardado.getDuracion());
        assertTrue(guardado.isEstado());
        assertEquals(tipoTratamiento.getId(), guardado.getTipoTratamiento().getId());
    }

    @Test
    void testBuscarTratamientoPorId() {
        Optional<Tratamiento> encontrado = tratamientoRepository.findById(tratamiento.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(tratamiento.getNombre(), encontrado.get().getNombre());
        assertEquals(tratamiento.getCosto(), encontrado.get().getCosto());
    }

    @Test
    void testActualizarTratamiento() {
        tratamiento.setCosto(new BigDecimal("350.00"));
        tratamiento.setDuracion(Duration.ofMinutes(75));
        Tratamiento actualizado = tratamientoRepository.save(tratamiento);

        assertEquals(new BigDecimal("350.00"), actualizado.getCosto());
        assertEquals(Duration.ofMinutes(75), actualizado.getDuracion());
    }

    @Test
    void testEliminarTratamiento() {
        tratamientoRepository.deleteById(tratamiento.getId());
        Optional<Tratamiento> eliminado = tratamientoRepository.findById(tratamiento.getId());

        assertFalse(eliminado.isPresent());
    }

    @Test
    void testBuscarTodosLosTratamientos() {
        entityManager.persist(
                Tratamiento.builder()
                        .nombre("Endodoncia")
                        .descripcion("Tratamiento de conducto radicular.")
                        .costo(new BigDecimal("500.00"))
                        .duracion(Duration.ofHours(2))
                        .estado(true)
                        .imagenURL("https://example.com/endodoncia.jpg")
                        .tipoTratamiento(tipoTratamiento)
                        .build()
        );

        Iterable<Tratamiento> tratamientos = tratamientoRepository.findAll();
        assertThat(tratamientos).hasSize(2);
    }
}