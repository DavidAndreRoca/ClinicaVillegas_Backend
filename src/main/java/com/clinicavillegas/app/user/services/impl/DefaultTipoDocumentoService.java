package com.clinicavillegas.app.user.services.impl;
import com.clinicavillegas.app.common.exceptions.ResourceNotFoundException;
import com.clinicavillegas.app.user.dto.request.TipoDocumentoRequest;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.repositories.TipoDocumentoRepository;
import com.clinicavillegas.app.user.services.TipoDocumentoService;
import com.clinicavillegas.app.user.specifications.TipoDocumentoSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DefaultTipoDocumentoService implements TipoDocumentoService {

    private static final String CACHE_TIPOS_DOCUMENTO_LISTA = "tiposDocumentoLista";
    private static final String CACHE_TIPOS_DOCUMENTO_PAGINADOS = "tiposDocumentoPaginados";
    private static final String CACHE_TIPO_DOCUMENTO_POR_ID = "tipoDocumentoPorId";

    private final TipoDocumentoRepository tipoDocumentoRepository;

    public DefaultTipoDocumentoService(TipoDocumentoRepository tipoDocumentoRepository){
        this.tipoDocumentoRepository = tipoDocumentoRepository;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_TIPOS_DOCUMENTO_LISTA, allEntries = true),
            @CacheEvict(value = CACHE_TIPOS_DOCUMENTO_PAGINADOS, allEntries = true)
    })
    public void agregarTipoDocumento(TipoDocumentoRequest request) {
        log.info("Agregando nuevo tipo de documento: {}", request.getNombre());
        tipoDocumentoRepository.save(
                TipoDocumento.builder()
                        .nombre(request.getNombre())
                        .acronimo(request.getAcronimo())
                        .estado(true)
                        .build());
    }

    @Override
    @Cacheable(value = CACHE_TIPOS_DOCUMENTO_LISTA, key = "{#nombre, #acronimo}")
    public List<TipoDocumento> obtenerTiposDocumento(String nombre, String acronimo) {
        log.info("Obteniendo lista completa de tipos de documento con nombre: {} y acrónimo: {}", nombre, acronimo);
        Specification<TipoDocumento> specs = TipoDocumentoSpecification.conNombre(nombre)
                .and(TipoDocumentoSpecification.conAcronimo(acronimo).and(TipoDocumentoSpecification.conEstado(true)));
        return tipoDocumentoRepository.findAll(specs);
    }

    @Override
    @Cacheable(value = CACHE_TIPOS_DOCUMENTO_PAGINADOS, key = "{#nombre, #acronimo, #pageable.pageNumber, #pageable.pageSize, #pageable.sort}")
    public Page<TipoDocumento> obtenerTiposDocumentoPaginados(String nombre, String acronimo, Pageable pageable) {
        log.info("Obteniendo tipos de documento paginados de la base de datos con nombre: {} y acrónimo: {}", nombre, acronimo);
        Specification<TipoDocumento> specs = TipoDocumentoSpecification.conNombre(nombre)
                .and(TipoDocumentoSpecification.conAcronimo(acronimo).and(TipoDocumentoSpecification.conEstado(true)));
        return tipoDocumentoRepository.findAll(specs, pageable);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_TIPO_DOCUMENTO_POR_ID, key = "#id"),
            @CacheEvict(value = CACHE_TIPOS_DOCUMENTO_LISTA, allEntries = true),
            @CacheEvict(value = CACHE_TIPOS_DOCUMENTO_PAGINADOS, allEntries = true)
    })
    public void actualizarTipoDocumento(Long id, TipoDocumentoRequest request) {
        log.info("Actualizando tipo de documento en la base de datos y caché para ID: {}", id);
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(TipoDocumento.class, id)
        );
        tipoDocumento.setNombre(request.getNombre());
        tipoDocumento.setAcronimo(request.getAcronimo());
        tipoDocumentoRepository.save(tipoDocumento);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CACHE_TIPO_DOCUMENTO_POR_ID, key = "#id"),
            @CacheEvict(value = CACHE_TIPOS_DOCUMENTO_LISTA, allEntries = true),
            @CacheEvict(value = CACHE_TIPOS_DOCUMENTO_PAGINADOS, allEntries = true)
    })
    public void eliminarTipoDocumento(Long id) {
        log.info("Eliminando tipo de documento (lógico) de la base de datos y caché para ID: {}", id);
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(TipoDocumento.class, id)
        );
        tipoDocumento.setEstado(false);
        tipoDocumentoRepository.save(tipoDocumento);
    }

    @Override
    @Cacheable(value = CACHE_TIPO_DOCUMENTO_POR_ID, key = "#id")
    public TipoDocumento obtenerTipoDocumento(Long id) {
        log.info("Obteniendo tipo de documento de la base de datos por ID: {}", id);
        return tipoDocumentoRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(TipoDocumento.class, id)
        );
    }
}