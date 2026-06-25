package com.bank.filerenamer.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base de los tests de integración: levanta PostgreSQL y LocalStack (S3) reales en contenedores
 * y crea el bucket de origen antes de las pruebas.
 */
@SpringBootTest
@Testcontainers
public abstract class AbstractIntegrationTest {

    protected static final String BUCKET = "incoming-files";
    protected static final String QUEUE = "process-jobs";

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    static final LocalStackContainer LOCALSTACK =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.5"))
                    .withServices(LocalStackContainer.Service.S3, LocalStackContainer.Service.SQS);

    static {
        POSTGRES.start();
        LOCALSTACK.start();
        try {
            LOCALSTACK.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", BUCKET);
            LOCALSTACK.execInContainer("awslocal", "sqs", "create-queue", "--queue-name", QUEUE);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudieron crear los recursos AWS de prueba", e);
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);

        registry.add("app.s3.bucket", () -> BUCKET);
        registry.add("app.s3.endpoint", () -> LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        registry.add("app.s3.region", LOCALSTACK::getRegion);
        registry.add("app.s3.access-key", LOCALSTACK::getAccessKey);
        registry.add("app.s3.secret-key", LOCALSTACK::getSecretKey);

        registry.add("app.sqs.queue-name", () -> QUEUE);
        registry.add("app.sqs.endpoint", () -> LOCALSTACK.getEndpointOverride(LocalStackContainer.Service.SQS).toString());
        registry.add("app.sqs.region", LOCALSTACK::getRegion);
        registry.add("app.sqs.access-key", LOCALSTACK::getAccessKey);
        registry.add("app.sqs.secret-key", LOCALSTACK::getSecretKey);
        registry.add("app.sqs.poll-delay-ms", () -> "500");
    }
}
