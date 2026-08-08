package com.cardpro.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private final RedisTemplate<String, String> redisTemplate;

    public void blacklistToken(String tokenId, long expirationMs) {
        // TEMPORARY BYPASS: Uncomment the code below once Redis is running locally or via Docker
        log.warn("Token blacklisting is temporarily bypassed. Redis is not connected.");

        /*
        redisTemplate.opsForValue().set(
            BLACKLIST_PREFIX + tokenId,
            "blacklisted",
            expirationMs,
            TimeUnit.MILLISECONDS
        );
        */
    }

    public boolean isTokenBlacklisted(String tokenId) {
        // TEMPORARY BYPASS: Always return false since Redis is not running
        log.warn("Token blacklist check is temporarily bypassed. Assuming token is valid.");
        return false;

        /*
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + tokenId));
        */
    }
}