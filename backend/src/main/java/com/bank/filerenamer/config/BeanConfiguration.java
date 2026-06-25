package com.bank.filerenamer.config;

import com.bank.filerenamer.application.BucketService;
import com.bank.filerenamer.application.EnqueueProcessService;
import com.bank.filerenamer.application.ManageRulesService;
import com.bank.filerenamer.application.ProcessFilesService;
import com.bank.filerenamer.application.QueryResultsService;
import com.bank.filerenamer.domain.port.in.BucketUseCase;
import com.bank.filerenamer.domain.port.in.EnqueueProcessUseCase;
import com.bank.filerenamer.domain.port.in.ManageRulesUseCase;
import com.bank.filerenamer.domain.port.in.ProcessFilesUseCase;
import com.bank.filerenamer.domain.port.in.QueryResultsUseCase;
import com.bank.filerenamer.domain.port.out.FileStoragePort;
import com.bank.filerenamer.domain.port.out.ProcessJobQueuePort;
import com.bank.filerenamer.domain.port.out.ResultRepositoryPort;
import com.bank.filerenamer.domain.port.out.RuleRepositoryPort;
import com.bank.filerenamer.domain.service.DateNormalizer;
import com.bank.filerenamer.domain.service.RuleEngine;
import com.bank.filerenamer.domain.service.TemplateRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Punto de cableado (composition root). El dominio y la aplicación no conocen Spring; aquí se
 * construyen sus instancias e inyectan los adaptadores que implementan los puertos.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public BucketUseCase bucketUseCase(FileStoragePort fileStorage, S3Properties s3Properties) {
        return new BucketService(fileStorage, s3Properties.getSampleFiles());
    }

    @Bean
    public DateNormalizer dateNormalizer() {
        return new DateNormalizer();
    }

    @Bean
    public TemplateRenderer templateRenderer() {
        return new TemplateRenderer();
    }

    @Bean
    public RuleEngine ruleEngine(DateNormalizer dateNormalizer, TemplateRenderer templateRenderer) {
        return new RuleEngine(dateNormalizer, templateRenderer);
    }

    @Bean
    public ProcessFilesUseCase processFilesUseCase(FileStoragePort fileStorage,
                                                   RuleRepositoryPort ruleRepository,
                                                   ResultRepositoryPort resultRepository,
                                                   RuleEngine ruleEngine) {
        return new ProcessFilesService(fileStorage, ruleRepository, resultRepository, ruleEngine);
    }

    @Bean
    public EnqueueProcessUseCase enqueueProcessUseCase(ProcessJobQueuePort jobQueue) {
        return new EnqueueProcessService(jobQueue);
    }

    @Bean
    public ManageRulesUseCase manageRulesUseCase(RuleRepositoryPort ruleRepository) {
        return new ManageRulesService(ruleRepository);
    }

    @Bean
    public QueryResultsUseCase queryResultsUseCase(ResultRepositoryPort resultRepository) {
        return new QueryResultsService(resultRepository);
    }
}
