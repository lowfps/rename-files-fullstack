package com.bank.filerenamer.domain.model;

/**
 * Archivo disponible en el bucket de origen, identificado por su clave (nombre).
 *
 * @param key nombre/clave del objeto en el bucket (ej. {@code PHO_CD_DES_20260430.txt})
 */
public record S3File(String key) {

    public S3File {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("La clave del archivo no puede ser vacía");
        }
    }

    /**
     * Nombre sin extensión. Soporta archivos sin extensión y con {@code .txt}.
     * Solo elimina la última extensión cuando el punto no es el primer carácter.
     */
    public String nameWithoutExtension() {
        int dot = key.lastIndexOf('.');
        return dot > 0 ? key.substring(0, dot) : key;
    }
}
