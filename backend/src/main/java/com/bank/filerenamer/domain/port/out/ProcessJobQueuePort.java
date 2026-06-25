package com.bank.filerenamer.domain.port.out;

/**
 * Puerto de salida hacia la cola de mensajería. El dominio define <i>qué</i> necesita (encolar una
 * solicitud de procesamiento) sin conocer la tecnología concreta (SQS); el adaptador la implementa.
 */
public interface ProcessJobQueuePort {

    /** Publica una solicitud de procesamiento en la cola y devuelve el id del mensaje. */
    String enqueue();
}
