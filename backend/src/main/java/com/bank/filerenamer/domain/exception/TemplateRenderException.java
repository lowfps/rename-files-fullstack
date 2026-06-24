package com.bank.filerenamer.domain.exception;

/** Se lanza cuando la plantilla destino referencia un placeholder sin valor disponible. */
public class TemplateRenderException extends RuntimeException {
    public TemplateRenderException(String message) {
        super(message);
    }
}
