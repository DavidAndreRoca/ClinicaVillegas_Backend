package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.TratamientoRequest;
import com.clinicavillegas.app.appointment.models.Tratamiento;
import com.clinicavillegas.app.appointment.services.TratamientoService;
import com.clinicavillegas.app.common.EndpointPaths;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(EndpointPaths.TRATAMIENTO_BASE)
public class TratamientoController {

    private final TratamientoService tratamientoService;

    public TratamientoController(TratamientoService tratamientoService) {
        this.tratamientoService = tratamientoService;
    }

    @GetMapping
    public ResponseEntity<?> getTratamientos(
            @RequestParam(name = "tipo", required = false) Long tipoId,
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "estado", defaultValue = "true") Boolean estado,
            @RequestParam(required = false, defaultValue = "false") boolean all, // Parámetro 'all'
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable) { // Pageable con valores por defecto

        if (all) {
            // Si 'all' es true, llama al servicio para obtener todos los tratamientos (sin paginación)
            List<Tratamiento> tratamientos = tratamientoService.obtenerTodosTratamientos(tipoId, nombre, estado);
            return ResponseEntity.ok(tratamientos);
        } else {
            // Si 'all' es false (o no se especifica), llama al servicio para obtener tratamientos paginados
            Page<Tratamiento> tratamientosPage = tratamientoService.obtenerTratamientosPaginados(tipoId, nombre, estado, pageable);
            return ResponseEntity.ok(tratamientosPage);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tratamiento> getTratamientoById(@PathVariable Long id) {
        Tratamiento tratamiento = tratamientoService.obtenerTratamiento(id);
        return ResponseEntity.ok(tratamiento);
    }

    @PostMapping
    public ResponseEntity<String> createTratamiento(@RequestBody TratamientoRequest request) {
        tratamientoService.guardarTratamiento(request);
        return ResponseEntity.ok("Tratamiento guardado con éxito");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateTratamiento(@PathVariable Long id, @RequestBody TratamientoRequest request) {
        tratamientoService.actualizarTratamiento(id, request);
        return ResponseEntity.ok("Tratamiento actualizado con éxito");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTratamiento(@PathVariable Long id) {
        tratamientoService.eliminarTratamiento(id);
        return ResponseEntity.ok("Tratamiento eliminado con éxito");
    }
}