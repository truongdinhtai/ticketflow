package com.ticketflow.booking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.time.Duration;

/**
 * Redis cache defaults: 60s TTL and JSON value serialization (so cached records
 * are human-readable in Redis and portable across services). Only takes effect
 * when {@code spring.cache.type=redis}; ignored when caching is off.
 */
@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
}
