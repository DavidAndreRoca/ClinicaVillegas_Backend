package com.clinicavillegas.app.email.services;

import com.clinicavillegas.app.appointment.models.*;
import com.clinicavillegas.app.email.dto.EmailRequest;
import com.clinicavillegas.app.email.services.impl.DefaultEmailService;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.models.Usuario;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private DefaultEmailService emailService;

    private EmailRequest emailRequest;
    private Cita cita;

    @BeforeEach
    void setUp() {
        // Configuración común para las pruebas
        emailRequest = new EmailRequest();
        emailRequest.setReceptor("paciente@example.com");
        emailRequest.setAsunto("Asunto de prueba");
        emailRequest.setContenido("Contenido de prueba");

        // Configurar una cita completa para pruebas de recordatorio
        Usuario usuarioPaciente = new Usuario();
        usuarioPaciente.setNombres("Juan");
        usuarioPaciente.setApellidoPaterno("Pérez");
        usuarioPaciente.setApellidoMaterno("Gómez");
        usuarioPaciente.setCorreo("paciente@example.com");

        Usuario usuarioDentista = new Usuario();
        usuarioDentista.setNombres("María");
        usuarioDentista.setApellidoPaterno("López");
        usuarioDentista.setApellidoMaterno("Rodríguez");
        usuarioDentista.setCorreo("dentista@example.com");

        Dentista dentista = new Dentista();
        dentista.setUsuario(usuarioDentista);

        Tratamiento tratamiento = new Tratamiento();
        tratamiento.setNombre("Limpieza dental");
        tratamiento.setCosto(BigDecimal.valueOf(150.0));
        tratamiento.setDuracion(Duration.ofMinutes(30));



        TipoDocumento tipoDocumento = new TipoDocumento();
        tipoDocumento.setNombre("DNI");

        cita = new Cita();
        cita.setUsuario(usuarioPaciente);
        cita.setDentista(dentista);
        cita.setTratamiento(tratamiento);
        cita.setTipoDocumento(tipoDocumento);
        cita.setNumeroIdentidad("12345678");
        cita.setFecha(LocalDate.now().plusDays(1));
        cita.setHora(LocalTime.of(10, 0));
        cita.setMonto(BigDecimal.valueOf(150.0));
    }

    @Test
    @DisplayName("Debe enviar correo electrónico básico exitosamente")
    void testEnviarCorreo() {
        // Act
        String resultado = emailService.enviarCorreo(emailRequest);

        // Assert
        assertEquals("Email sent successfully!", resultado);
        verify(javaMailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("Debe manejar error al enviar correo electrónico")
    void testEnviarCorreoConError() {
        // Arrange
        doThrow(new RuntimeException("Error de envío")).when(javaMailSender).send(any(SimpleMailMessage.class));

        // Act
        String resultado = emailService.enviarCorreo(emailRequest);

        // Assert
        assertEquals("Email sending error!", resultado);
    }

    @Test
    @DisplayName("Debe generar y enviar código de verificación")
    void testEnviarCodigoVerificacion() throws MessagingException {
        // Arrange
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        String codigo = emailService.enviarCodigoVerificacion("test@example.com");

        // Assert
        assertNotNull(codigo);
        assertEquals(6, codigo.length()); // El código debe ser de 6 dígitos
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Debe manejar error al enviar código de verificación")
    void testEnviarCodigoVerificacionConError() throws Exception {
        // Arrange
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Error de envío")).when(javaMailSender).send(any(MimeMessage.class));

        // Act
        // El método enviarCodigoVerificacion sí lanza la excepción si ocurre un error de envío.
        // Por lo tanto, usamos assertThrows para capturarla.
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            emailService.enviarCodigoVerificacion("test@example.com");
        });

        // Assert
        assertEquals("Error de envío", thrown.getMessage());
        // No verificamos un 'codigo' porque esperamos que la excepción se lance antes de que se retorne.
    }

    @Test
    @DisplayName("Debe enviar recordatorio de cita al paciente y al dentista")
    void testEnviarRecordatorio() throws MessagingException {
        // Arrange
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        emailService.enviarRecordatorio(cita);

        // Assert
        verify(javaMailSender, times(2)).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Debe manejar error al enviar recordatorio")
    void testEnviarRecordatorioConError() throws Exception {
        // Arrange
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Cambiar a RuntimeException ya que JavaMailSender.send() no declara MessagingException
        doThrow(new RuntimeException("Error de envío simulado"))
                .when(javaMailSender).send(any(MimeMessage.class));

        // Act
        emailService.enviarRecordatorio(cita);

        // Assert
        verify(javaMailSender, atLeastOnce()).send(any(MimeMessage.class));
        // No debería lanzar excepción, solo registrar el error internamente
    }

    @Test
    @DisplayName("El código de verificación debe ser numérico")
    void testCodigoVerificacionEsNumerico() throws Exception {
        // Arrange
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        String codigo = emailService.enviarCodigoVerificacion("test@example.com");

        // Assert
        assertNotNull(codigo);
        assertTrue(codigo.matches("\\d+"));
    }
}
