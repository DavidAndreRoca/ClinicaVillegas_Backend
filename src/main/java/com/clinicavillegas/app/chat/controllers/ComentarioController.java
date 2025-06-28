package com.clinicavillegas.app.chat.controllers;

import com.clinicavillegas.app.chat.dto.request.ComentarioRequest;
import com.clinicavillegas.app.chat.dto.response.ComentarioResponse;
import com.clinicavillegas.app.chat.services.ComentarioService;
import com.clinicavillegas.app.common.EndpointPaths;
import com.clinicavillegas.app.user.models.Rol;
import com.clinicavillegas.app.user.models.Usuario;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.CHAT_BASE)
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    // Cambiado a Page y añadido Pageable como parámetro
    // Spring automáticamente mapea los parámetros 'page', 'size', 'sort' de la URL a un objeto Pageable.
    public ResponseEntity<Page<ComentarioResponse>> obtenerComentarios(Pageable pageable){
        return ResponseEntity.ok(comentarioService.obtenerComentarios(pageable));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> agregarComentario(@Valid @RequestBody ComentarioRequest request, @AuthenticationPrincipal Usuario usario){
        if (usario.getRol().equals(Rol.DENTISTA)){
            request.setUsuarioId(usario.getId());
        }
        comentarioService.agregarComentario(request);
        return ResponseEntity.ok(Map.of("mensaje", "Comentario agregado con exito"));
    }
}