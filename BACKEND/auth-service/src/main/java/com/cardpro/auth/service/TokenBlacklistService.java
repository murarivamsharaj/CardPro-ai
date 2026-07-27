package com.cardpro.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";
    private final RedisTemplate<String, String> redisTemplate;

    public void blacklistToken(String tokenId, long expirationMs) {
        redisTemplate.opsForValue().set(
            BLACKLIST_PREFIX + tokenId,
            "blacklisted",
            expirationMs,
            TimeUnit.MILLISECONDS
        );
    }

    public boolean isTokenBlacklisted(String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + tokenId));
    }
}
