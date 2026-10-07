package org.eu.liaohongdong.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import org.eu.liaohongdong.common.service.RedisService;
import org.eu.liaohongdong.common.service.impl.RedisServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

import java.time.Duration;

// Redis基础配置
public class BaseRedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory,
                                                       RedisSerializer<Object> serializer) {
        RedisTemplate<String, Object> tmp = new RedisTemplate<>();
        tmp.setConnectionFactory(factory);
        tmp.setKeySerializer(new StringRedisSerializer());
        tmp.setValueSerializer(serializer);
        tmp.setHashKeySerializer(new StringRedisSerializer());
        tmp.setHashValueSerializer(serializer);
        tmp.afterPropertiesSet();
        return tmp;
    }

    @Bean
    public RedisSerializer<Object> redisSerializer() {
        // 必须设置，否则无法将JSON转化为对象，会转化成Map类型
        ObjectMapper objectMapper = JsonMapper.builder()
                .changeDefaultVisibility(
                        checker -> checker.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY))
                .activateDefaultTyping(
                        BasicPolymorphicTypeValidator.builder()
                                .allowIfBaseType(Object.class)
                                .allowIfSubType((ctx, clazz) -> true)
                                .build(),
                        DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY)
                .build();
        // 创建JSON序列化器
        return new GenericJacksonJsonRedisSerializer(objectMapper);
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory factory) {
        RedisCacheWriter writer = RedisCacheWriter.nonLockingRedisCacheWriter(factory);
        // 设置Redis缓存有效期为1天
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer()))
                .entryTtl(Duration.ofDays(1));
        return new RedisCacheManager(writer, redisCacheConfiguration);
    }

    @Bean
    public RedisService redisService() {
        return new RedisServiceImpl();
    }
}
