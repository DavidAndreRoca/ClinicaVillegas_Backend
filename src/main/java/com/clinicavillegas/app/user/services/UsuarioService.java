package com.clinicavillegas.app.user.services;

import com.clinicavillegas.app.user.dto.request.UsuarioRequest;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;

import java.util.List;

public interface UsuarioService {
    List<UsuarioResponse> obtenerClientes(String nombre, String rol);
    UsuarioResponse obtenerClientePorId(Long id);

    void actualizarClientePorId(Long id, UsuarioRequest request);

    void eliminarCliente(Long id);
}
