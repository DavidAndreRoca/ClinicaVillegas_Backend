package com.clinicavillegas.app.email.services;

import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.email.dto.EmailRequest;

public interface EmailService {

    String enviarCorreo(EmailRequest email);

    String enviarCodigoVerificacion(String email);

    void enviarRecordatorio(Cita cita);

}
