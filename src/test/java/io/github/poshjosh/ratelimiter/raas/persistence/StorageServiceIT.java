package io.github.poshjosh.ratelimiter.raas.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.*;
import java.util.stream.Collectors;

import io.github.poshjosh.ratelimiter.raas.cache.RedisInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Template;
import lombok.SneakyThrows;
import net.bytebuddy.utility.RandomString;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

@SpringBootTest
@InitializeS3Bucket
class StorageServiceIT implements RedisInitializer {

    @Autowired
    private S3Template s3Template;

    @Autowired
    private StorageService storageService;

    @Autowired
    private AwsS3BucketProperties awsS3BucketProperties;

    @Test
    void shouldSaveFileSuccessfullyToBucket() {
        final var key = RandomString.make(10) + ".txt";
        storageService.save(key, givenContent(RandomString.make(50)));
        final var isFileSaved = s3Template.objectExists(awsS3BucketProperties.getBucketName(), key);
        assertThat(isFileSaved).isTrue();
    }

    @Test
    void saveShouldThrowExceptionForNonExistBucket() {
        final var key = RandomString.make(10) + ".txt";
        final var fileToUpload = givenContent(RandomString.make(50));

        final var originalBucketName = awsS3BucketProperties.getBucketName();

        final var nonExistingBucketName = RandomString.make(20).toLowerCase();
        awsS3BucketProperties.setBucketName(nonExistingBucketName);

        final var exception = assertThrows(S3Exception.class, () -> storageService.save(key, fileToUpload));
        assertThat(exception.getCause()).hasCauseInstanceOf(NoSuchBucketException.class);

        // Reset the bucket name to the original value
        awsS3BucketProperties.setBucketName(originalBucketName);
    }

    @Test
    @SneakyThrows
    void shouldLoadSavedFileSuccessfullyFromBucketForValidKey() {
        final var key = RandomString.make(10) + ".txt";
        final var fileContent = RandomString.make(50);
        storageService.save(key, givenContent(fileContent));

        final var loadedObject = storageService.load(key);

        final var retrievedContent = readFile(loadedObject.getContentAsByteArray());
        assertThat(retrievedContent).isEqualTo(fileContent);
    }

    @Test
    void shouldDeleteFileFromBucketSuccessfully() {
        final var key = RandomString.make(10) + ".txt";
        storageService.save(key, givenContent(RandomString.make(50)));

        final var bucketName = awsS3BucketProperties.getBucketName();

        var isFileSaved = s3Template.objectExists(bucketName, key);
        assertThat(isFileSaved).isTrue();

        storageService.delete(key);

        isFileSaved = s3Template.objectExists(bucketName, key);
        assertThat(isFileSaved).isFalse();
    }

    private String readFile(byte[] bytes) {
        final var inputStreamReader = new InputStreamReader(new ByteArrayInputStream(bytes));
        return new BufferedReader(inputStreamReader).lines().collect(Collectors.joining("\n"));
    }

    private InputStream givenContent(final String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
}