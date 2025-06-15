package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.TipoTratamientoRequest;
import com.clinicavillegas.app.appointment.models.TipoTratamiento;
import com.clinicavillegas.app.appointment.services.TipoTratamientoService;
import com.clinicavillegas.app.common.EndpointPaths;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public ResponseEntity<?> getTipoTratamientos(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false,defaultValue = "true") Boolean estado,
            @RequestParam(required = false, defaultValue = "false") boolean all,
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable) {

        if (all) {
            // Llama al método 'obtenerTiposTratamiento' para obtener la lista completa.
            List<TipoTratamiento> tiposTratamiento = tipoTratamientoService.obtenerTiposTratamiento(nombre, estado);
            return ResponseEntity.ok(tiposTratamiento);
        } else {
            // Llama al método paginado por defecto.
            Page<TipoTratamiento> tiposTratamientoPage = tipoTratamientoService.obtenerTiposTratamientoPaginados(nombre, estado, pageable);
            return ResponseEntity.ok(tiposTratamientoPage);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> agregarTipoTratamiento(@Valid @RequestBody TipoTratamientoRequest request) {
        tipoTratamientoService.agregarTipoTratamiento(request);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Tipo de tratamiento agregado con éxito");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarTipoTratamiento(@PathVariable("id") Long id, @Valid @RequestBody TipoTratamientoRequest request) {
        tipoTratamientoService.actualizarTipoTratamiento(id, request);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Tipo de tratamiento actualizado con éxito");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarTipoTratamiento(@PathVariable("id") Long id) {
        tipoTratamientoService.eliminarTipoTratamiento(id);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Tipo de tratamiento eliminado con éxito");
        return ResponseEntity.ok(response);
    }
}