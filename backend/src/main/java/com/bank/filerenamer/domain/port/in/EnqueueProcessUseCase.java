package com.bank.filerenamer.domain.port.in;

/**
 * Puerto de entrada para solicitar el renombramiento de forma <b>asíncrona</b>: en vez de
 * ejecutar el motor en la misma petición HTTP, encola un trabajo que un worker procesará después.
 * El resultado se consulta luego en el panel de control (ejecuciones).
 */
public interface EnqueueProcessUseCase {

    /** Encola una solicitud de procesamiento y devuelve el identificador del trabajo. */
    String enqueue();
}
