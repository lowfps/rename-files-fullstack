package com.bank.filerenamer.adapter.out.messaging;

import com.bank.filerenamer.config.SqsProperties;
import com.bank.filerenamer.domain.port.out.ProcessJobQueuePort;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.time.Instant;

/**
 * Adaptador de salida que publica solicitudes de procesamiento en una cola SQS.
 * Implementa {@link ProcessJobQueuePort}: el dominio solo ve el puerto, no SQS.
 */
@Component
public class SqsJobQueueAdapter implements ProcessJobQueuePort {

    private final SqsClient sqs;
    private final String queueName;
    private volatile String cachedQueueUrl;

    public SqsJobQueueAdapter(SqsClient sqs, SqsProperties props) {
        this.sqs = sqs;
        this.queueName = props.getQueueName();
    }

    @Override
    public String enqueue() {
        String body = "{\"type\":\"PROCESS_BUCKET\",\"requestedAt\":\"" + Instant.now() + "\"}";
        var response = sqs.sendMessage(b -> b.queueUrl(queueUrl()).messageBody(body));
        return response.messageId();
    }

    /** Resuelve la URL de la cola por nombre la primera vez y la cachea (evita carreras al arrancar). */
    private String queueUrl() {
        String url = cachedQueueUrl;
        if (url == null) {
            url = sqs.getQueueUrl(b -> b.queueName(queueName)).queueUrl();
            cachedQueueUrl = url;
        }
        return url;
    }
}
