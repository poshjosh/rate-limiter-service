package io.github.poshjosh.ratelimiter.raas.persistence;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ToString
@ConfigurationProperties(prefix = "app.aws.s3")
public class AwsS3BucketProperties {

    @NotBlank(message = "S3 bucket name is required")
    private String bucketName;
}