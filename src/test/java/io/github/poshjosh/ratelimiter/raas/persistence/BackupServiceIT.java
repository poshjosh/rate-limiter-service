package io.github.poshjosh.ratelimiter.raas.persistence;

import io.github.poshjosh.ratelimiter.raas.ScheduleConfiguration;
import io.github.poshjosh.ratelimiter.raas.cache.RedisInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(ScheduleConfiguration.class)
@SpringBootTest
@InitializeS3Bucket
class BackupServiceIT implements RedisInitializer {

    @Autowired AppRedisProperties appRedisProperties;

    @MockBean StorageService storageService;

    @Autowired BackupService backupService;

    @Test
    void givenSaveIntervalExceeded_thenSaveMustHaveBeenAttempted()
            throws IOException, InterruptedException {

        when(storageService.isReadyToBeSaved(any(Path.class), anyLong())).thenReturn(false);

        // given save interval exceeded
        Thread.sleep(TimeUnit.SECONDS.toMillis(appRedisProperties.getBackupInterval() + 2));

        // then save must have been attempted
        verify(storageService, atLeastOnce()).isReadyToBeSaved(any(Path.class), anyLong());
    }
}