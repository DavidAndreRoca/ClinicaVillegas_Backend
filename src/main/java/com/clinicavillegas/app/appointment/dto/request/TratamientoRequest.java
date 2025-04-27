package com.clinicavillegas.app.appointment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TratamientoRequest {
    String nombre;
    String descripcion;
    BigDecimal costo;
    String imagenURL;
    int duracion;
    Long tipoTratamientoId;
}
