package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.CitaReprogramarRequest;
import com.clinicavillegas.app.appointment.dto.request.CitaRequest;
import com.clinicavillegas.app.appointment.dto.request.ValidacionCitaRequest;
import com.clinicavillegas.app.appointment.dto.response.CitaResponse;
import com.clinicavillegas.app.appointment.services.CitaService;
import com.clinicavillegas.app.common.EndpointPaths;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.CITA_BASE)
public class CitaController {

    private final CitaService citaService;
    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public ResponseEntity<List<CitaResponse>> obtenerCitas(
            @RequestParam(name = "usuarioId", required = false) Long usuarioId,
            @RequestParam(name = "dentistaId", required = false) Long dentistaId,
            @RequestParam(name = "estado", required = false) String estado,
            @RequestParam(name = "fechaInicio", required = false) LocalDate fechaInicio,
            @RequestParam(name = "fechaFin", required = false) LocalDate fechaFin,
            @RequestParam(name = "tratamientoId", required = false) Long tratamientoId,
            @RequestParam(name = "sexo", required = false) String sexo
    ){
        return ResponseEntity.ok(citaService.obtenerCitas(usuarioId, dentistaId, estado, fechaInicio, fechaFin, tratamientoId, sexo));
    }

    @PostMapping("/validar")
    public ResponseEntity<Boolean> validarCita(@RequestBody ValidacionCitaRequest request){
        boolean validacion = citaService.validarDisponibilidad(request);
        return ResponseEntity.ok(validacion);
    }

    @PutMapping("/atender/{id}")
    public ResponseEntity<Map<String, Object>> atenderCita(@PathVariable("id") Long id){
        citaService.atenderCita(id);
        return ResponseEntity.ok(Map.of("mensaje", "Cita atendida con exito"));
    }
    @PutMapping("/reprogramar/{id}")
    public ResponseEntity<Map<String, Object>> reprogramarCita(@PathVariable("id") Long id, @RequestBody CitaReprogramarRequest request){
        citaService.reprogramarCita(id, request);
        return ResponseEntity.ok(Map.of("mensaje", "Cita reprogramada con exito"));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> agregarCita(@RequestBody CitaRequest citaRequest){
        citaService.agregarCita(citaRequest);
        return ResponseEntity.ok(Map.of("mensaje", "Cita agregada con exito"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarCita(@PathVariable("id") Long id, @RequestBody CitaRequest citaRequest){
        citaService.actualizarCita(id, citaRequest);
        return ResponseEntity.ok(Map.of("mensaje", "Cita actualizada con exito"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarCita(@PathVariable("id") Long id){
        citaService.eliminarCita(id);
        return ResponseEntity.ok(Map.of("mensaje", "Cita cancelada con exito"));
    }
}