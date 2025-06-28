package com.clinicavillegas.app.appointment.repositories;

import com.clinicavillegas.app.JpaTestConfig;
import com.clinicavillegas.app.appointment.models.Dentista;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(JpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class DentistaRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DentistaRepository dentistaRepository;

    private Usuario usuario;
    private TipoDocumento tipoDocumento;
    private Dentista dentista;

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
                        .nombres("Luis")
                        .apellidoPaterno("Ramos")
                        .apellidoMaterno("Pérez")
                        .contrasena("clave123")
                        .correo("luis.ramos@gmail.com")
                        .telefono("999888777")
                        .numeroIdentidad("12345678")
                        .rol(Rol.DENTISTA)
                        .tipoDocumento(tipoDocumento)
                        .sexo(Sexo.MASCULINO)
                        .fechaNacimiento(LocalDate.of(1990, 1, 1))
                        .estado(true)
                        .build()
        );

        dentista = entityManager.persist(
                Dentista.builder()
                        .nColegiatura("COD123")
                        .especializacion("Ortodoncia")
                        .estado(true)
                        .usuario(usuario)
                        .build()
        );
    }

    @Test
    void testGuardarDentista() {
        Usuario nuevoUsuario = entityManager.persist(
                Usuario.builder()
                        .nombres("Carla")
                        .apellidoPaterno("Lopez")
                        .apellidoMaterno("Silva")
                        .contrasena("clave456")
                        .correo("carla.lopez@gmail.com")
                        .telefono("911222333")
                        .numeroIdentidad("87654321")
                        .tipoDocumento(tipoDocumento)
                        .sexo(Sexo.FEMENINO)
                        .rol(Rol.DENTISTA)
                        .fechaNacimiento(LocalDate.of(1992, 3, 15))
                        .estado(true)
                        .build()
        );

        Dentista nuevoDentista = Dentista.builder()
                .nColegiatura("COD456")
                .especializacion("Implantología")
                .estado(true)
                .usuario(nuevoUsuario)
                .build();

        Dentista guardado = dentistaRepository.save(nuevoDentista);

        assertNotNull(guardado.getId());
        assertEquals("COD456", guardado.getNColegiatura());
        assertEquals("Implantología", guardado.getEspecializacion());
    }

    @Test
    void testBuscarPorUsuario() {
        Optional<Dentista> resultado = dentistaRepository.findByUsuario(usuario);

        assertTrue(resultado.isPresent());
        assertEquals(dentista.getId(), resultado.get().getId());
        assertEquals(usuario.getId(), resultado.get().getUsuario().getId());
    }

    @Test
    void testBuscarEspecializaciones() {
        List<String> especializaciones = dentistaRepository.findEspecializaciones();

        assertThat(especializaciones).isNotEmpty();
        assertThat(especializaciones).contains("Ortodoncia");
    }
}
