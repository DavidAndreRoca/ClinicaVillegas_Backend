package com.clinicavillegas.app.common;

import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.appointment.repositories.CitaRepository;
import com.clinicavillegas.app.appointment.specifications.CitaSpecification;
import com.clinicavillegas.app.email.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
public class ScheduledConfig {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 1 0 * * *")
    public void executeTask() {
        Specification<Cita> specs = CitaSpecification.conFecha(LocalDateTime.now().toLocalDate())
                .and(CitaSpecification.conEstado("Pendiente"));
        List<Cita> citas = citaRepository.findAll(specs);
        for (Cita cita : citas) {
            emailService.enviarRecordatorio(cita);
        }
    }
}
