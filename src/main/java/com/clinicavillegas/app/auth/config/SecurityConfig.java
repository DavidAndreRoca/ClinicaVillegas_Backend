package com.clinicavillegas.app.auth.config;

import com.clinicavillegas.app.common.EndpointPaths;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        authRequest -> authRequest
                                .requestMatchers("/api/citas/**").permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                .requestMatchers(deepMatcher(EndpointPaths.USUARIO_BASE)).hasRole("ADMINISTRADOR")

                                .requestMatchers(HttpMethod.GET, deepMatcher(EndpointPaths.TIPO_TRATAMIENTO_BASE)).hasAnyRole("PACIENTE", "DENTISTA", "ADMINISTRADOR")
                                .requestMatchers(HttpMethod.POST, deepMatcher(EndpointPaths.TIPO_TRATAMIENTO_BASE)).hasRole("ADMINISTRADOR")
                                .requestMatchers(HttpMethod.PUT, deepMatcher(EndpointPaths.TIPO_TRATAMIENTO_BASE)).hasRole("ADMINISTRADOR")
                                .requestMatchers(HttpMethod.DELETE, deepMatcher(EndpointPaths.TIPO_TRATAMIENTO_BASE)).hasRole("ADMINISTRADOR")

                                .requestMatchers(HttpMethod.GET, deepMatcher(EndpointPaths.TRATAMIENTO_BASE)).hasAnyRole("PACIENTE", "DENTISTA", "ADMINISTRADOR")
                                .requestMatchers(HttpMethod.POST, deepMatcher(EndpointPaths.TRATAMIENTO_BASE)).hasRole("ADMINISTRADOR")
                                .requestMatchers(HttpMethod.PUT, deepMatcher(EndpointPaths.TRATAMIENTO_BASE)).hasRole("ADMINISTRADOR")
                                .requestMatchers(HttpMethod.DELETE, deepMatcher(EndpointPaths.TRATAMIENTO_BASE)).hasRole("ADMINISTRADOR")

                                .requestMatchers(HttpMethod.GET, deepMatcher(EndpointPaths.TIPO_DOCUMENTO_BASE)).permitAll()
                                .requestMatchers(HttpMethod.POST, deepMatcher(EndpointPaths.TIPO_DOCUMENTO_BASE)).hasRole("ADMINISTRADOR")
                                .requestMatchers(HttpMethod.PUT, deepMatcher(EndpointPaths.TIPO_DOCUMENTO_BASE)).hasRole("ADMINISTRADOR")
                                .requestMatchers(HttpMethod.DELETE, deepMatcher(EndpointPaths.TIPO_DOCUMENTO_BASE)).hasRole("ADMINISTRADOR")

                                .requestMatchers(HttpMethod.GET, deepMatcher(EndpointPaths.DENTISTA_BASE)).hasAnyRole("PACIENTE", "DENTISTA", "ADMINISTRADOR")
                                .requestMatchers(HttpMethod.POST, deepMatcher(EndpointPaths.DENTISTA_BASE)).hasRole("ADMINISTRADOR")
                                .requestMatchers(HttpMethod.PUT, deepMatcher(EndpointPaths.DENTISTA_BASE)).hasAnyRole("DENTISTA", "ADMINISTRADOR")
                                .requestMatchers(HttpMethod.DELETE, deepMatcher(EndpointPaths.DENTISTA_BASE)).hasRole("ADMINISTRADOR")

                                .requestMatchers(HttpMethod.GET, deepMatcher(EndpointPaths.HORARIO_BASE)).authenticated()
                                .requestMatchers(HttpMethod.POST, deepMatcher(EndpointPaths.HORARIO_BASE)).hasAnyRole("DENTISTA", "ADMINISTRADOR")
                                .requestMatchers(HttpMethod.PUT, deepMatcher(EndpointPaths.HORARIO_BASE)).hasAnyRole("DENTISTA", "ADMINISTRADOR")
                                .requestMatchers(HttpMethod.DELETE, deepMatcher(EndpointPaths.HORARIO_BASE)).hasAnyRole("DENTISTA", "ADMINISTRADOR")

                                .requestMatchers(deepMatcher(EndpointPaths.CITA_BASE)).hasAnyRole("PACIENTE", "DENTISTA", "ADMINISTRADOR")

                                .requestMatchers(HttpMethod.POST ,deepMatcher(EndpointPaths.AUTH_BASE)).permitAll()
                                .requestMatchers(HttpMethod.GET ,deepMatcher(EndpointPaths.AUTH_BASE)).authenticated()

                                .requestMatchers(deepMatcher(EndpointPaths.EMAIL_BASE)).permitAll()

                                .requestMatchers(deepMatcher(EndpointPaths.RENIEC_BASE)).permitAll()

                                .requestMatchers(HttpMethod.GET, deepMatcher(EndpointPaths.CHAT_BASE)).permitAll()
                                .requestMatchers(HttpMethod.POST, deepMatcher(EndpointPaths.CHAT_BASE)).hasAnyRole("PACIENTE", "DENTISTA")
                                .requestMatchers(HttpMethod.PUT, deepMatcher(EndpointPaths.CHAT_BASE)).hasAnyRole("PACIENTE", "DENTISTA")
                                .requestMatchers(HttpMethod.DELETE, deepMatcher(EndpointPaths.CHAT_BASE)).hasAnyRole("PACIENTE", "DENTISTA")

                                .requestMatchers(deepMatcher(EndpointPaths.REPORTE_BASE)).permitAll()
                                .anyRequest().denyAll()

                )
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    public static String deepMatcher(String endpoint){
        return String.format("%s/**", endpoint);
    }
}
