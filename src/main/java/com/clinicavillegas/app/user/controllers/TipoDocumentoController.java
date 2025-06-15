package com.clinicavillegas.app.user.controllers;

import com.clinicavillegas.app.common.EndpointPaths;
import com.clinicavillegas.app.user.dto.request.TipoDocumentoRequest;
import com.clinicavillegas.app.user.models.TipoDocumento;
import com.clinicavillegas.app.user.services.TipoDocumentoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(EndpointPaths.TIPO_DOCUMENTO_BASE)
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    public TipoDocumentoController(TipoDocumentoService tipoDocumentoService) {
        this.tipoDocumentoService = tipoDocumentoService;
    }

    @GetMapping
    public ResponseEntity<?> getTiposDocumento(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "acronimo", required = false) String acronimo,
            @RequestParam(name = "all", required = false, defaultValue = "false") boolean all,
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable
    ){
        if (all) {
            // Llama al método para obtener la lista completa si all=true
            List<TipoDocumento> tiposDocumento = tipoDocumentoService.obtenerTiposDocumento(nombre, acronimo);
            return ResponseEntity.ok(tiposDocumento);
        } else {
            // Llama al método paginado por defecto
            Page<TipoDocumento> tiposDocumentoPaginados = tipoDocumentoService.obtenerTiposDocumentoPaginados(nombre, acronimo, pageable);
            return ResponseEntity.ok(tiposDocumentoPaginados);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoDocumento> obtenerTipoDocumento(@PathVariable("id") Long id){
        TipoDocumento tipoDocumento = tipoDocumentoService.obtenerTipoDocumento(id);
        if (tipoDocumento == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tipoDocumento);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> agregarTipoDocumento(@Valid @RequestBody TipoDocumentoRequest tipoDocumentoRequest){
        tipoDocumentoService.agregarTipoDocumento(tipoDocumentoRequest);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Tipo de documento agregado con exito");
        return ResponseEntity.ok(response);

    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarTipoDocumento(@PathVariable("id") Long id, @Valid @RequestBody TipoDocumentoRequest tipoDocumentoRequest){
        tipoDocumentoService.actualizarTipoDocumento(id, tipoDocumentoRequest);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Tipo de documento actualizado con exito");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarTipoDocumento(@PathVariable("id") Long id){
        tipoDocumentoService.eliminarTipoDocumento(id);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Tipo de documento eliminado con exito");
        return ResponseEntity.ok(response);
    }
}