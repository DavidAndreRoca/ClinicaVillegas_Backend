package com.clinicavillegas.app.appointment.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import com.clinicavillegas.app.JpaTestConfig;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(JpaTestConfig.class)
@ActiveProfiles("test")
public class TipoTratamientoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TipoTratamientoRepository tipoTratamientoRepository;

    private TipoTratamiento activo;

    @BeforeEach
    void setUp() {
        activo = TipoTratamiento.builder()
                .nombre("Fisioterapia")
                .estado(true)
                .build();

        TipoTratamiento inactivo = TipoTratamiento.builder()
                .nombre("Acupuntura")
                .estado(false)
                .build();

        entityManager.persist(activo);
        entityManager.persist(inactivo);
        entityManager.flush();
    }

    @Test
    void testGuardarTipoTratamiento() {
        TipoTratamiento nuevo = TipoTratamiento.builder()
                .nombre("Osteopatía")
                .estado(true)
                .build();

        TipoTratamiento guardado = tipoTratamientoRepository.save(nuevo);

        assertNotNull(guardado.getId());
        assertEquals("Osteopatía", guardado.getNombre());
        assertTrue(guardado.isEstado());
    }

    @Test
    void testObtenerTipoTratamientoPorId() {
        Optional<TipoTratamiento> encontrado = tipoTratamientoRepository.findById(activo.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(activo.getNombre(), encontrado.get().getNombre());
    }

    @Test
    void testActualizarTipoTratamiento() {
        activo.setNombre("Fisioterapia Avanzada");
        activo.setEstado(false);

        TipoTratamiento actualizado = tipoTratamientoRepository.save(activo);

        assertEquals("Fisioterapia Avanzada", actualizado.getNombre());
        assertFalse(actualizado.isEstado());
    }

    @Test
    void testEliminarTipoTratamiento() {
        tipoTratamientoRepository.delete(activo);

        Optional<TipoTratamiento> eliminado = tipoTratamientoRepository.findById(activo.getId());
        assertFalse(eliminado.isPresent());
    }

    @Test
    void cuandoSeBuscaPorEstadoVerdadero_entoncesDevuelveSoloActivos() {
        List<TipoTratamiento> activos = tipoTratamientoRepository.findByEstado(true);

        assertThat(activos).hasSize(1);
        assertThat(activos.getFirst().getNombre()).isEqualTo("Fisioterapia");
    }

    @Test
    void cuandoSeBuscaPorEstadoFalso_entoncesDevuelveSoloInactivos() {
        List<TipoTratamiento> inactivos = tipoTratamientoRepository.findByEstado(false);

        assertThat(inactivos).hasSize(1);
        assertThat(inactivos.getFirst().getNombre()).isEqualTo("Acupuntura");
    }

    @Test
    void cuandoSeBuscaPorEspecificacionDeNombre_entoncesDevuelveFiltrados() {
        Specification<TipoTratamiento> spec = (root, query, cb) ->
                cb.like(root.get("nombre"), "%terapia%");

        List<TipoTratamiento> resultados = tipoTratamientoRepository.findAll(spec);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.getFirst().getNombre()).isEqualTo("Fisioterapia");
    }

    @Test
    void cuandoSeBuscaPorEspecificacionDeEstado_entoncesDevuelveFiltrados() {
        Specification<TipoTratamiento> spec = (root, query, cb) ->
                cb.equal(root.get("estado"), true);

        List<TipoTratamiento> resultados = tipoTratamientoRepository.findAll(spec);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.getFirst().getNombre()).isEqualTo("Fisioterapia");
    }

    @Test
    void cuandoSeBuscaPorEspecificacionDeNombreYEstado_deberiaFiltrarPorNombreYEstado() {
        Specification<TipoTratamiento> spec = (root, query, cb) ->
                cb.and(
                        cb.like(root.get("nombre"), "%puntura%"),
                        cb.equal(root.get("estado"), false)
                );

        List<TipoTratamiento> resultados = tipoTratamientoRepository.findAll(spec);

        assertThat(resultados).hasSize(1);
        assertThat(resultados.getFirst().getNombre()).isEqualTo("Acupuntura");
    }

    @Test
    void testContarTipoTratamientos() {
        long count = tipoTratamientoRepository.count();
        assertEquals(2, count);
    }

    @Test
    void testExisteTipoTratamientoPorId() {
        boolean exists = tipoTratamientoRepository.existsById(activo.getId());
        assertTrue(exists);
    }
}
