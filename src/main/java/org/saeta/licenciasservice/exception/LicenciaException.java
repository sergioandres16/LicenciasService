package org.saeta.licenciasservice.exception;

public class LicenciaException extends RuntimeException {

    public LicenciaException(String message) {
        super(message);
    }

    public LicenciaException(String message, Throwable cause) {
        super(message, cause);
    }
}