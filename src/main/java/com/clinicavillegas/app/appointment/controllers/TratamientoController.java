package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.TratamientoRequest;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.appointment.services.TratamientoService;
import com.clinicavillegas.app.common.EndpointPaths;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.TRATAMIENTO_BASE)
public class TratamientoController {
    private final TratamientoService tratamientoService;

    public TratamientoController(TratamientoService tratamientoService) {
        this.tratamientoService = tratamientoService;
    }

    @GetMapping
    public ResponseEntity<List<Tratamiento>> getTratamientos(
            @RequestParam(name = "tipo", required = false) Long tipoId,
            @RequestParam(name = "nombre", required = false) String nombre
    ) {
        return ResponseEntity.ok(tratamientoService.obtenerTratamientos(tipoId, nombre));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> guardarTratamiento(@Valid @RequestBody TratamientoRequest request) {
        tratamientoService.guardarTratamiento(request);
        return ResponseEntity.ok(Map.of("mensaje", "Tratamiento guardado con exito"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarTratamiento(@PathVariable Long id, @Valid @RequestBody TratamientoRequest request) {
        tratamientoService.actualizarTratamiento(id, request);
        return ResponseEntity.ok(Map.of("mensaje", "Tratamiento actualizado con exito"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarTratamiento(@PathVariable Long id) {
        tratamientoService.eliminarTratamiento(id);
        return ResponseEntity.ok(Map.of("mensaje", "Tratamiento eliminado con exito"));
    }

}
