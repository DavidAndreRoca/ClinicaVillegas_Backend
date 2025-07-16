package com.clinicavillegas.app.appointment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CancelacionDentistaRequest {
    @NotBlank(message = "El motivo de cese laboral del dentista no puede estar en blanco.")
    @Size(max = 500, message = "El motivo de cese laboral no puede superar los 500 caracteres.")
    private String motivoCese;
}
