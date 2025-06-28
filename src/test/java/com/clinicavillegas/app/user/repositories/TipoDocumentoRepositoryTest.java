package com.clinicavillegas.app.user.repositories;

import com.clinicavillegas.app.JpaTestConfig;
import com.clinicavillegas.app.user.models.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class TipoDocumentoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TipoDocumentoRepository tipoDocumentoRepository;

    private TipoDocumento tipoDocumentoDNI;

    @BeforeEach
    void setUp() {
        tipoDocumentoDNI = entityManager.persist(
                TipoDocumento.builder()
                        .nombre("DNI")
                        .acronimo("DNI")
                        .estado(true)
                        .build()
        );
    }

    @Test
    void testGuardarTipoDocumento() {
        TipoDocumento nuevoTipoDocumento = TipoDocumento.builder()
                .nombre("Pasaporte")
                .acronimo("PAS")
                .estado(true)
                .build();

        TipoDocumento guardado = tipoDocumentoRepository.save(nuevoTipoDocumento);

        assertNotNull(guardado.getId());
        assertEquals("Pasaporte", guardado.getNombre());
        assertEquals("PAS", guardado.getAcronimo());
        assertTrue(guardado.isEstado());
    }

    @Test
    void testBuscarTipoDocumentoPorId() {
        Optional<TipoDocumento> encontrado = tipoDocumentoRepository.findById(tipoDocumentoDNI.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(tipoDocumentoDNI.getNombre(), encontrado.get().getNombre());
        assertEquals(tipoDocumentoDNI.getAcronimo(), encontrado.get().getAcronimo());
    }

    @Test
    void testActualizarTipoDocumento() {
        tipoDocumentoDNI.setEstado(false);
        tipoDocumentoDNI.setAcronimo("DOC_IDENT");
        TipoDocumento actualizado = tipoDocumentoRepository.save(tipoDocumentoDNI);

        assertFalse(actualizado.isEstado());
        assertEquals("DOC_IDENT", actualizado.getAcronimo());
    }

    @Test
    void testEliminarTipoDocumento() {
        Long idTipoDocumento = tipoDocumentoDNI.getId();
        tipoDocumentoRepository.deleteById(idTipoDocumento);
        Optional<TipoDocumento> eliminado = tipoDocumentoRepository.findById(idTipoDocumento);

        assertFalse(eliminado.isPresent());
    }

    @Test
    void testBuscarTodosLosTiposDocumento() {
        entityManager.persist(
                TipoDocumento.builder()
                        .nombre("Carnet de Extranjería")
                        .acronimo("CE")
                        .estado(true)
                        .build()
        );

        Iterable<TipoDocumento> tiposDocumento = tipoDocumentoRepository.findAll();
        assertThat(tiposDocumento).hasSize(2);
    }
}