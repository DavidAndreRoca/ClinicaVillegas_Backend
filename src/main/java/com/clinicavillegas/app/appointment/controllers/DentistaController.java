package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.DentistaRequest;
import com.clinicavillegas.app.appointment.dto.response.DentistaResponse;
import com.clinicavillegas.app.appointment.services.DentistaService;
import com.clinicavillegas.app.common.EndpointPaths;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault; // ¡Importar esta anotación!
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.DENTISTA_BASE)
public class DentistaController {
    private final DentistaService dentistaService;

    public DentistaController(DentistaService dentistaService) {
        this.dentistaService = dentistaService;
    }

    @GetMapping
    public ResponseEntity<?> obtenerDentistas(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "especializacion", required = false) String especializacion,
            @RequestParam(name = "usuarioId", required = false) Long usuarioId,
            @RequestParam(required = false, defaultValue = "false") boolean all, // Nuevo parámetro 'all'
            @PageableDefault(page = 0, size = 10, sort = "id") Pageable pageable // Usamos 'id' como sort por defecto
    ) {
        if (all) {
            // Si 'all' es true, obtenemos todos los dentistas (sin paginación)
            List<DentistaResponse> dentistas = dentistaService.obtenerDentistas(nombre, especializacion, usuarioId);
            return ResponseEntity.ok(dentistas);
        } else {
            // Si 'all' es false (o no se especifica), obtenemos dentistas paginados
            Page<DentistaResponse> dentistasPage = dentistaService.obtenerDentistasPaginados(nombre, especializacion, usuarioId, pageable);
            return ResponseEntity.ok(dentistasPage); // Devuelve el objeto Page completo con metadata de paginación
        }
    }

    @GetMapping("/especialidades")
    public ResponseEntity<List<String>> obtenerEspecialidades(){
        return ResponseEntity.ok(dentistaService.obtenerEspecialidades());
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> agregarDentista(@Valid @RequestBody DentistaRequest request){
        dentistaService.agregarDentista(request);
        return ResponseEntity.ok(Map.of("mensaje", "Dentista agregado con exito"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarDentista(@PathVariable("id") Long id, @Valid @RequestBody DentistaRequest request){
        dentistaService.actualizarDentista(id, request);
        return ResponseEntity.ok(Map.of("mensaje", "Dentista actualizado con exito"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarDentista(@PathVariable("id") Long id){
        dentistaService.eliminarDentista(id);
        return ResponseEntity.ok(Map.of("mensaje", "Dentista eliminado con exito"));
    }
}