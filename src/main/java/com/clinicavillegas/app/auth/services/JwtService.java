package com.clinicavillegas.app.auth.services;

import com.clinicavillegas.app.appointment.models.Dentista;
import com.clinicavillegas.app.appointment.repositories.DentistaRepository;
import com.clinicavillegas.app.appointment.specifications.DentistaSpecification;
import com.clinicavillegas.app.user.models.Rol;
import com.clinicavillegas.app.user.models.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    public String SECRET_KEY;

    private final DentistaRepository dentistaRepository;

    public JwtService(DentistaRepository dentistaRepository) {
        this.dentistaRepository = dentistaRepository;
    }

    public String getToken(UserDetails user) {
        return getToken(new HashMap<>(), user);
    }

    public String getToken(Map<String, Object> extraClaims, UserDetails user) {
        extraClaims.put("role", user.getAuthorities().stream().findFirst().orElseThrow().getAuthority());
        if (user instanceof Usuario usuario) {
            extraClaims.put("id", usuario.getId());
            if (usuario.getRol().equals(Rol.DENTISTA)) {
                Specification<Dentista> specs = DentistaSpecification.conUsuarioId(usuario.getId());
                extraClaims.put("dentistaId", dentistaRepository.findAll(specs).getFirst().getId());
            }
            extraClaims.put("nombres", usuario.getApellidoPaterno() + " " + usuario.getApellidoMaterno() + ", " + usuario.getNombres());
            extraClaims.put("imagenPerfil", usuario.getImagenPerfil());
        }
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ 1000 * 60 * 60 * 4))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getKey() {
        byte[] keyBytes= Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String getEmailFromToken(String token) {
        return getClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userEmail = getEmailFromToken(token);
        return userEmail.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    private Claims getAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T getClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Date getExpiration(String token){
        return getClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token){
        return getExpiration(token).before(new Date());
    }
}
