package com.droplink.config;

import com.droplink.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulerConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);

    private final FileService fileService;

    public SchedulerConfig(FileService fileService) {
        this.fileService = fileService;
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanExpiredFiles() {
        log.info("开始清理过期文件...");
        try {
            fileService.cleanExpiredFiles();
            log.info("过期文件清理完成");
        } catch (Exception e) {
            log.error("清理过期文件失败", e);
        }
    }
}
