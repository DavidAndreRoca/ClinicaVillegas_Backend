package com.clinicavillegas.app.user.services;

import com.clinicavillegas.app.user.dto.request.TipoDocumentoRequest;
import com.clinicavillegas.app.user.models.TipoDocumento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TipoDocumentoService {

    // Método para obtener la lista completa con filtros (sin paginación)
    List<TipoDocumento> obtenerTiposDocumento(String nombre, String acronimo);

    // NUEVO MÉTODO: Para obtener tipos de documento CON PAGINACIÓN y filtros
    Page<TipoDocumento> obtenerTiposDocumentoPaginados(String nombre, String acronimo, Pageable pageable);

    void agregarTipoDocumento(TipoDocumentoRequest request);
    void actualizarTipoDocumento(Long id, TipoDocumentoRequest request);
    void eliminarTipoDocumento(Long id);
    TipoDocumento obtenerTipoDocumento(Long id);
}