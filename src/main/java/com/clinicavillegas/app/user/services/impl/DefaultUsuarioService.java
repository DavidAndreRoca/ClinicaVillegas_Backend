// DefaultUsuarioService.java
package com.clinicavillegas.app.user.services.impl;

import com.clinicavillegas.app.user.dto.request.UsuarioRequest;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.mappers.UsuarioMapper;
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import com.clinicavillegas.app.user.services.UsuarioService;
import com.clinicavillegas.app.user.specifications.UsuarioSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.util.List;

@Service
@Slf4j
public class DefaultUsuarioService implements UsuarioService {

    private static final String CACHE_USUARIOS_LISTA = "usuariosLista";
    private static final String CACHE_USUARIO_POR_ID = "usuarioPorId";

    private final UsuarioRepository usuarioRepository;

    // El constructor ahora solo inyecta UsuarioRepository
    public DefaultUsuarioService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }

    // Método para obtener usuarios SIN PAGINACIÓN (con tus filtros)
    @Override
    @Cacheable(value = CACHE_USUARIOS_LISTA, key = "{#nombres, #rol, #estado}")
    public List<UsuarioResponse> obtenerUsuarios(String nombres, String rol, Boolean estado) {
        log.info("Obteniendo usuarios (sin paginación) con nombre: {}, rol: {} y estado: {}", nombres, rol, estado);
        Specification<Usuario> specs = Specification.where(UsuarioSpecification.conNombres(nombres))
                .and(UsuarioSpecification.conRol(rol))
                .and(UsuarioSpecification.conEstado(estado));
        List<Usuario> usuarios = usuarioRepository.findAll(specs);
        return usuarios.stream().map(UsuarioMapper::toDto).toList();
    }

    // Método para obtener usuarios CON PAGINACIÓN y los mismos filtros
    @Override
    public Page<UsuarioResponse> obtenerUsuariosPaginados(String nombres, String rol, Boolean estado, Pageable pageable) {
        log.info("Obteniendo usuarios (paginado) con nombre: {}, rol: {}, estado: {} y paginación: {}", nombres, rol, estado, pageable);
        Specification<Usuario> specs = Specification.where(UsuarioSpecification.conNombres(nombres))
                .and(UsuarioSpecification.conRol(rol))
                .and(UsuarioSpecification.conEstado(estado));

        Page<Usuario> usuariosPage = usuarioRepository.findAll(specs, pageable);
        return usuariosPage.map(UsuarioMapper::toDto);
    }

    // Método para obtener un usuario por ID
    @Override
    @Cacheable(value = CACHE_USUARIO_POR_ID, key = "#id")
    public UsuarioResponse obtenerUsuarioPorId(Long id){
        log.info("Obteniendo usuario de la base de datos por ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return UsuarioMapper.toDto(usuario);
    }

    // Método para actualizar un usuario por ID
    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_USUARIO_POR_ID, key = "#id"),
            @CacheEvict(value = CACHE_USUARIOS_LISTA, allEntries = true)
    })
    public void actualizarUsuarioPorId(Long id, UsuarioRequest request) {
        log.info("Actualizando usuario en la base de datos y caché para ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        // Aquí actualizas solo los campos que el UsuarioRequest te permite actualizar.
        // Asumiendo que el UsuarioRequest solo tiene campos actualizables (nombres, apellidos, teléfono, imagenPerfil).
        usuario.setNombres(request.getNombres());
        usuario.setApellidoPaterno(request.getApellidoPaterno());
        usuario.setApellidoMaterno(request.getApellidoMaterno());
        usuario.setTelefono(request.getTelefono());
        usuario.setImagenPerfil(request.getImagenPerfil());
        usuarioRepository.save(usuario);
    }

    // Método para eliminación lógica de un usuario
    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_USUARIO_POR_ID, key = "#id"),
            @CacheEvict(value = CACHE_USUARIOS_LISTA, allEntries = true)
    })
    public void eliminarUsuario(Long id) {
        log.info("Eliminando usuario (lógico) de la base de datos y caché para ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        usuario.setEstado(false); // Eliminación lógica: cambia el estado a false
        usuarioRepository.save(usuario);
    }
}