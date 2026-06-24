package com.bank.filerenamer.domain.port.in;

import com.bank.filerenamer.domain.model.S3File;

import java.util.List;

/**
 * Puerto de entrada para operar el bucket de origen: listar archivos y sembrar el lote de muestra.
 */
public interface BucketUseCase {

    List<S3File> listFiles();

    /** Sube el lote de archivos de muestra al bucket (simulación de carga). Devuelve sus nombres. */
    List<String> seedSampleFiles();
}
