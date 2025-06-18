package com.ecommerce.productservice.config;

import com.ecommerce.productservice.payload.response.ProductResponseDTO;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig implements CachingConfigurer {

  public static final String CACHE_NAME = "products";
  private static final Logger LOGGER = LoggerFactory.getLogger(RedisConfig.class);

  @Value(value = "${spring.redis.host}")
  private String host;

  @Value(value = "${spring.redis.port}")
  private Integer port;

  @Value(value = "${redis.timeout}")
  private String timeout;

  @Bean
  public JedisConnectionFactory jedisConnectionFactory() {

    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(host);
    redisStandaloneConfiguration.setPort(port);
    JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration =
        JedisClientConfiguration.builder();
    jedisClientConfiguration.connectTimeout(Duration.ofSeconds(Integer.parseInt(timeout)));
    return new JedisConnectionFactory(
        redisStandaloneConfiguration, jedisClientConfiguration.build());
  }

  @Bean
  public RedisTemplate<String, ProductResponseDTO> redisTemplate() {

    final RedisTemplate<String, ProductResponseDTO> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(jedisConnectionFactory());
    redisTemplate.setDefaultSerializer(new GenericJackson2JsonRedisSerializer());
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
    redisTemplate.setHashKeySerializer(new GenericJackson2JsonRedisSerializer());
    redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
    return redisTemplate;
  }

  @Bean
  public RedisCacheManager cacheManager() {

    RedisCacheConfiguration config =
        RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10));

    return RedisCacheManager.builder(jedisConnectionFactory()).cacheDefaults(config).build();
  }

  @Override
  public CacheErrorHandler errorHandler() {

    return new CacheErrorHandler() {
      @Override
      public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {

        LOGGER.error(
            "Failure getting from cache: "
                + cache.getName()
                + ", exception: "
                + exception.toString());
      }

      @Override
      public void handleCachePutError(
          RuntimeException exception, Cache cache, Object key, Object value) {

        LOGGER.error(
            "Failure putting into cache: "
                + cache.getName()
                + ", exception: "
                + exception.toString());
      }

      @Override
      public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {

        LOGGER.error(
            "Failure evicting from cache: "
                + cache.getName()
                + ", exception: "
                + exception.toString());
      }

      @Override
      public void handleCacheClearError(RuntimeException exception, Cache cache) {

        LOGGER.error(
            "Failure clearing cache: " + cache.getName() + ", exception: " + exception.toString());
      }
    };
  }
}
