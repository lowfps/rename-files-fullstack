package com.bank.filerenamer.domain.port.out;

import com.bank.filerenamer.domain.model.S3File;

import java.util.List;

/**
 * Puerto de salida hacia el almacenamiento de archivos (S3). El dominio no conoce la
 * implementación (LocalStack, S3 real, etc.).
 */
public interface FileStoragePort {

    /** Lista los archivos disponibles en el bucket de origen. */
    List<S3File> listFiles();

    /** Sube un archivo (simulación de carga batch). */
    void putFile(String key, byte[] content);
}
