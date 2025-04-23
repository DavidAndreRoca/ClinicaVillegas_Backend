package com.clinicavillegas.app.user.mappers;

import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.models.Usuario;

public class UsuarioMapper {
    public static UsuarioResponse toDto(Usuario usuario){
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombres(usuario.getNombres())
                .apellidoPaterno(usuario.getApellidoPaterno())
                .apellidoMaterno(usuario.getApellidoMaterno())
                .numeroIdentidad(usuario.getNumeroIdentidad())
                .sexo(usuario.getSexo())
                .telefono(usuario.getTelefono())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .correo(usuario.getCorreo())
                .imagenPerfil(usuario.getImagenPerfil())
                .estado(usuario.isEstado())
                .imagenPerfil(usuario.getImagenPerfil())
                .rol(usuario.getRol())
                .tipoDocumento(usuario.getTipoDocumento())
                .fechaCreacion(usuario.getFechaCreacion())
                .fechaModificacion(usuario.getFechaModificacion())
                .build();
    }
}
