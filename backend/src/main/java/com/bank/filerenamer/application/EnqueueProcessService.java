package com.bank.filerenamer.application;

import com.bank.filerenamer.domain.port.in.EnqueueProcessUseCase;
import com.bank.filerenamer.domain.port.out.ProcessJobQueuePort;

/**
 * Caso de uso de encolado asíncrono. Solo delega en el puerto de cola: no conoce SQS ni Spring,
 * igual que el resto de la capa de aplicación.
 */
public class EnqueueProcessService implements EnqueueProcessUseCase {

    private final ProcessJobQueuePort jobQueue;

    public EnqueueProcessService(ProcessJobQueuePort jobQueue) {
        this.jobQueue = jobQueue;
    }

    @Override
    public String enqueue() {
        return jobQueue.enqueue();
    }
}
