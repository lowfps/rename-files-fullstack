package com.bank.filerenamer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración de la cola SQS para el procesamiento asíncrono. Apunta a LocalStack en local y al
 * SQS real de AWS sin tocar el dominio (solo endpoint/credenciales/región).
 */
@ConfigurationProperties(prefix = "app.sqs")
public class SqsProperties {

    /** Nombre de la cola de trabajos de procesamiento. */
    private String queueName = "process-jobs";
    /** Endpoint SQS; nulo para usar el endpoint real de AWS. */
    private String endpoint;
    private String region = "us-east-1";
    private String accessKey = "test";
    private String secretKey = "test";
    /** Cada cuántos ms el worker sondea la cola. */
    private long pollDelayMs = 2000;
    /** Long-polling: segundos que SQS espera por mensajes antes de responder vacío. */
    private int waitTimeSeconds = 1;
    /** Máximo de mensajes a recibir por sondeo. */
    private int maxMessages = 5;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public long getPollDelayMs() {
        return pollDelayMs;
    }

    public void setPollDelayMs(long pollDelayMs) {
        this.pollDelayMs = pollDelayMs;
    }

    public int getWaitTimeSeconds() {
        return waitTimeSeconds;
    }

    public void setWaitTimeSeconds(int waitTimeSeconds) {
        this.waitTimeSeconds = waitTimeSeconds;
    }

    public int getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }
}
