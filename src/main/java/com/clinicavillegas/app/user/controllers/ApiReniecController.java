package com.clinicavillegas.app.user.controllers;

import com.clinicavillegas.app.common.EndpointPaths;
import com.clinicavillegas.app.user.services.ApiReniecService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.RENIEC_BASE)
public class ApiReniecController {
    private final ApiReniecService apiReniecService;

    public ApiReniecController(ApiReniecService apiReniecService) {
        this.apiReniecService = apiReniecService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> consultarDni(@RequestParam(name = "dni", required = true) String dni) {
        Map<String, Object> response = apiReniecService.consultarDni(dni);
        return ResponseEntity.ok(response);
    }
}
