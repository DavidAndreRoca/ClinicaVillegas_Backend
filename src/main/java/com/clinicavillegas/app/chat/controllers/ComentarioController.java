package com.clinicavillegas.app.chat.controllers;

import com.clinicavillegas.app.chat.dto.ComentarioRequest;
import com.clinicavillegas.app.chat.dto.ComentarioResponse;
import com.clinicavillegas.app.chat.services.ComentarioService;
import com.clinicavillegas.app.common.EndpointPaths;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.CHAT_BASE)
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    public ResponseEntity<List<ComentarioResponse>> obtenerComentarios(){
        return ResponseEntity.ok(comentarioService.obtenerComentarios());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> agregarComentario(@RequestBody ComentarioRequest request){
        comentarioService.agregarComentario(request);
        return ResponseEntity.ok(Map.of("mensaje", "Comentario agregado con exito"));
    }
}
