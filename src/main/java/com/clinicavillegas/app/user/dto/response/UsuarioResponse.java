package com.clinicavillegas.app.user.dto.response;

import com.clinicavillegas.app.user.models.Rol;
import com.clinicavillegas.app.user.models.Sexo;
import com.clinicavillegas.app.user.models.TipoDocumento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponse {
    private Long id;

    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;

    private String numeroIdentidad;
    private Sexo sexo;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String correo;
    private String imagenPerfil;
    private boolean estado;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    private Rol rol;
    private TipoDocumento tipoDocumento;
}
