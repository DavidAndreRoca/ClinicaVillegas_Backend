package com.clinicavillegas.app.appointment.mappers;

import com.clinicavillegas.app.appointment.dto.request.CitaRequest;
import com.clinicavillegas.app.appointment.dto.response.CitaResponse;
import com.clinicavillegas.app.appointment.dto.response.DentistaResponse;
import com.clinicavillegas.app.appointment.models.Cita;
import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.common.exceptions.ResourceNotFoundException;
import com.clinicavillegas.app.user.models.Sexo;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.models.Usuario;

public class CitaMapper {

    public static CitaResponse toDto(Cita cita){
        return CitaResponse.builder()
                .id(cita.getId())
                .fecha(cita.getFecha())
                .hora(cita.getHora())
                .monto(cita.getMonto())
                .nombres(cita.getNombres())
                .apellidoPaterno(cita.getApellidoPaterno())
                .apellidoMaterno(cita.getApellidoMaterno())
                .tipoDocumento(cita.getTipoDocumento())
                .numeroIdentidad(cita.getNumeroIdentidad())
                .telefono(cita.getUsuario().getTelefono())
                .sexo(cita.getSexo().toString())
                .estado(cita.getEstado())
                .observaciones(cita.getObservaciones())
                .fechaNacimiento(cita.getFechaNacimiento())
                .dentista(DentistaResponse.builder()
                        .id(cita.getDentista().getId())
                        .nombres(cita.getDentista().getUsuario().getNombres())
                        .apellidoPaterno(cita.getDentista().getUsuario().getApellidoPaterno())
                        .apellidoMaterno(cita.getDentista().getUsuario().getApellidoMaterno())
                        .especializacion(cita.getDentista().getEspecializacion())
                        .nColegiatura(cita.getDentista().getNColegiatura())
                        .estado(cita.getDentista().isEstado())
                        .tipoDocumento(cita.getDentista().getUsuario().getTipoDocumento())
                        .numeroIdentidad(cita.getDentista().getUsuario().getNumeroIdentidad())
                        .sexo(cita.getDentista().getUsuario().getSexo().toString())
                        .fechaNacimiento(cita.getDentista().getUsuario().getFechaNacimiento().toString())
                        .telefono(cita.getDentista().getUsuario().getTelefono())
                        .build())
                .usuarioId(cita.getUsuario().getId())
                .tratamiento(cita.getTratamiento())
                .build();
    }
}
