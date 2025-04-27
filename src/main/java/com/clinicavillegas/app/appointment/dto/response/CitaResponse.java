package com.clinicavillegas.app.appointment.dto.response;

import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.user.models.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CitaResponse {
    Long id;
    LocalDate fecha;
    LocalTime hora;
    BigDecimal monto;

    String nombres;
    String apellidoPaterno;
    String apellidoMaterno;
    TipoDocumento tipoDocumento;
    String numeroIdentidad;
    String sexo;
    String telefono;
    LocalDate fechaNacimiento;
    String estado;
    DentistaResponse dentista;
    Long usuarioId;
    Tratamiento tratamiento;
}
