package com.bank.filerenamer.application;

import com.bank.filerenamer.domain.port.out.ProcessJobQueuePort;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * El servicio de encolado solo debe delegar en el puerto de cola y devolver su id de trabajo,
 * sin conocer la tecnología subyacente (SQS).
 */
class EnqueueProcessServiceTest {

    @Test
    void delegatesToQueueAndReturnsJobId() {
        ProcessJobQueuePort queue = mock(ProcessJobQueuePort.class);
        when(queue.enqueue()).thenReturn("msg-123");

        EnqueueProcessService service = new EnqueueProcessService(queue);
        String jobId = service.enqueue();

        assertThat(jobId).isEqualTo("msg-123");
        verify(queue).enqueue();
    }
}
