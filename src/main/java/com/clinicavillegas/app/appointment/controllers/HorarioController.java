package com.clinicavillegas.app.appointment.controllers;

import com.clinicavillegas.app.appointment.dto.request.HorarioRequest;
import com.clinicavillegas.app.appointment.dto.response.HorarioResponse;
import com.clinicavillegas.app.appointment.services.HorarioService;
import com.clinicavillegas.app.common.EndpointPaths;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.HORARIO_BASE)
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @GetMapping
    public ResponseEntity<List<HorarioResponse>> obtenerHorarios(
            @RequestParam(name = "dentistaId", required = false) Long dentistaId,
            @RequestParam(name = "dia", required = false) String dia

    ){
        return ResponseEntity.ok(horarioService.obtenerHorarios(dentistaId, dia));
    }


    @PostMapping
    public ResponseEntity<Map<String, Object>> agregarHorario(@RequestBody HorarioRequest request){
        horarioService.agregarHorario(request);
        return ResponseEntity.ok(Map.of("mensaje", "Horario agregado con exito"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarHorario(@PathVariable Long id){
        horarioService.eliminarHorario(id);
        return ResponseEntity.ok(Map.of("mensaje", "Horario eliminado con exito"));
    }
}
