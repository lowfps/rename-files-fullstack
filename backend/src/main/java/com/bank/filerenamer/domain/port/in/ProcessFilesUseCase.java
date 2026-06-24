package com.bank.filerenamer.domain.port.in;

import com.bank.filerenamer.domain.model.ProcessRun;

/**
 * Puerto de entrada para ejecutar el renombramiento sobre los archivos del bucket.
 */
public interface ProcessFilesUseCase {

    /** Lista el bucket, aplica las reglas vigentes, persiste y devuelve la ejecución. */
    ProcessRun process();

    /** Reprocesa con las reglas vigentes (útil tras cambiar el catálogo). */
    ProcessRun reprocess(Long sourceRunId);
}
