package com.clinicavillegas.app.appointment.mappers;

import com.clinicavillegas.app.appointment.dto.response.DentistaResponse;
import com.clinicavillegas.app.appointment.models.Dentista;

public class DentistaMapper {
    public static DentistaResponse toDto(Dentista dentista){
        return DentistaResponse.builder()
                .id(dentista.getId())
                .nColegiatura(dentista.getNColegiatura())
                .especializacion(dentista.getEspecializacion())
                .usuarioId(dentista.getUsuario().getId())
                .nombres(dentista.getUsuario().getNombres())
                .correo(dentista.getUsuario().getCorreo())
                .estado(dentista.isEstado())
                .apellidoPaterno(dentista.getUsuario().getApellidoPaterno())
                .apellidoMaterno(dentista.getUsuario().getApellidoMaterno())
                .tipoDocumento(dentista.getUsuario().getTipoDocumento())
                .numeroIdentidad(dentista.getUsuario().getNumeroIdentidad())
                .sexo(dentista.getUsuario().getSexo().toString())
                .telefono(dentista.getUsuario().getTelefono())
                .fechaNacimiento(dentista.getUsuario().getFechaNacimiento().toString())
                .build();
    }
}
