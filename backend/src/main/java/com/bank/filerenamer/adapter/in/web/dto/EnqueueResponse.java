package com.bank.filerenamer.adapter.in.web.dto;

/** Respuesta del encolado asíncrono: identificador del trabajo y estado inicial. */
public record EnqueueResponse(String jobId, String status) {

    public static EnqueueResponse queued(String jobId) {
        return new EnqueueResponse(jobId, "QUEUED");
    }
}
