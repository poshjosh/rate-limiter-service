package io.github.poshjosh.ratelimiter.raas.persistence;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@Slf4j
public class S3BucketInitializer implements BeforeAllCallback {

    private static final DockerImageName LOCALSTACK_IMAGE =
            DockerImageName.parse("localstack/localstack:3.4");
    private static final LocalStackContainer localStackContainer = new LocalStackContainer(LOCALSTACK_IMAGE)
            .withCopyFileToContainer(MountableFile.forClasspathResource("init-s3-bucket.sh", 744),
                    "/etc/localstack/init/ready.d/init-s3-bucket.sh")
            .withServices(LocalStackContainer.Service.S3)
            .waitingFor(Wait.forLogMessage(".*Executed init-s3-bucket.sh.*", 1));

    @Override
    public void beforeAll(final ExtensionContext context) {
        log.info("Creating localstack container : {}", LOCALSTACK_IMAGE);
        localStackContainer.start();
        addConfigurationProperties();

        log.info("Successfully started localstack container : {}", LOCALSTACK_IMAGE);
    }

    private void addConfigurationProperties() {
        System.setProperty("spring.cloud.aws.credentials.access-key", localStackContainer.getAccessKey());
        System.setProperty("spring.cloud.aws.credentials.secret-key", localStackContainer.getSecretKey());
        System.setProperty("spring.cloud.aws.s3.region", localStackContainer.getRegion());
        System.setProperty("spring.cloud.aws.s3.endpoint", localStackContainer.getEndpoint().toString());
    }
}