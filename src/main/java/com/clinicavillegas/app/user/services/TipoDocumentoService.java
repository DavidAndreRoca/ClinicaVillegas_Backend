package com.clinicavillegas.app.user.services;

import com.clinicavillegas.app.user.dto.request.TipoDocumentoRequest;
import com.clinicavillegas.app.user.models.TipoDocumento;

import java.util.List;

public interface TipoDocumentoService {
    void agregarTipoDocumento(TipoDocumentoRequest request);

    List<TipoDocumento> obtenerTiposDocumento(String nombre, String acronimo);

    void actualizarTipoDocumento(Long id, TipoDocumentoRequest request);

    void eliminarTipoDocumento(Long id);
    TipoDocumento obtenerTipoDocumento(Long id);
}
