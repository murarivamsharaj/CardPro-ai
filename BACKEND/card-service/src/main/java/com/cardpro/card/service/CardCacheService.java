package com.cardpro.card.service;

import com.cardpro.card.dto.response.PublicCardResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@Slf4j
public class CardCacheService {

    private static final String CACHE_PREFIX = "card:profile:v2:";

    @Value("${app.cache.profile-ttl-seconds:300}")
    private long cacheTtlSeconds;

    private final RedisTemplate<String, Object> redisTemplate;

    // Wrapping in Optional completely stops Spring from crashing if the bean is missing
    public CardCacheService(Optional<RedisTemplate<String, Object>> redisTemplateOptional) {
        this.redisTemplate = redisTemplateOptional.orElse(null);
        if (this.redisTemplate == null) {
            log.info("RedisTemplate not configured. CardCacheService running in pass-through mode.");
        }
    }

    public PublicCardResponse getCachedProfile(String slug) {
        if (slug == null || redisTemplate == null) return null;

        try {
            Object cachedValue = redisTemplate.opsForValue().get(CACHE_PREFIX + slug);
            if (cachedValue instanceof PublicCardResponse response) {
                return response;
            }
        } catch (Exception ex) {
            log.warn("Redis cache read failed for slug '{}': {}", slug, ex.getMessage());
        }
        return null;
    }

    public void cacheProfile(String slug, PublicCardResponse response) {
        if (slug == null || response == null || redisTemplate == null) return;

        try {
            redisTemplate.opsForValue().set(
                    CACHE_PREFIX + slug,
                    response,
                    Duration.ofSeconds(cacheTtlSeconds)
            );
        } catch (Exception ex) {
            log.warn("Redis cache write failed for slug '{}': {}", slug, ex.getMessage());
        }
    }

    public void evictCache(String slug) {
        if (slug == null || redisTemplate == null) return;

        try {
            redisTemplate.delete(CACHE_PREFIX + slug);
        } catch (Exception ex) {
            log.warn("Redis cache evict failed for slug '{}': {}", slug, ex.getMessage());
        }
    }
}