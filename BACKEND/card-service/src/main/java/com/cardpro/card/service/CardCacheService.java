package com.cardpro.card.service;

import com.cardpro.card.dto.response.PublicCardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardCacheService {

    // Versioned prefix: bump the version whenever the cached DTO shape changes
    // (e.g. adding `id` to PublicCardResponse) so stale entries written by an
    // older build are ignored instead of served without the new fields.
    private static final String CACHE_PREFIX = "card:profile:v2:";

    // Reads from application.yml, defaults to 300 if missing
    @Value("${app.cache.profile-ttl-seconds:300}")
    private long cacheTtlSeconds;

    private final RedisTemplate<String, Object> redisTemplate;

    public PublicCardResponse getCachedProfile(String slug) {
        if (slug == null) return null;

        try {
            Object cachedValue = redisTemplate.opsForValue().get(CACHE_PREFIX + slug);
            if (cachedValue instanceof PublicCardResponse response) {
                return response;
            }
        } catch (Exception ex) {
            // Redis is optional: treat unavailability as a cache miss
            log.warn("Redis cache read failed for slug '{}': {}", slug, ex.getMessage());
        }
        return null;
    }

    public void cacheProfile(String slug, PublicCardResponse response) {
        if (slug == null || response == null) return;

        try {
            redisTemplate.opsForValue().set(
                    CACHE_PREFIX + slug,
                    response,
                    Duration.ofSeconds(cacheTtlSeconds) // Uses YAML value
            );
        } catch (Exception ex) {
            // Redis is optional: log and continue without caching
            log.warn("Redis cache write failed for slug '{}': {}", slug, ex.getMessage());
        }
    }

    public void evictCache(String slug) {
        if (slug == null) return;

        try {
            redisTemplate.delete(CACHE_PREFIX + slug);
        } catch (Exception ex) {
            // Redis is optional: log and continue without eviction
            log.warn("Redis cache evict failed for slug '{}': {}", slug, ex.getMessage());
        }
    }
}
