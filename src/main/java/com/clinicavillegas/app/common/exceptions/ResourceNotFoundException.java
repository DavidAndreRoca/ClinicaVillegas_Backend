package com.clinicavillegas.app.common.exceptions;

public class ResourceNotFoundException extends RuntimeException{

    public <T> ResourceNotFoundException(Class<T> tipo, Object id) {
        super(String.format("Recurso del tipo '%s' con ID '%s' no fue encontrado", tipo.getName(), id));
    }

    public <T> ResourceNotFoundException(Class<T> tipo, String campo, Object valor) {
        super(String.format("Recurso del tipo '%s' con '%s' = '%s' no fue encontrado", tipo.getName(), campo, valor));
    }

    public <T> ResourceNotFoundException(Class<T> tipo, String mensajePersonalizado) {
        super(String.format("Recurso del tipo '%s': %s", tipo.getName(), mensajePersonalizado));
    }

}
