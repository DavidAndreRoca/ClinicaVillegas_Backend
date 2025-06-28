package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.DentistaRequest;
import com.clinicavillegas.app.appointment.dto.response.DentistaResponse;
import com.clinicavillegas.app.appointment.mappers.DentistaMapper; // Podría ser útil si usas el mapper directamente en el test para crear respuestas esperadas.
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
import org.springframework.data.domain.Page; // ¡Nuevo! Importar Page
import org.springframework.data.domain.PageImpl; // ¡Nuevo! Importar PageImpl
import org.springframework.data.domain.PageRequest; // ¡Nuevo! Importar PageRequest
import org.springframework.data.domain.Pageable; // ¡Nuevo! Importar Pageable
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException; // Mantener si otras partes del código aún pueden lanzarla, aunque ResourceNotFoundException es más específica.
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
    @DisplayName("Debe obtener todos los dentistas (sin filtros, método obsoleto si solo se usa el paginado)")
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

        // Si el método obtenerDentistas() sin parámetros ya no existe o llama al paginado
        // este test podría necesitar ajustarse para reflejar el cambio en la firma.
        // Asumiendo que obtenerDentistas() sin parámetros *llama* a findAll(), este es correcto.
        when(dentistaRepository.findAll()).thenReturn(List.of(dentista1, dentista2));

        List<Dentista> dentistas = dentistaService.obtenerDentistas(); // Este test es para el método que devuelve List<Dentista>

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
        when(dentistaRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            dentistaService.obtenerDentista(1L);
        });

        assertEquals("Recurso del tipo 'com.clinicavillegas.app.appointment.models.Dentista' con ID '1' no fue encontrado", exception.getMessage());

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
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Mockear el save del usuario

        assertDoesNotThrow(() -> dentistaService.agregarDentista(request));
        verify(usuarioRepository, times(1)).save(any(Usuario.class)); // Verificar que se guardó el usuario
        verify(dentistaRepository, times(1)).save(any(Dentista.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al agregar un dentista con usuario inexistente")
    void testAgregarDentistaUsuarioNoEncontrado() {
        DentistaRequest request = DentistaRequest.builder()
                .nColegiatura("12345")
                .especializacion("Ortodoncia")
                .usuarioId(1L)
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> dentistaService.agregarDentista(request));

        assertEquals("Recurso del tipo 'com.clinicavillegas.app.user.models.Usuario' con ID '1' no fue encontrado", exception.getMessage());

        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, never()).save(any(Usuario.class)); // Verificar que no se guardó el usuario
        verify(dentistaRepository, never()).save(any(Dentista.class));
    }

    @Test
    @DisplayName("Debe eliminar un dentista correctamente")
    void testEliminarDentista() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombres("Juan")
                .apellidoPaterno("Perez")
                .apellidoMaterno("Ramirez")
                .rol(Rol.DENTISTA)
                .build();

        Dentista dentista = Dentista.builder()
                .id(1L)
                .nColegiatura("12345")
                .especializacion("Ortodoncia")
                .estado(true)
                .usuario(usuario)
                .build();

        List<Horario> horarios = List.of(
                Horario.builder()
                        .id(1L)
                        .dia(Dia.LUNES)
                        .horaComienzo(LocalTime.of(9, 0))
                        .horaFin(LocalTime.of(12, 0))
                        .dentista(dentista)
                        .build()
        );

        when(dentistaRepository.findById(1L)).thenReturn(Optional.of(dentista));
        when(horarioRepository.findByDentista(dentista)).thenReturn(horarios);

        dentistaService.eliminarDentista(1L);

        verify(horarioRepository, times(1)).findByDentista(dentista);
        verify(horarioRepository, times(horarios.size())).delete(any(Horario.class));
        verify(usuarioRepository, times(1)).save(usuario);
        assertEquals(Rol.PACIENTE, usuario.getRol()); // Asegurarse de que el rol se actualizó en el objeto
        verify(dentistaRepository, times(1)).delete(dentista);
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar eliminar un dentista inexistente")
    void testEliminarDentistaNoEncontrado() {
        when(dentistaRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> dentistaService.eliminarDentista(1L));

        assertEquals("Recurso del tipo 'com.clinicavillegas.app.appointment.models.Dentista' con ID '1' no fue encontrado", exception.getMessage());

        verify(dentistaRepository, times(1)).findById(1L);
        verify(dentistaRepository, never()).delete(any(Dentista.class));
    }

    @Test
    @DisplayName("Debe actualizar un dentista correctamente")
    void testActualizarDentista() {
        Usuario usuarioAnterior = Usuario.builder()
                .id(1L)
                .nombres("Pedro")
                .apellidoPaterno("Perez")
                .apellidoMaterno("Ramirez")
                .rol(Rol.DENTISTA)
                .build();

        Usuario usuarioActual = Usuario.builder()
                .id(20L)
                .nombres("Juan")
                .apellidoPaterno("Gomez")
                .apellidoMaterno("Lopez")
                .rol(Rol.PACIENTE) // Este rol se cambia a DENTISTA en el servicio
                .build();

        Dentista dentista = Dentista.builder()
                .id(1L)
                .nColegiatura("NC12345")
                .especializacion("Ortodoncia")
                .estado(true)
                .usuario(usuarioAnterior)
                .build();

        DentistaRequest request = DentistaRequest.builder()
                .nColegiatura("NC54321")
                .especializacion("Cirugía Maxilofacial")
                .usuarioId(usuarioActual.getId())
                .build();

        when(dentistaRepository.findById(1L)).thenReturn(Optional.of(dentista));
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(usuarioActual));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Mockear el save de usuario
        when(dentistaRepository.save(any(Dentista.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Mockear el save de dentista

        dentistaService.actualizarDentista(1L, request);

        verify(usuarioRepository, times(1)).save(usuarioAnterior); // Guarda el usuario anterior con rol PACIENTE
        assertEquals(Rol.PACIENTE, usuarioAnterior.getRol());

        verify(usuarioRepository, times(1)).findById(20L); // Busca el nuevo usuario
        verify(usuarioRepository, times(1)).save(usuarioActual); // Guarda el nuevo usuario con rol DENTISTA
        assertEquals(Rol.DENTISTA, usuarioActual.getRol()); // Asegurarse de que el rol del nuevo usuario se actualizó

        assertEquals(usuarioActual, dentista.getUsuario());
        assertEquals("NC54321", dentista.getNColegiatura());
        assertEquals("Cirugía Maxilofacial", dentista.getEspecializacion());

        verify(dentistaRepository, times(1)).save(dentista);
    }

    @Test
    @DisplayName("Debe obtener una lista de dentistas filtrados correctamente (sin paginación)")
    void testObtenerDentistasFiltrados() {
        Usuario usuario = Usuario.builder()
                .id(10L)
                .nombres("Carlos")
                .apellidoPaterno("Gómez")
                .apellidoMaterno("Fernández")
                .correo("carlos.gomez@mail.com")
                .telefono("999888777")
                .sexo(Sexo.MASCULINO) // Usar el enum directamente
                .fechaNacimiento(LocalDate.of(1990, 7, 15))
                .build();

        Dentista dentista = Dentista.builder()
                .id(1L)
                .nColegiatura("NC12345")
                .estado(true)
                .especializacion("Ortodoncia")
                .usuario(usuario)
                .build();

        when(dentistaRepository.findAll(any(Specification.class))).thenReturn(List.of(dentista));

        // Este test sigue siendo válido para el método List<DentistaResponse> obtenerDentistas(String, String, Long)
        List<DentistaResponse> dentistas = dentistaService.obtenerDentistas("Carlos", "Ortodoncia", 10L);

        assertNotNull(dentistas);
        assertEquals(1, dentistas.size());

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
        assertEquals(Sexo.MASCULINO.name(), response.getSexo()); // Asegurarse de que el enum se compara con su nombre
        assertEquals("1990-07-15", response.getFechaNacimiento().toString());

        verify(dentistaRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Debe obtener una página de dentistas filtrados y paginados correctamente")
    void testObtenerDentistasPaginados() {
        // 1. Datos de prueba
        Usuario usuario = Usuario.builder()
                .id(10L)
                .nombres("Carlos")
                .apellidoPaterno("Gómez")
                .apellidoMaterno("Fernández")
                .correo("carlos.gomez@mail.com")
                .telefono("999888777")
                .sexo(Sexo.MASCULINO)
                .fechaNacimiento(LocalDate.of(1990, 7, 15))
                .build();

        Dentista dentista = Dentista.builder()
                .id(1L)
                .nColegiatura("NC12345")
                .estado(true)
                .especializacion("Ortodoncia")
                .usuario(usuario)
                .build();

        // 2. Crear el DentistaResponse esperado (usando el mapper si es necesario)
        // En un test unitario del servicio, es mejor crear el DTO directamente para controlar la conversión
        DentistaResponse dentistaResponse = DentistaMapper.toDto(dentista);

        // 3. Configurar el objeto Pageable para la llamada
        Pageable pageable = PageRequest.of(0, 10); // Página 0, tamaño 10

        // 4. Crear un PageImpl con los datos simulados
        Page<Dentista> mockDentistaPage = new PageImpl<>(List.of(dentista), pageable, 1); // Total de elementos: 1

        // 5. Mockear el comportamiento del repositorio
        // Ahora findAll espera una Specification Y un Pageable
        when(dentistaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockDentistaPage);

        // 6. Llamar al método del servicio a probar
        Page<DentistaResponse> resultadoPage = dentistaService.obtenerDentistasPaginados(
                "Carlos", "Ortodoncia", 10L, pageable);

        // 7. Aserciones
        assertNotNull(resultadoPage);
        assertEquals(1, resultadoPage.getTotalElements()); // Total de elementos
        assertEquals(1, resultadoPage.getContent().size()); // Elementos en la página actual
        assertEquals(0, resultadoPage.getNumber()); // Número de página
        assertEquals(10, resultadoPage.getSize()); // Tamaño de página

        DentistaResponse resultadoDentista = resultadoPage.getContent().get(0);
        assertEquals(dentistaResponse.getId(), resultadoDentista.getId());
        assertEquals(dentistaResponse.getNombres(), resultadoDentista.getNombres());
        assertEquals(dentistaResponse.getEspecializacion(), resultadoDentista.getEspecializacion());
        // Puedes añadir más aserciones para verificar que el DTO se mapeó correctamente

        // 8. Verificar interacciones del mock
        verify(dentistaRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Debe obtener una lista de especialidades de dentistas")
    void testObtenerEspecialidades() {
        List<String> especialidadesMock = List.of("Ortodoncia", "Endodoncia", "Cirugía Maxilofacial");

        when(dentistaRepository.findEspecializaciones()).thenReturn(especialidadesMock);

        List<String> especialidades = dentistaService.obtenerEspecialidades();

        assertNotNull(especialidades);
        assertEquals(3, especialidades.size());
        assertEquals("Ortodoncia", especialidades.get(0));
        assertEquals("Endodoncia", especialidades.get(1));
        assertEquals("Cirugía Maxilofacial", especialidades.get(2));

        verify(dentistaRepository, times(1)).findEspecializaciones();
    }
}