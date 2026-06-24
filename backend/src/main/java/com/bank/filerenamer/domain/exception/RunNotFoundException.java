package com.bank.filerenamer.domain.exception;

/** La ejecución (process run) solicitada no existe. */
public class RunNotFoundException extends RuntimeException {
    public RunNotFoundException(Long id) {
        super("No existe la ejecución con id " + id);
    }
}
