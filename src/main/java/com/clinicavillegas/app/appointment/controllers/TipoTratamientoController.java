package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.TipoTratamientoRequest;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.services.TipoTratamientoService;
import com.clinicavillegas.app.common.EndpointPaths;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.TIPO_TRATAMIENTO_BASE)
public class TipoTratamientoController {
    private final TipoTratamientoService tipoTratamientoService;

    public TipoTratamientoController(TipoTratamientoService tipoTratamientoService) {
        this.tipoTratamientoService = tipoTratamientoService;
    }

    @GetMapping
    public ResponseEntity<List<TipoTratamiento>> obtenerTiposTratamiento() {
        return ResponseEntity.ok(tipoTratamientoService.obtenerTiposTratamiento());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> agregarTipoTratamiento(@Valid @RequestBody TipoTratamientoRequest request) {
        tipoTratamientoService.agregarTipoTratamiento(request);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Tipo de tratamiento agregado con exito");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarTipoTratamiento(@PathVariable("id") Long id, @Valid @RequestBody TipoTratamientoRequest request) {
        tipoTratamientoService.actualizarTipoTratamiento(id, request);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Tipo de tratamiento actualizado con exito");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarTipoTratamiento(@PathVariable("id") Long id) {
        tipoTratamientoService.eliminarTipoTratamiento(id);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Tipo de tratamiento eliminado con exito");
        return ResponseEntity.ok(response);
    }
}
