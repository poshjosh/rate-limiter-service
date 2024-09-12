package io.github.poshjosh.ratelimiter.raas.persistence;

import io.awspring.cloud.s3.S3Template;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile({"default", "dev", "prod", "!test"})
@Component
@RequiredArgsConstructor
public class S3PostSetup {
    private final S3Template s3Template;
    private final AwsS3BucketProperties awsS3BucketProperties;

    @PostConstruct
    public void checkBucketExists() {
        if (!s3Template.bucketExists(awsS3BucketProperties.getBucketName())) {
            throw new IllegalArgumentException(
                    "Bucket does not exist: " + awsS3BucketProperties.getBucketName());
        }
    }
}
