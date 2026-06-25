package com.bank.filerenamer.adapter.in.messaging;

import com.bank.filerenamer.config.SqsProperties;
import com.bank.filerenamer.domain.model.ProcessRun;
import com.bank.filerenamer.domain.port.in.ProcessFilesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueDoesNotExistException;

import java.util.List;

/**
 * Adaptador de <b>entrada</b> asíncrono: sondea la cola SQS (long-polling) y, por cada mensaje,
 * ejecuta el <b>mismo</b> caso de uso {@link ProcessFilesUseCase#process()} que dispara el
 * controlador REST. El motor de reglas y el dominio no cambian: solo se agrega un canal de entrada.
 *
 * <p>Semántica de cola: un mensaje solo se borra si el procesamiento termina bien; si falla, no se
 * borra y SQS lo vuelve a entregar tras el visibility timeout (entrega "al menos una vez").
 */
@Component
public class SqsProcessWorker {

    private static final Logger log = LoggerFactory.getLogger(SqsProcessWorker.class);

    private final SqsClient sqs;
    private final ProcessFilesUseCase processFiles;
    private final SqsProperties props;
    private volatile String cachedQueueUrl;

    public SqsProcessWorker(SqsClient sqs, ProcessFilesUseCase processFiles, SqsProperties props) {
        this.sqs = sqs;
        this.processFiles = processFiles;
        this.props = props;
    }

    @Scheduled(fixedDelayString = "${app.sqs.poll-delay-ms:2000}")
    public void poll() {
        String queueUrl = queueUrlOrNull();
        if (queueUrl == null) {
            return; // la cola aún no existe (arranque); se reintenta en el próximo sondeo
        }

        List<Message> messages = sqs.receiveMessage(b -> b
                .queueUrl(queueUrl)
                .maxNumberOfMessages(props.getMaxMessages())
                .waitTimeSeconds(props.getWaitTimeSeconds())).messages();

        for (Message message : messages) {
            handle(queueUrl, message);
        }
    }

    private void handle(String queueUrl, Message message) {
        try {
            log.info("SQS → procesando trabajo {} ({})", message.messageId(), message.body());
            ProcessRun run = processFiles.process();
            sqs.deleteMessage(b -> b.queueUrl(queueUrl).receiptHandle(message.receiptHandle()));
            log.info("SQS ← trabajo {} completado: ejecución #{} ({} archivos)",
                    message.messageId(), run.id(), run.total());
        } catch (Exception e) {
            // No se borra el mensaje: SQS lo reintenta tras el visibility timeout.
            log.error("Fallo procesando trabajo {}; se reintentará", message.messageId(), e);
        }
    }

    private String queueUrlOrNull() {
        String url = cachedQueueUrl;
        if (url == null) {
            try {
                url = sqs.getQueueUrl(b -> b.queueName(props.getQueueName())).queueUrl();
                cachedQueueUrl = url;
            } catch (QueueDoesNotExistException e) {
                return null;
            }
        }
        return url;
    }
}
