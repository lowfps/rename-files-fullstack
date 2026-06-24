package com.bank.filerenamer.adapter.out.storage;

import com.bank.filerenamer.config.S3Properties;
import com.bank.filerenamer.domain.model.S3File;
import com.bank.filerenamer.domain.port.out.FileStoragePort;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.Comparator;
import java.util.List;

/** Adaptador de almacenamiento sobre Amazon S3 (LocalStack en local) con AWS SDK v2. */
@Component
public class S3StorageAdapter implements FileStoragePort {

    private final S3Client s3Client;
    private final String bucket;

    public S3StorageAdapter(S3Client s3Client, S3Properties props) {
        this.s3Client = s3Client;
        this.bucket = props.getBucket();
    }

    @Override
    public List<S3File> listFiles() {
        ListObjectsV2Request request = ListObjectsV2Request.builder().bucket(bucket).build();
        return s3Client.listObjectsV2Paginator(request).contents().stream()
                .map(S3Object::key)
                .sorted(Comparator.naturalOrder())
                .map(S3File::new)
                .toList();
    }

    @Override
    public void putFile(String key, byte[] content) {
        s3Client.putObject(
                PutObjectRequest.builder().bucket(bucket).key(key).build(),
                RequestBody.fromBytes(content));
    }
}
