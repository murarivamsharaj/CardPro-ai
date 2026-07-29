package com.cardpro.card.service;

import com.cardpro.card.dto.response.PublicCardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CardCacheService {

    private static final String CACHE_PREFIX = "card:profile:";

    // Reads from application.yml, defaults to 300 if missing
    @Value("${app.cache.profile-ttl-seconds:300}")
    private long cacheTtlSeconds;

    private final RedisTemplate<String, Object> redisTemplate;

    public PublicCardResponse getCachedProfile(String slug) {
        if (slug == null) return null;

        Object cachedValue = redisTemplate.opsForValue().get(CACHE_PREFIX + slug);
        if (cachedValue instanceof PublicCardResponse response) {
            return response;
        }
        return null;
    }

    public void cacheProfile(String slug, PublicCardResponse response) {
        if (slug != null && response != null) {
            redisTemplate.opsForValue().set(
                    CACHE_PREFIX + slug,
                    response,
                    Duration.ofSeconds(cacheTtlSeconds) // Uses YAML value
            );
        }
    }

    public void evictCache(String slug) {
        if (slug != null) {
            redisTemplate.delete(CACHE_PREFIX + slug);
        }
    }
}
