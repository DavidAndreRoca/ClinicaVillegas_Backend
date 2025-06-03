package com.clinicavillegas.app.user.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class ApiReniecService {

    private final RestTemplate restTemplate;

    @Value("${app.reniec.url}")
    private String RENIEC_URL;

    @Value("${app.reniec.token}")
    private String RENIEC_TOKEN;

    public ApiReniecService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> consultarDni(String dni) {
        String url = String.format("%sdni?numero=%s", RENIEC_URL, dni);
        String token = RENIEC_TOKEN;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("Referer", "https://apis.net.pe/consulta-dni-api");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>() {
                });

        return response.getBody();
    }
}
