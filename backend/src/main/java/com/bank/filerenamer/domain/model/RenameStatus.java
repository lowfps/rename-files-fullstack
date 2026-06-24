package com.bank.filerenamer.domain.model;

/**
 * Resultado posible de aplicar el motor de reglas a un archivo de origen.
 */
public enum RenameStatus {
    /** El archivo casó con una regla y se generó el nombre estandarizado. */
    TRANSFORMED,
    /** El archivo casó con una regla pero la transformación falló (fecha inválida, plantilla incompleta). */
    ERROR,
    /** Ninguna regla activa casó con el archivo. */
    NO_MAPEADO
}
