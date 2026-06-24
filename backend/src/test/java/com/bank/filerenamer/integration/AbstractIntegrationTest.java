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

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    static final LocalStackContainer LOCALSTACK =
            new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
                    .withServices(LocalStackContainer.Service.S3);

    static {
        POSTGRES.start();
        LOCALSTACK.start();
        try {
            LOCALSTACK.execInContainer("awslocal", "s3api", "create-bucket", "--bucket", BUCKET);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo crear el bucket de prueba", e);
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
    }
}
