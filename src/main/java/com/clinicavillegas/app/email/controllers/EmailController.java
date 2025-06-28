package com.clinicavillegas.app.email.controllers;

import com.clinicavillegas.app.common.EndpointPaths;
import com.clinicavillegas.app.email.dto.CodeRequest;
import com.clinicavillegas.app.email.dto.EmailRequest;
import com.clinicavillegas.app.email.services.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.EMAIL_BASE)
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/sendemail")
    public String sendEmail(@Valid @RequestBody EmailRequest request){
        return emailService.enviarCorreo(request);
    }

    @PostMapping("/sendcode")
    public ResponseEntity<Map<String, Object>> sendCode(@Valid @RequestBody CodeRequest request){
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("code", emailService.enviarCodigoVerificacion(request.getEmail()));
        return ResponseEntity.ok(response);
    }

}