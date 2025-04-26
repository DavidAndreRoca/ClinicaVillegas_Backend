package com.clinicavillegas.app.appointment.dto.response;

import com.clinicavillegas.app.user.models.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DentistaResponse {
    Long id;
    String nColegiatura;
    boolean estado;
    String especializacion;
    Long usuarioId;
    String nombres;
    String apellidoPaterno;
    String apellidoMaterno;
    String correo;
    TipoDocumento tipoDocumento;
    String numeroIdentidad;
    String telefono;
    String sexo;
    String fechaNacimiento;
}
