package com.clinicavillegas.app.appointment.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DentistaRequest {
    String nColegiatura;
    String especializacion;
    Long usuarioId;
}
