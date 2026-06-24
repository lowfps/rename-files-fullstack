package com.bank.filerenamer.domain.exception;

/** La regla solicitada no existe. */
public class RuleNotFoundException extends RuntimeException {
    public RuleNotFoundException(Long id) {
        super("No existe la regla con id " + id);
    }
}
