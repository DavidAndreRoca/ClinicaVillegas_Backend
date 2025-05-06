package com.clinicavillegas.app.user.controllers;

import com.clinicavillegas.app.common.EndpointPaths;
import com.clinicavillegas.app.user.dto.request.UsuarioRequest;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.services.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.USUARIO_BASE)
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService){
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> obtenerClientes(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "rol", required = false) String rol
    ) {
        return ResponseEntity.ok(usuarioService.obtenerClientes(nombre, rol));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerClientePorId(@PathVariable Long id){
        return ResponseEntity.ok(usuarioService.obtenerClientePorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarCliente(@PathVariable Long id, @Valid UsuarioRequest request){
        usuarioService.actualizarClientePorId(id, request);
        return ResponseEntity.ok(Map.of("mensaje", "Cliente actualizado con exito"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarCliente(@PathVariable Long id){
        usuarioService.eliminarCliente(id);
        return ResponseEntity.ok(Map.of("mensaje", "Cliente eliminado con exito"));
    }

}
