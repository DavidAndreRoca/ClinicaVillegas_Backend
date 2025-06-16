package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.CitaReprogramarRequest;
import com.clinicavillegas.app.appointment.dto.request.CitaRequest;
import com.clinicavillegas.app.appointment.dto.request.ValidacionCitaRequest;
import com.clinicavillegas.app.appointment.dto.response.CitaResponse;
import com.clinicavillegas.app.appointment.services.CitaService;
import com.clinicavillegas.app.common.EndpointPaths;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(EndpointPaths.CITA_BASE)
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public ResponseEntity<?> buscarCitas(
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long dentistaId,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) Long tratamientoId,
            @RequestParam(required = false) String sexo,
            @RequestParam(required = false, defaultValue = "false") boolean all,
            @PageableDefault(page = 0, size = 10, sort = "fecha") Pageable pageable) {

        if (all) {
            List<CitaResponse> citas = citaService.obtenerCitas(usuarioId, dentistaId, estado, fechaInicio, fechaFin, tratamientoId, sexo);
            return ResponseEntity.ok(citas);
        } else {
            Page<CitaResponse> citasPage = citaService.obtenerCitasPaginadas(usuarioId, dentistaId, estado, fechaInicio, fechaFin, tratamientoId, sexo, pageable);
            return ResponseEntity.ok(citasPage);
        }
    }

    @PostMapping
    public ResponseEntity<Void> agregarCita(@Valid @RequestBody CitaRequest citaRequest) {
        citaService.agregarCita(citaRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> actualizarCita(@PathVariable Long id, @Valid @RequestBody CitaRequest citaRequest) {
        citaService.actualizarCita(id, citaRequest);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/atender")
    public ResponseEntity<Void> atenderCita(@PathVariable Long id) {
        citaService.atenderCita(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCita(@PathVariable Long id) {
        citaService.eliminarCita(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validar-disponibilidad")
    public ResponseEntity<Boolean> validarDisponibilidad(@Valid @RequestBody ValidacionCitaRequest request) {
        boolean disponible = citaService.validarDisponibilidad(request);
        return ResponseEntity.ok(disponible);
    }

    @PatchMapping("/{id}/reprogramar")
    public ResponseEntity<Void> reprogramarCita(@PathVariable Long id, @Valid @RequestBody CitaReprogramarRequest request) {
        citaService.reprogramarCita(id, request);
        return ResponseEntity.ok().build();
    }
}