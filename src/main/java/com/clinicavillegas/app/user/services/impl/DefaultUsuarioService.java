package com.clinicavillegas.app.user.services.impl;

import com.clinicavillegas.app.user.dto.request.UsuarioRequest;
import com.clinicavillegas.app.user.dto.response.UsuarioResponse;
import com.clinicavillegas.app.user.mappers.UsuarioMapper;
import com.clinicavillegas.app.user.models.Usuario;
import com.clinicavillegas.app.user.repositories.UsuarioRepository;
import com.clinicavillegas.app.user.services.UsuarioService;
import com.clinicavillegas.app.user.specifications.UsuarioSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultUsuarioService implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public DefaultUsuarioService(UsuarioRepository usuarioRepository){
        this.usuarioRepository = usuarioRepository;
    }
    public List<UsuarioResponse> obtenerClientes(String nombre, String rol) {
        Specification<Usuario> specs = UsuarioSpecification.conEstado(true)
                .and(UsuarioSpecification.conRol(rol))
                .and(UsuarioSpecification.conNombres(nombre));
        List<Usuario> usuarios = usuarioRepository.findAll(specs);
        return usuarios.stream().map(UsuarioMapper::toDto).toList();
    }
    public UsuarioResponse obtenerClientePorId(Long id){
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        return UsuarioMapper.toDto(usuario);
    }

    public void actualizarClientePorId(Long id, UsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setNombres(request.getNombres());
        usuario.setApellidoPaterno(request.getApellidoPaterno());
        usuario.setApellidoMaterno(request.getApellidoMaterno());
        usuario.setTelefono(request.getTelefono());
        usuario.setImagenPerfil(request.getImagenPerfil());
        usuarioRepository.save(usuario);
    }

    public void eliminarCliente(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElseThrow();
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
    }
}
