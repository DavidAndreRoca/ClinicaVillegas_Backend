package com.clinicavillegas.app.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    String correo;
    String tipoDocumento;
    String documento;
    String nombres;
    String apellidoPaterno;
    String apellidoMaterno;
    LocalDate fechaNacimiento;
    String telefono;
    String sexo;
    String contrasena;
}