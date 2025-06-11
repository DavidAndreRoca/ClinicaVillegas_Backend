package com.clinicavillegas.app.chat.repositories;

import com.clinicavillegas.app.JpaTestConfig;
import com.clinicavillegas.app.chat.models.Comentario;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class ComentarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ComentarioRepository comentarioRepository;

    private Usuario usuario;
    private Comentario comentarioPadre;

    @BeforeEach
    void setUp() {
        TipoDocumento tipoDocumento = entityManager.persist(
                TipoDocumento.builder()
                        .nombre("DNI")
                        .acronimo("DNI")
                        .estado(true)
                        .build()
        );

        usuario = entityManager.persist(
                Usuario.builder()
                        .nombres("Ana")
                        .apellidoPaterno("García")
                        .apellidoMaterno("Pérez")
                        .contrasena("securepass123")
                        .correo("ana.garcia@example.com")
                        .telefono("987654321")
                        .numeroIdentidad("99887766")
                        .tipoDocumento(tipoDocumento)
                        .sexo(Sexo.FEMENINO)
                        .fechaNacimiento(LocalDate.of(1990, 3, 20))
                        .estado(true)
                        .build()
        );

        comentarioPadre = entityManager.persist(
                Comentario.builder()
                        .contenido("Este es un comentario principal.")
                        .usuario(usuario)
                        .build()
        );
    }

    @Test
    void testGuardarComentario() {
        Comentario nuevoComentario = Comentario.builder()
                .contenido("¡Excelente artículo!")
                .usuario(usuario)
                .build();

        Comentario guardado = comentarioRepository.save(nuevoComentario);

        assertNotNull(guardado.getId());
        assertEquals("¡Excelente artículo!", guardado.getContenido());
        assertEquals(usuario.getId(), guardado.getUsuario().getId());
        assertNull(guardado.getComentario()); // No es una respuesta a otro comentario
    }

    @Test
    void testGuardarRespuestaAComentario() {
        Comentario respuesta = Comentario.builder()
                .contenido("Estoy de acuerdo con tu punto.")
                .usuario(usuario)
                .comentario(comentarioPadre) // Es una respuesta al comentario padre
                .build();

        Comentario guardada = comentarioRepository.save(respuesta);

        assertNotNull(guardada.getId());
        assertEquals("Estoy de acuerdo con tu punto.", guardada.getContenido());
        assertEquals(usuario.getId(), guardada.getUsuario().getId());
        assertEquals(comentarioPadre.getId(), guardada.getComentario().getId());
    }

    @Test
    void testBuscarComentarioPorId() {
        Optional<Comentario> encontrado = comentarioRepository.findById(comentarioPadre.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(comentarioPadre.getContenido(), encontrado.get().getContenido());
        assertEquals(usuario.getId(), encontrado.get().getUsuario().getId());
    }

    @Test
    void testBuscarComentariosPorComentarioPadre() {
        entityManager.persist(
                Comentario.builder()
                        .contenido("Primera respuesta.")
                        .usuario(usuario)
                        .comentario(comentarioPadre)
                        .build()
        );
        entityManager.persist(
                Comentario.builder()
                        .contenido("Segunda respuesta al mismo comentario.")
                        .usuario(usuario)
                        .comentario(comentarioPadre)
                        .build()
        );

        List<Comentario> respuestas = comentarioRepository.findByComentario(comentarioPadre);

        assertThat(respuestas).hasSize(2);
        assertTrue(respuestas.stream().allMatch(c -> c.getComentario().getId().equals(comentarioPadre.getId())));
    }

    @Test
    void testActualizarComentario() {
        comentarioPadre.setContenido("Contenido del comentario padre actualizado.");
        Comentario actualizado = comentarioRepository.save(comentarioPadre);

        assertEquals("Contenido del comentario padre actualizado.", actualizado.getContenido());
    }

    @Test
    void testEliminarComentario() {
        Long idComentarioPadre = comentarioPadre.getId();
        comentarioRepository.deleteById(idComentarioPadre);
        Optional<Comentario> eliminado = comentarioRepository.findById(idComentarioPadre);

        assertFalse(eliminado.isPresent());
    }
}