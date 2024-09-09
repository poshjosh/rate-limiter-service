package io.github.poshjosh.ratelimiter.raas.persistence;

import io.awspring.cloud.s3.S3Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@EnableConfigurationProperties(AppRedisProperties.class)
public class BackupService {
    private static final String REDIS_DB_FILE_NAME = "dump.rdb";

    private final Path redisDataPath;

    private final StorageService storageService;

    public BackupService(AppRedisProperties appRedisProperties, StorageService storageService) {
        this.redisDataPath = Paths.get(appRedisProperties.getDataDir(), REDIS_DB_FILE_NAME);
        this.storageService = storageService;
    }

    public void restoreRedisDataFromBackup() throws IOException {
        log.info("Fetching Redis backup data.");
        S3Resource resource = storageService.load(REDIS_DB_FILE_NAME);
        if (resource == null || !resource.exists()) {
            log.info("Skipping Redis data restoration. Reason: No backup data found.");
            return;
        }
        if (storageService.isReadyToBeSaved(redisDataPath, resource.lastModified())) {
            log.info("Skipping Redis data restoration. Reason: Redis data already present at: {}",
                    redisDataPath);
            return;
        }
        Files.write(redisDataPath, resource.getContentAsByteArray());
        log.info("Restored Redis data from backup to: {}", redisDataPath);
    }

    private long lastSaveTime = 0;

    @Scheduled(
            timeUnit = TimeUnit.SECONDS,
            initialDelayString = "${app.redis.backup-interval}",
            fixedRateString = "${app.redis.backup-interval}")
    public void backupRedisData() {
        try {
            log.trace("Checking if Redis data is ready to be backed up from: {}", redisDataPath);
            if (storageService.isReadyToBeSaved(redisDataPath, lastSaveTime)) {
                lastSaveTime = System.currentTimeMillis();
                storageService.save(REDIS_DB_FILE_NAME, redisDataPath);
                log.debug("Backed up Redis data from {}", redisDataPath);
            }
        } catch (IOException e) {
            log.warn(e.toString());
            log.debug("Failed to backup Redis data from " + redisDataPath, e);
        }
    }
}
