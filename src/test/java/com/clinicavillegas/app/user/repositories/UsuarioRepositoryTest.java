package com.clinicavillegas.app.user.repositories;

import com.clinicavillegas.app.JpaTestConfig;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private TipoDocumento tipoDocumento;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        tipoDocumento = entityManager.persist(
                TipoDocumento.builder()
                        .nombre("DNI")
                        .acronimo("DNI")
                        .estado(true)
                        .build()
        );

        usuario = entityManager.persist(
                Usuario.builder()
                        .nombres("Pedro")
                        .apellidoPaterno("Martínez")
                        .apellidoMaterno("Soto")
                        .contrasena("password123")
                        .correo("pedro.martinez@example.com")
                        .telefono("999888777")
                        .numeroIdentidad("12345678")
                        .tipoDocumento(tipoDocumento)
                        .sexo(Sexo.MASCULINO)
                        .fechaNacimiento(LocalDate.of(1992, 1, 25))
                        .estado(true)
                        .rol(Rol.PACIENTE)
                        .build()
        );
    }

    @Test
    void testGuardarUsuario() {
        Usuario nuevoUsuario = Usuario.builder()
                .nombres("Laura")
                .apellidoPaterno("Gómez")
                .apellidoMaterno("Rojas")
                .contrasena("newpass456")
                .correo("laura.gomez@example.com")
                .telefono("911222333")
                .numeroIdentidad("87654321")
                .tipoDocumento(tipoDocumento)
                .sexo(Sexo.FEMENINO)
                .fechaNacimiento(LocalDate.of(1988, 7, 10))
                .estado(true)
                .rol(Rol.DENTISTA)
                .build();

        Usuario guardado = usuarioRepository.save(nuevoUsuario);

        assertNotNull(guardado.getId());
        assertEquals("Laura", guardado.getNombres());
        assertEquals("laura.gomez@example.com", guardado.getCorreo());
        assertEquals(Rol.DENTISTA, guardado.getRol());
    }

    @Test
    void testBuscarUsuarioPorId() {
        Optional<Usuario> encontrado = usuarioRepository.findById(usuario.getId());

        assertTrue(encontrado.isPresent());
        assertEquals(usuario.getNombres(), encontrado.get().getNombres());
        assertEquals(usuario.getCorreo(), encontrado.get().getCorreo());
    }

    @Test
    void testBuscarUsuarioPorCorreo() {
        Optional<Usuario> encontrado = usuarioRepository.findByCorreo("pedro.martinez@example.com");

        assertTrue(encontrado.isPresent());
        assertEquals(usuario.getId(), encontrado.get().getId());
        assertEquals(usuario.getNombres(), encontrado.get().getNombres());
    }

    @Test
    void testActualizarUsuario() {
        usuario.setTelefono("900111222");
        usuario.setEstado(false);
        Usuario actualizado = usuarioRepository.save(usuario);

        assertEquals("900111222", actualizado.getTelefono());
        assertFalse(actualizado.isEstado());
    }

    @Test
    void testEliminarUsuario() {
        Long idUsuario = usuario.getId();
        usuarioRepository.deleteById(idUsuario);
        Optional<Usuario> eliminado = usuarioRepository.findById(idUsuario);

        assertFalse(eliminado.isPresent());
    }

    @Test
    void testBuscarTodosLosUsuarios() {
        entityManager.persist(
                Usuario.builder()
                        .nombres("Roberto")
                        .apellidoPaterno("Díaz")
                        .apellidoMaterno("Castro")
                        .contrasena("passroberto")
                        .correo("roberto.diaz@example.com")
                        .telefono("933444555")
                        .numeroIdentidad("22334455")
                        .tipoDocumento(tipoDocumento)
                        .sexo(Sexo.MASCULINO)
                        .fechaNacimiento(LocalDate.of(1975, 10, 1))
                        .estado(true)
                        .rol(Rol.ADMINISTRADOR)
                        .build()
        );

        Iterable<Usuario> usuarios = usuarioRepository.findAll();
        assertThat(usuarios).hasSize(2);
    }
}