package com.bank.filerenamer.application;

import com.bank.filerenamer.domain.model.S3File;
import com.bank.filerenamer.domain.port.in.BucketUseCase;
import com.bank.filerenamer.domain.port.out.FileStoragePort;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Operaciones sobre el bucket de origen. La siembra de muestra apoya la demostración end-to-end.
 */
public class BucketService implements BucketUseCase {

    private final FileStoragePort fileStorage;
    private final List<String> sampleFiles;

    public BucketService(FileStoragePort fileStorage, List<String> sampleFiles) {
        this.fileStorage = fileStorage;
        this.sampleFiles = List.copyOf(sampleFiles);
    }

    @Override
    public List<S3File> listFiles() {
        return fileStorage.listFiles();
    }

    @Override
    public List<String> seedSampleFiles() {
        byte[] content = "archivo de muestra".getBytes(StandardCharsets.UTF_8);
        sampleFiles.forEach(name -> fileStorage.putFile(name, content));
        return sampleFiles;
    }
}
