package io.github.poshjosh.ratelimiter.raas.persistence;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Slf4j
@Service
@EnableConfigurationProperties(AwsS3BucketProperties.class)
public class StorageService {

    private final S3Template s3Template;
    private final AwsS3BucketProperties awsS3BucketProperties;

    public StorageService(S3Template s3Template, AwsS3BucketProperties awsS3BucketProperties) {
        this.s3Template = Objects.requireNonNull(s3Template);
        this.awsS3BucketProperties = Objects.requireNonNull(awsS3BucketProperties);
        if (!s3Template.bucketExists(awsS3BucketProperties.getBucketName())) {
            throw new IllegalArgumentException(
                    "Bucket does not exist: " + awsS3BucketProperties.getBucketName());
        }
    }

    public void save(String objectKey, Path path) throws IOException{
        try (var inputStream = Files.newInputStream(path)) {
            save(objectKey, inputStream);
        }
    }

    public boolean isReadyToBeSaved(Path path, long lastSaveTime) throws IOException {
        if (!Files.exists(path)) {
            log.debug("File does not exist: {}", path);
            return false;
        }
        if (!Files.isReadable(path)) {
            throw new IOException("Not readable: " + path);
        }
        long lastModifiedTime = Files.getLastModifiedTime(path).toMillis();
        return lastModifiedTime > lastSaveTime && Files.size(path) > 0;
    }

    public void save(String objectKey, InputStream content) {
        Objects.requireNonNull(objectKey);
        Objects.requireNonNull(content);
        var bucketName = awsS3BucketProperties.getBucketName();
        s3Template.upload(bucketName, objectKey, content);
    }

    public S3Resource load(String objectKey) {
        var bucketName = awsS3BucketProperties.getBucketName();
        return s3Template.download(bucketName, objectKey);
    }

    public void delete(String objectKey) {
        var bucketName = awsS3BucketProperties.getBucketName();
        s3Template.deleteObject(bucketName, objectKey);
    }
}