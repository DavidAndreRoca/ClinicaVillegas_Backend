package com.clinicavillegas.app.email.services.impl;

import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.email.MailTemplates;
import com.clinicavillegas.app.email.dto.EmailRequest;
import com.clinicavillegas.app.email.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Random;

@Slf4j
@Service
public class DefaultEmailService implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String emisor;

    public DefaultEmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public String enviarCorreo(EmailRequest email) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("Clinica Dental Villegas<" + emisor + ">");
            mailMessage.setTo(email.getReceptor());
            mailMessage.setSubject(email.getAsunto());
            mailMessage.setText(email.getContenido());

            javaMailSender.send(mailMessage);
            return "Email sent successfully!";
        } catch (Exception e) {
            return "Email sending error!";
        }
    }

    public String enviarCodigoVerificacion(String email) {

        Random random = new Random();
        int codeInt = random.nextInt(1000000);
        String code = String.format("%06d", codeInt);
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("ClinicaDentalVillegas<" + emisor + ">");
            helper.setTo(email);
            helper.setSubject("Código de verificación para el registro");

            String htmlContent = String.format(MailTemplates.VERIFICATION_CODE, code);

            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            log.error("Error al enviar el código por correo: {}", e.getMessage());
        }
        return code;
    }

    public void enviarRecordatorio(Cita cita) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("ClinicaDentalVillegas<" + emisor + ">");
            helper.setTo(cita.getUsuario().getCorreo());
            helper.setSubject("Recordatorio de cita");

            String htmlContent = String.format(
                    MailTemplates.USER_REMINDER,
                    cita.getUsuario().getNombres(),
                    cita.getUsuario().getApellidoPaterno(),
                    cita.getUsuario().getApellidoMaterno(),
                    cita.getFecha().toString(),
                    cita.getHora().toString(),
                    cita.getTratamiento().getNombre(),
                    cita.getMonto().toString(),
                    cita.getTipoDocumento().getNombre(),
                    cita.getNumeroIdentidad(),
                    cita.getDentista().getUsuario().getNombres(),
                    cita.getDentista().getUsuario().getApellidoPaterno(),
                    cita.getDentista().getUsuario().getApellidoMaterno()
            );

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);

            // Enviar correo al dentista
            MimeMessage mimeMessageDentista = javaMailSender.createMimeMessage();
            MimeMessageHelper helperDentista = new MimeMessageHelper(mimeMessageDentista, true, "UTF-8");

            helperDentista.setFrom("ClinicaDentalVillegas<" + emisor + ">");
            helperDentista.setTo(cita.getDentista().getUsuario().getCorreo());
            helperDentista.setSubject("Recordatorio de cita programada");

            String htmlContentDentista = String.format(
                    MailTemplates.DENTIST_REMINDER,
                    cita.getDentista().getUsuario().getNombres(),
                    cita.getDentista().getUsuario().getApellidoPaterno(),
                    cita.getDentista().getUsuario().getApellidoMaterno(),
                    cita.getFecha().toString(),
                    cita.getHora().toString(),
                    cita.getUsuario().getNombres(),
                    cita.getUsuario().getApellidoPaterno(),
                    cita.getUsuario().getApellidoMaterno()
            );

            helperDentista.setText(htmlContentDentista, true);
            javaMailSender.send(mimeMessageDentista);
        } catch (Exception  e) {
            log.error("Error al enviar el correo de recordatorio al dentista: {}", e.getMessage());
        }

    }
    public void enviarConfirmacionReserva(Cita cita) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("ClinicaDentalVillegas<" + emisor + ">");
            helper.setTo(cita.getUsuario().getCorreo());
            helper.setSubject("Confirmación de reserva de cita");

            String htmlContent = String.format(
                    MailTemplates.USER_CONFIRMATION,
                    cita.getUsuario().getNombres(),
                    cita.getUsuario().getApellidoPaterno(),
                    cita.getUsuario().getApellidoMaterno(),
                    cita.getFecha().toString(),
                    cita.getHora().toString(),
                    cita.getTratamiento().getNombre(),
                    cita.getMonto().toString(),
                    cita.getTipoDocumento().getNombre(),
                    cita.getNumeroIdentidad(),
                    cita.getDentista().getUsuario().getNombres(),
                    cita.getDentista().getUsuario().getApellidoPaterno(),
                    cita.getDentista().getUsuario().getApellidoMaterno()
            );

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (Exception  e) {
            log.error("Error al enviar el correo de confirmación al usuario: {}", e.getMessage());
        }

    }
    public void enviarReprogramacionCita(Cita cita, LocalDate anteriorFecha, LocalTime anteriorHora) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("ClinicaDentalVillegas<" + emisor + ">");
            helper.setTo(cita.getDentista().getUsuario().getCorreo());
            helper.setSubject("Reprogramación de cita");

            String htmlContent = String.format(
                    MailTemplates.DENTIST_RESCHEDULE_NOTIFICATION,
                    cita.getDentista().getUsuario().getNombres(),
                    cita.getDentista().getUsuario().getApellidoPaterno(),
                    cita.getDentista().getUsuario().getApellidoMaterno(),
                    cita.getNombres(),
                    cita.getApellidoPaterno(),
                    cita.getApellidoMaterno(),
                    anteriorFecha,
                    anteriorHora,
                    cita.getFecha(),
                    cita.getHora()

            );

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (Exception  e) {
            log.error("Error al enviar el correo de reprogramación al dentista: {}", e.getMessage());
        }

    }
    public void enviarCancelacionReserva(Cita cita) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom("ClinicaDentalVillegas<" + emisor + ">");
            helper.setTo(cita.getDentista().getUsuario().getCorreo());
            helper.setSubject("Cancelación de cita");

            String htmlContent = String.format(
                    MailTemplates.DENTIST_CANCELLATION_NOTIFICATION,
                    cita.getDentista().getUsuario().getNombres(),
                    cita.getDentista().getUsuario().getApellidoPaterno(),
                    cita.getDentista().getUsuario().getApellidoMaterno(),
                    cita.getNombres(),
                    cita.getApellidoPaterno(),
                    cita.getApellidoMaterno(),
                    cita.getFecha(),
                    cita.getHora(),
                    cita.getTratamiento().getNombre(),
                    cita.getObservaciones() != null ? cita.getObservaciones() : ""
            );

            helper.setText(htmlContent, true);
            javaMailSender.send(mimeMessage);
        } catch (Exception  e) {
            log.error("Error al enviar el correo de cancelación al dentista: {}", e.getMessage());
        }

    }

}