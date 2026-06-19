package com.sakny.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Enables Spring's cache abstraction backed by Redis.
 *
 * <p>Values are stored as JSON via {@link GenericJackson2JsonRedisSerializer} so cached entries are
 * human-readable in Redis and round-trip back to their concrete types. Default typing is enabled and
 * the JSR-310 module is registered because cached DTOs carry {@code LocalDate}/{@code LocalDateTime}/
 * {@code BigDecimal} fields.
 *
 * <p>Each cache has its own TTL: reference data (governorates, cities, amenities) is effectively
 * static and kept for a day, single property reads for 30 minutes, and filtered listings for 5
 * minutes since they change more often and are evicted on writes.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String GOVERNORATES = "governorates";
    public static final String CITIES_BY_GOVERNORATE = "citiesByGovernorate";
    public static final String AMENITIES = "amenities";
    public static final String PROPERTIES = "properties";
    public static final String PROPERTY_LISTINGS = "propertyListings";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer valueSerializer =
                new GenericJackson2JsonRedisSerializer(buildObjectMapper());

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(valueSerializer));

        Map<String, RedisCacheConfiguration> caches = new HashMap<>();
        caches.put(GOVERNORATES, defaultConfig.entryTtl(Duration.ofHours(24)));
        caches.put(CITIES_BY_GOVERNORATE, defaultConfig.entryTtl(Duration.ofHours(24)));
        caches.put(AMENITIES, defaultConfig.entryTtl(Duration.ofHours(24)));
        caches.put(PROPERTIES, defaultConfig.entryTtl(Duration.ofMinutes(30)));
        caches.put(PROPERTY_LISTINGS, defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(caches)
                .build();
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // Embed type information so polymorphic / generic values deserialize back to their real types.
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return mapper;
    }
}
