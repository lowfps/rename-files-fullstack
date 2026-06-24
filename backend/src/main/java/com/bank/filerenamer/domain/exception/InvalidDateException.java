package com.bank.filerenamer.domain.exception;

/** Se lanza cuando el token de fecha embebido no es una fecha válida para el formato declarado. */
public class InvalidDateException extends RuntimeException {
    public InvalidDateException(String message) {
        super(message);
    }
}
