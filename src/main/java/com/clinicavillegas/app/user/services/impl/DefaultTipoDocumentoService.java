package com.clinicavillegas.app.user.services.impl;

import com.clinicavillegas.app.common.exceptions.ResourceNotFoundException;
import com.clinicavillegas.app.user.dto.request.TipoDocumentoRequest;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.repositories.TipoDocumentoRepository;
import com.clinicavillegas.app.user.services.TipoDocumentoService;
import com.clinicavillegas.app.user.specifications.TipoDocumentoSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DefaultTipoDocumentoService implements TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    public DefaultTipoDocumentoService(TipoDocumentoRepository tipoDocumentoRepository){
        this.tipoDocumentoRepository = tipoDocumentoRepository;
    }
    public void agregarTipoDocumento(TipoDocumentoRequest request) {
        tipoDocumentoRepository.save(
                TipoDocumento.builder()
                        .nombre(request.getNombre())
                        .acronimo(request.getAcronimo())
                        .estado(true)
                        .build());
    }

    public List<TipoDocumento> obtenerTiposDocumento() {
        return tipoDocumentoRepository.findAll();
    }

    public List<TipoDocumento> obtenerTiposDocumento(String nombre, String acronimo) {
        Specification<TipoDocumento> specs = TipoDocumentoSpecification.conNombre(nombre)
                .and(TipoDocumentoSpecification.conAcronimo(acronimo).and(TipoDocumentoSpecification.conEstado(true)));
        return tipoDocumentoRepository.findAll(specs);
    }

    public void actualizarTipoDocumento(Long id, TipoDocumentoRequest request) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(TipoDocumento.class, id)
        );
        tipoDocumento.setNombre(request.getNombre());
        tipoDocumento.setAcronimo(request.getAcronimo());
        tipoDocumentoRepository.save(tipoDocumento);
    }

    public void eliminarTipoDocumento(Long id) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(TipoDocumento.class, id)
        );
        tipoDocumento.setEstado(false);
        tipoDocumentoRepository.save(tipoDocumento);
    }

    public TipoDocumento obtenerTipoDocumento(Long id) {
        return tipoDocumentoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(TipoDocumento.class, id)
        );
    }
}
