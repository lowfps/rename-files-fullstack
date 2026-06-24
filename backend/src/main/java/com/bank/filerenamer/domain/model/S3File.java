package com.bank.filerenamer.domain.model;

public record S3File(String key) {

    public S3File {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("La clave del archivo no puede ser vacía");
        }
    }
    
    public String nameWithoutExtension() {
        int dot = key.lastIndexOf('.');
        return dot > 0 ? key.substring(0, dot) : key;
    }
}
