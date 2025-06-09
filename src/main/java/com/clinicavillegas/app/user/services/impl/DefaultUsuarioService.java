package com.clinicavillegas.app.user.services.impl;

import com.clinicavillegas.app.user.dto.request.UsuarioRequest;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.mappers.UsuarioMapper;
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import com.clinicavillegas.app.user.services.UsuarioService;
import com.clinicavillegas.app.user.specifications.UsuarioSpecification;
import lombok.extern.slf4j.Slf4j;
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

    public DefaultUsuarioService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }

    @Cacheable(value = CACHE_USUARIOS_LISTA, key = "{#nombre, #rol}")
    public List<UsuarioResponse> obtenerClientes(String nombre, String rol) {
        log.info("Obteniendo clientes de la base de datos con nombre: {} y rol: {}", nombre, rol);
        Specification<Usuario> specs = UsuarioSpecification.conEstado(true)
                .and(UsuarioSpecification.conRol(rol))
                .and(UsuarioSpecification.conNombres(nombre));
        List<Usuario> usuarios = usuarioRepository.findAll(specs);
        return usuarios.stream().map(UsuarioMapper::toDto).toList();
    }

    @Cacheable(value = CACHE_USUARIO_POR_ID, key = "#id") //
    public UsuarioResponse obtenerClientePorId(Long id){
        log.info("Obteniendo cliente de la base de datos por ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        return UsuarioMapper.toDto(usuario);
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_USUARIO_POR_ID, key = "#id"),
            @CacheEvict(value = CACHE_USUARIOS_LISTA, allEntries = true)
    })
    public void actualizarClientePorId(Long id, UsuarioRequest request) {
        log.info("Actualizando cliente en la base de datos y caché para ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setNombres(request.getNombres());
        usuario.setApellidoPaterno(request.getApellidoPaterno());
        usuario.setApellidoMaterno(request.getApellidoMaterno());
        usuario.setTelefono(request.getTelefono());
        usuario.setImagenPerfil(request.getImagenPerfil());
        usuarioRepository.save(usuario);
    }

    @Caching(evict = {
            @CacheEvict(value = CACHE_USUARIO_POR_ID, key = "#id"),
            @CacheEvict(value = CACHE_USUARIOS_LISTA, allEntries = true)
    })
    public void eliminarCliente(Long id) {
        log.info("Eliminando cliente (lógico) de la base de datos y caché para ID: {}", id);
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
    }
}