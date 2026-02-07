package com.yx.note_app.config;

import com.yx.note_app.security.RefreshTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);

    private final RefreshTokenService refreshTokenService;

    public ScheduledTasks(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredRefreshTokens() {
        logger.info("Starting expired refresh token cleanup");
        refreshTokenService.cleanupExpiredTokens();
        logger.info("Expired refresh token cleanup completed");
    }
}
