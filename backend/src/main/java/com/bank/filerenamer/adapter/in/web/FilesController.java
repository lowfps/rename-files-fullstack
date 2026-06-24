package com.bank.filerenamer.adapter.in.web;

import com.bank.filerenamer.domain.model.S3File;
import com.bank.filerenamer.domain.port.in.BucketUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lectura del bucket y simulación de carga batch. */
@RestController
@RequestMapping("/api/files")
public class FilesController {

    private final BucketUseCase bucket;

    public FilesController(BucketUseCase bucket) {
        this.bucket = bucket;
    }

    @GetMapping
    public List<String> list() {
        return bucket.listFiles().stream().map(S3File::key).toList();
    }

    @PostMapping("/seed")
    public List<String> seed() {
        return bucket.seedSampleFiles();
    }
}
