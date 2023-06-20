package hamedshahidi.wordfrequency.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * Configuration class for Redis caching.
 */
@Configuration
@EnableCaching
public class RedisConfig {

    private final RedisConnectionFactory redisConnectionFactory;

    /**
     * Constructs a new RedisConfig with the given RedisConnectionFactory.
     *
     * @param redisConnectionFactory the RedisConnectionFactory to use
     */
    @Autowired
    public RedisConfig(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    /**
     * Creates and configures the CacheManager for Redis caching.
     *
     * @return the CacheManager instance
     */
    @Bean
    public CacheManager cacheManager() {
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues();

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }
}
