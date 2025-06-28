// UsuarioController.java
package com.clinicavillegas.app.user.controllers;

import com.clinicavillegas.app.common.EndpointPaths;
import com.clinicavillegas.app.user.dto.request.UsuarioRequest;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.USUARIO_BASE) // Asumiendo que es "/api/usuarios"
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @GetMapping // Este es el endpoint principal con paginación y filtros
    public ResponseEntity<?> getUsuarios(
            @RequestParam(required = false) String nombres, // Coincide con el campo de la entidad
            @RequestParam(required = false) String rol,
            @RequestParam(required = false, defaultValue = "true") Boolean estado, // Valor por defecto
            @RequestParam(required = false, defaultValue = "false") boolean all, // Bandera para deshabilitar paginación
            @PageableDefault(page = 0, size = 10, sort = "nombres") Pageable pageable) { // Paginación por defecto y ordenación

        if (all) {
            List<UsuarioResponse> usuarios = usuarioService.obtenerUsuarios(nombres, rol, estado);
            return ResponseEntity.ok(usuarios);
        } else {
            Page<UsuarioResponse> usuariosPage = usuarioService.obtenerUsuariosPaginados(nombres, rol, estado, pageable);
            return ResponseEntity.ok(usuariosPage);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerUsuarioPorId(@PathVariable Long id){
        return ResponseEntity.ok(usuarioService.obtenerUsuarioPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request){
        usuarioService.actualizarUsuarioPorId(id, request);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario actualizado con éxito"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarUsuario(@PathVariable Long id){
        usuarioService.eliminarUsuario(id);
        return ResponseEntity.ok(Map.of("mensaje", "Usuario eliminado (lógicamente) con éxito"));
    }
}