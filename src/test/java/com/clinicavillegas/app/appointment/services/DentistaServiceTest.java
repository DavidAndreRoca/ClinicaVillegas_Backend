package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.DentistaRequest;
import com.clinicavillegas.app.appointment.dto.response.DentistaResponse;
import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.appointment.models.Dia;
import com.clinicavillegas.app.appointment.models.Horario;
import com.clinicavillegas.app.appointment.repositories.DentistaRepository;
import com.clinicavillegas.app.appointment.repositories.HorarioRepository;
import com.clinicavillegas.app.appointment.services.impl.DefaultDentistaService;
import com.clinicavillegas.app.common.exceptions.ResourceNotFoundException;
import com.clinicavillegas.app.user.models.Rol;
import com.clinicavillegas.app.user.models.Sexo;
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DentistaServiceTest {

    private DefaultDentistaService dentistaService;

    @Mock
    private DentistaRepository dentistaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private HorarioRepository horarioRepository;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dentistaService = new DefaultDentistaService(dentistaRepository, usuarioRepository, horarioRepository);
    }

    @Test
    @DisplayName("Debe obtener todos los dentistas")
    void testObtenerTodosLosDentistas() {
        Dentista dentista1 = Dentista.builder()
                .id(1L)
                .nColegiatura("12345")
                .especializacion("Ortodoncia")
                .estado(true)
                .build();

        Dentista dentista2 = Dentista.builder()
                .id(2L)
                .nColegiatura("67890")
                .especializacion("Endodoncia")
                .estado(true)
                .build();

        when(dentistaRepository.findAll()).thenReturn(List.of(dentista1, dentista2));

        List<Dentista> dentistas = dentistaService.obtenerDentistas();

        assertNotNull(dentistas);
        assertEquals(2, dentistas.size());
        verify(dentistaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debe obtener un dentista por ID")
    void testObtenerDentistaPorId() {
        Dentista dentista = Dentista.builder()
                .id(1L)
                .nColegiatura("12345")
                .especializacion("Ortodoncia")
                .estado(true)
                .build();

        when(dentistaRepository.findById(1L)).thenReturn(Optional.of(dentista));

        Dentista resultado = dentistaService.obtenerDentista(1L);

        assertNotNull(resultado);
        assertEquals("12345", resultado.getNColegiatura());
        assertEquals("Ortodoncia", resultado.getEspecializacion());
        verify(dentistaRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Debe lanzar ResourceNotFoundException si no encuentra el dentista por ID")
    void testDentistaNoEncontrado() {
        // Simulamos que no hay un dentista con el ID dado (Optional vacío)
        when(dentistaRepository.findById(1L)).thenReturn(Optional.empty());

        // Verificamos que se lanza la excepción ResourceNotFoundException
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            dentistaService.obtenerDentista(1L);
        });

        assertEquals("Recurso del tipo 'com.clinicavillegas.app.appointment.models.Dentista' con ID '1' no fue encontrado", exception.getMessage());

        // Verificamos que se llamó a findById exactamente una vez
        verify(dentistaRepository, times(1)).findById(1L);
    }


    @Test
    @DisplayName("Debe agregar un nuevo dentista")
    void testAgregarDentista() {
        DentistaRequest request = DentistaRequest.builder()
                .nColegiatura("12345")
                .especializacion("Ortodoncia")
                .usuarioId(1L)
                .build();

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombres("Juan")
                .apellidoPaterno("Perez")
                .apellidoMaterno("Ramirez")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(dentistaRepository.save(any(Dentista.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> dentistaService.agregarDentista(request));
        verify(dentistaRepository, times(1)).save(any(Dentista.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al agregar un dentista con usuario inexistente")
    void testAgregarDentistaUsuarioNoEncontrado() {
        // Configuración del request
        DentistaRequest request = DentistaRequest.builder()
                .nColegiatura("12345")
                .especializacion("Ortodoncia")
                .usuarioId(1L)
                .build();

        // Simulamos que no se encuentra el usuario
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        // Verificamos que se lance NoSuchElementException
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> dentistaService.agregarDentista(request));

        // Opcional: verificar mensaje de la excepción si es necesario.
        assertEquals("Recurso del tipo 'com.clinicavillegas.app.user.models.Usuario' con ID '1' no fue encontrado", exception.getMessage());

        // Verificar interacciones del mock
        verify(usuarioRepository, times(1)).findById(1L);
        verify(dentistaRepository, never()).save(any(Dentista.class));
    }

    @Test
    @DisplayName("Debe eliminar un dentista correctamente")
    void testEliminarDentista() {
        // Configuración del objeto Usuario asociado al dentista
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombres("Juan")
                .apellidoPaterno("Perez")
                .apellidoMaterno("Ramirez")
                .rol(Rol.DENTISTA)
                .build();

        // Configuración del dentista con un usuario asociado
        Dentista dentista = Dentista.builder()
                .id(1L)
                .nColegiatura("12345")
                .especializacion("Ortodoncia")
                .estado(true)
                .usuario(usuario)
                .build();

        // Configuración de horarios asociados al dentista
        List<Horario> horarios = List.of(
                Horario.builder()
                        .id(1L)
                        .dia(Dia.LUNES)
                        .horaComienzo(LocalTime.of(9, 0))
                        .horaFin(LocalTime.of(12, 0))
                        .dentista(dentista)
                        .build()
        );

        // Configuración del mock del repositorio de dentistas
        when(dentistaRepository.findById(1L)).thenReturn(Optional.of(dentista));

        // Configuración del mock del repositorio de horarios
        when(horarioRepository.findByDentista(dentista)).thenReturn(horarios);

        // Llamada al método a probar
        dentistaService.eliminarDentista(1L);

        // Verificaciones
        verify(horarioRepository, times(1)).findByDentista(dentista);    // Verifica que se consultaron los horarios
        verify(horarioRepository, times(horarios.size())).delete(any(Horario.class)); // Verifica que se eliminaron todos los horarios
        verify(usuarioRepository, times(1)).save(usuario); // Verifica que se actualizó el rol del usuario
        verify(dentistaRepository, times(1)).delete(dentista); // Verifica que se eliminó el dentista
    }
    @Test
    @DisplayName("Debe lanzar excepción al intentar eliminar un dentista inexistente")
    void testEliminarDentistaNoEncontrado() {
        // Configuramos el mock para devolver un Optional vacío
        when(dentistaRepository.findById(1L)).thenReturn(Optional.empty());

        // Verificamos que la excepción lanzada sea del tipo NoSuchElementException
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> dentistaService.eliminarDentista(1L));

        // Validamos que el mensaje de la excepción sea el esperado (si aplica lógica específica)
        assertEquals("Recurso del tipo 'com.clinicavillegas.app.appointment.models.Dentista' con ID '1' no fue encontrado", exception.getMessage());

        // Verificamos que se llamó findById en dentistaRepository
        verify(dentistaRepository, times(1)).findById(1L);

        // Verificamos que nunca se intentó eliminar un dentista
        verify(dentistaRepository, never()).delete(any(Dentista.class));
    }
    @Test
    @DisplayName("Debe actualizar un dentista correctamente")
    void testActualizarDentista() {
        // Configuración del usuario anterior asociado al dentista original
        Usuario usuarioAnterior = Usuario.builder()
                .id(1L)
                .nombres("Pedro")
                .apellidoPaterno("Perez")
                .apellidoMaterno("Ramirez")
                .rol(Rol.DENTISTA) // Rol inicial
                .build();

        // Configuración del usuario nuevo a asociar al dentista
        Usuario usuarioActual = Usuario.builder()
                .id(20L)
                .nombres("Juan")
                .apellidoPaterno("Gomez")
                .apellidoMaterno("Lopez")
                .rol(Rol.PACIENTE) // Rol del nuevo usuario
                .build();

        // Configuración del dentista original
        Dentista dentista = Dentista.builder()
                .id(1L)
                .nColegiatura("NC12345")
                .especializacion("Ortodoncia")
                .estado(true)
                .usuario(usuarioAnterior)
                .build();

        // Configuración del request para actualizar el dentista
        DentistaRequest request = DentistaRequest.builder()
                .nColegiatura("NC54321")
                .especializacion("Cirugía Maxilofacial")
                .usuarioId(usuarioActual.getId())
                .build();

        // Mock del repositorio de dentistas: encontrar dentista por ID
        when(dentistaRepository.findById(1L)).thenReturn(Optional.of(dentista));

        // Mock del repositorio de usuarios: encontrar el nuevo usuario por ID
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(usuarioActual));

        // Llamada al método a probar
        dentistaService.actualizarDentista(1L, request);

        // Verificaciones: Cambios en el usuario anterior (rol cambia a PACIENTE)
        verify(usuarioRepository, times(1)).save(usuarioAnterior);
        assertEquals(Rol.PACIENTE, usuarioAnterior.getRol());

        // Verificaciones: Configuración del usuario actual asociado al dentista
        verify(usuarioRepository, times(1)).findById(20L);
        assertEquals(usuarioActual, dentista.getUsuario());
        assertEquals("NC54321", dentista.getNColegiatura());
        assertEquals("Cirugía Maxilofacial", dentista.getEspecializacion());

        // Verificación: Guardar el dentista actualizado
        verify(dentistaRepository, times(1)).save(dentista);
    }
    @Test
    @DisplayName("Debe obtener una lista de dentistas filtrados correctamente")
    void testObtenerDentistasFiltrados() {
        // Configuración del usuario con fechaNacimiento válida
        Usuario usuario = Usuario.builder()
                .id(10L)
                .nombres("Carlos")
                .apellidoPaterno("Gómez")
                .apellidoMaterno("Fernández")
                .correo("carlos.gomez@mail.com")
                .telefono("999888777")
                .sexo(Sexo.valueOf("MASCULINO"))
                .fechaNacimiento(LocalDate.of(1990, 7, 15)) // Fecha válida
                .build();

        // Configuración del dentista
        Dentista dentista = Dentista.builder()
                .id(1L)
                .nColegiatura("NC12345")
                .estado(true)
                .especializacion("Ortodoncia")
                .usuario(usuario)
                .build();

        // Mock del repositorio: devolver una lista de dentistas
        when(dentistaRepository.findAll(any(Specification.class))).thenReturn(List.of(dentista));

        // Llamada al método
        List<DentistaResponse> dentistas = dentistaService.obtenerDentistas("Carlos", "Ortodoncia", 10L);

        // Verificaciones: Validar los resultados
        assertNotNull(dentistas);
        assertEquals(1, dentistas.size());

        // Validar la primera respuesta
        DentistaResponse response = dentistas.get(0);
        assertEquals(1L, response.getId());
        assertEquals("NC12345", response.getNColegiatura());
        assertEquals("Carlos", response.getNombres());
        assertEquals("Gómez", response.getApellidoPaterno());
        assertEquals("Fernández", response.getApellidoMaterno());
        assertEquals("Ortodoncia", response.getEspecializacion());
        assertTrue(response.isEstado());
        assertEquals(10L, response.getUsuarioId());
        assertEquals("carlos.gomez@mail.com", response.getCorreo());
        assertEquals("999888777", response.getTelefono());
        assertEquals("MASCULINO", response.getSexo());
        assertEquals("1990-07-15", response.getFechaNacimiento()); // Verifica el valor de fechaNacimiento

        // Verificar interacciones
        verify(dentistaRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Debe obtener una lista de especialidades de dentistas")
    void testObtenerEspecialidades() {
        // Lista de especialidades simuladas
        List<String> especialidadesMock = List.of("Ortodoncia", "Endodoncia", "Cirugía Maxilofacial");

        // Configurar el mock para el repositorio
        when(dentistaRepository.findEspecializaciones()).thenReturn(especialidadesMock);

        // Llamada al método a probar
        List<String> especialidades = dentistaService.obtenerEspecialidades();

        // Validaciones
        assertNotNull(especialidades);
        assertEquals(3, especialidades.size());
        assertEquals("Ortodoncia", especialidades.get(0));
        assertEquals("Endodoncia", especialidades.get(1));
        assertEquals("Cirugía Maxilofacial", especialidades.get(2));

        // Verificar que el método del repositorio fue invocado
        verify(dentistaRepository, times(1)).findEspecializaciones();
    }
}