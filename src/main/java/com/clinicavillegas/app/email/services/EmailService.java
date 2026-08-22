package com.clinicavillegas.app.email.services;

import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.email.dto.EmailRequest;

import java.time.LocalDate;
import java.time.LocalTime;

public interface EmailService {

    String enviarCorreo(EmailRequest email);

    String enviarCodigoVerificacion(String email);

    void enviarRecordatorio(Cita cita);

    void enviarConfirmacionReserva(Cita cita);

    void enviarReprogramacionCita(Cita cita, LocalDate anteriorFecha, LocalTime anteriorHora);

    void enviarCancelacionReserva(Cita cita);

}
