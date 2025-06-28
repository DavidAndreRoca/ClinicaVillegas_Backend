package com.clinicavillegas.app.user.services;

import com.clinicavillegas.app.user.dto.request.UsuarioRequest;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UsuarioService {
    // Método para obtener usuarios SIN PAGINACIÓN (con tus filtros)
    // Usamos 'nombres' para ser consistente con la entidad y 'estado' como filtro
    List<UsuarioResponse> obtenerUsuarios(String nombres, String rol, Boolean estado); // <-- Parámetros de filtro y estado

    // NUEVO MÉTODO: Para obtener usuarios CON PAGINACIÓN y los mismos filtros
    Page<UsuarioResponse> obtenerUsuariosPaginados(String nombres, String rol, Boolean estado, Pageable pageable); // <-- Parámetros de filtro, estado y Pageable

    // Los demás métodos CRUD que ya tienes:
    // Asumo que 'eliminarCliente' es una eliminación lógica que cambia el estado a false// <-- Añadir este método de creación
    UsuarioResponse obtenerUsuarioPorId(Long id); // <-- Renombrado para consistencia
    void actualizarUsuarioPorId(Long id, UsuarioRequest request); // <-- Renombrado para consistencia
    void eliminarUsuario(Long id); // <-- Renombrado para consistencia
}