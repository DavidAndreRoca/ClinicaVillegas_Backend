package com.clinicavillegas.app.appointment.services;

import com.clinicavillegas.app.appointment.dto.request.CancelacionDentistaRequest;
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
    @DisplayName("Debe agregar un nuevo dentista si el usuario no tiene uno asociado")
    void testAgregarNuevoDentista() {
        DentistaRequest request = DentistaRequest.builder()
                .nColegiatura("NEW123")
                .especializacion("Pediatría Dental")
                .usuarioId(1L)
                .build();

        Usuario usuario = Usuario.builder()
                .id(1L)
                .nombres("Nuevo")
                .apellidoPaterno("Usuario")
                .rol(Rol.PACIENTE)
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(dentistaRepository.findByUsuario(any(Usuario.class))).thenReturn(Optional.empty());
        when(dentistaRepository.save(any(Dentista.class))).thenAnswer(invocation -> {
            Dentista savedDentista = invocation.getArgument(0);
            savedDentista.setId(100L);
            return savedDentista;
        });
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> dentistaService.agregarDentista(request));

        verify(dentistaRepository, times(1)).findByUsuario(usuario);
        verify(dentistaRepository, times(1)).save(any(Dentista.class));
        verify(usuarioRepository, times(1)).save(usuario);
        assertEquals(Rol.DENTISTA, usuario.getRol());
    }


    @Test
    @DisplayName("Debe eliminar lógicamente un dentista y sus horarios")
    void testEliminarDentistaLogicamente() {
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
                        .dia(Dia.LUNES) // Asegúrate de usar tu enum Dia si aplica
                        .horaComienzo(LocalTime.of(9, 0))
                        .horaFin(LocalTime.of(12, 0))
                        .dentista(dentista)
                        .build()
        );

        CancelacionDentistaRequest cancelacionRequest = new CancelacionDentistaRequest("Cese por motivos personales");

        when(dentistaRepository.findById(1L)).thenReturn(Optional.of(dentista));
        when(horarioRepository.findByDentista(dentista)).thenReturn(horarios);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dentistaRepository.save(any(Dentista.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dentistaService.eliminarDentista(1L, cancelacionRequest);

        verify(horarioRepository, times(1)).findByDentista(dentista);
        verify(horarioRepository, times(horarios.size())).delete(any(Horario.class));
        verify(usuarioRepository, times(1)).save(usuario);
        assertEquals(Rol.PACIENTE, usuario.getRol());

        // Verificar que se guardó el dentista con el estado actualizado (eliminación lógica)
        verify(dentistaRepository, times(1)).save(dentista);
        assertEquals("Cese por motivos personales", dentista.getMotivoCese());
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
                .rol(Rol.PACIENTE)
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
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(dentistaRepository.save(any(Dentista.class))).thenAnswer(invocation -> invocation.getArgument(0));

        dentistaService.actualizarDentista(1L, request);

        verify(usuarioRepository, times(1)).save(usuarioAnterior);
        assertEquals(Rol.PACIENTE, usuarioAnterior.getRol());

        verify(usuarioRepository, times(1)).findById(20L);
        verify(usuarioRepository, times(1)).save(usuarioActual);
        assertEquals(Rol.DENTISTA, usuarioActual.getRol());

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

        when(dentistaRepository.findAll(any(Specification.class))).thenReturn(List.of(dentista));

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
        assertEquals(Sexo.MASCULINO.name(), response.getSexo());
        assertEquals("1990-07-15", response.getFechaNacimiento().toString());

        verify(dentistaRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    @DisplayName("Debe obtener una página de dentistas filtrados y paginados correctamente")
    void testObtenerDentistasPaginados() {
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

        DentistaResponse dentistaResponse = DentistaMapper.toDto(dentista);

        Pageable pageable = PageRequest.of(0, 10);

        Page<Dentista> mockDentistaPage = new PageImpl<>(List.of(dentista), pageable, 1);

        when(dentistaRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mockDentistaPage);

        Page<DentistaResponse> resultadoPage = dentistaService.obtenerDentistasPaginados(
                "Carlos", "Ortodoncia", 10L, pageable);

        assertNotNull(resultadoPage);
        assertEquals(1, resultadoPage.getTotalElements());
        assertEquals(1, resultadoPage.getContent().size());
        assertEquals(0, resultadoPage.getNumber());
        assertEquals(10, resultadoPage.getSize());

        DentistaResponse resultadoDentista = resultadoPage.getContent().get(0);
        assertEquals(dentistaResponse.getId(), resultadoDentista.getId());
        assertEquals(dentistaResponse.getNombres(), resultadoDentista.getNombres());
        assertEquals(dentistaResponse.getEspecializacion(), resultadoDentista.getEspecializacion());

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