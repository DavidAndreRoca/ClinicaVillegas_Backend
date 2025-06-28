package com.clinicavillegas.app.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TipoDocumentoRequest {

    @NotBlank(message = "El nombre del tipo de documento del usuario no puede estar vacío")
    @Size(max = 100, message = "El nombre no debe pasar de 100 letras")
    String nombre;

    @NotBlank(message = "El acrónimo del tipo de documento del usuario no puede estar vacío")
    @Size(max = 15, message = "El acrónimo no debería superar las 15 letras")
    String acronimo;
}
