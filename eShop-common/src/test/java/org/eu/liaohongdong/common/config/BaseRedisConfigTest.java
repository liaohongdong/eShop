package org.eu.liaohongdong.common.config;

import org.eu.liaohongdong.common.service.RedisService;
import org.eu.liaohongdong.common.service.impl.RedisServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class BaseRedisConfigTest {

    private final BaseRedisConfig config = new BaseRedisConfig();

    static class User {
        private String name;
        private int age;
        private List<String> tags;
    }

    @Test
    void redisSerializer_roundTripsString() {
        RedisSerializer<Object> serializer = config.redisSerializer();

        Object back = serializer.deserialize(serializer.serialize("hello"));

        assertEquals("hello", back);
    }

    @Test
    void redisSerializer_roundTripsArrayListWithTypeHint() {
        RedisSerializer<Object> serializer = config.redisSerializer();
        List<String> src = new ArrayList<>(List.of("a", "b"));

        byte[] bytes = serializer.serialize(src);
        Object back = serializer.deserialize(bytes);

        assertTrue(new String(bytes, StandardCharsets.UTF_8).startsWith("[\"java.util.ArrayList\""),
                "array-shaped types fall back to wrapper-array type hint");
        assertInstanceOf(ArrayList.class, back);
        assertEquals(src, back);
    }

    @Test
    void redisSerializer_roundTripsHashMap() {
        RedisSerializer<Object> serializer = config.redisSerializer();
        Map<String, Object> src = new HashMap<>();
        src.put("k", 123);

        byte[] bytes = serializer.serialize(src);
        Object back = serializer.deserialize(bytes);

        assertTrue(new String(bytes, StandardCharsets.UTF_8).contains("\"@class\""),
                "object-shaped types must carry @class hint");
        assertInstanceOf(HashMap.class, back);
        assertEquals(src, back);
    }

    @Test
    void redisSerializer_roundTripsPojoWithPrivateFieldsNoGetters() {
        RedisSerializer<Object> serializer = config.redisSerializer();
        User src = new User();
        src.name = "张三";
        src.age = 18;
        src.tags = new ArrayList<>(List.of("java", "redis"));

        Object back = serializer.deserialize(serializer.serialize(src));

        assertInstanceOf(User.class, back);
        User user = (User) back;
        assertEquals("张三", user.name);
        assertEquals(18, user.age);
        assertEquals(List.of("java", "redis"), user.tags);
    }

    @Test
    void redisSerializer_nullBecomesEmptyBytesAndBack() {
        RedisSerializer<Object> serializer = config.redisSerializer();

        byte[] bytes = serializer.serialize(null);

        assertEquals(0, bytes.length);
        assertNull(serializer.deserialize(bytes));
    }

    @Test
    void redisSerializer_deserializeWithTargetClass() {
        RedisSerializer<Object> serializer = config.redisSerializer();

        ArrayList<?> back = (ArrayList<?>) serializer.deserialize(
                serializer.serialize(new ArrayList<>(List.of("x"))), ArrayList.class);

        assertEquals(List.of("x"), back);
    }

    @Test
    void redisTemplate_wiresSerializersAndFactory() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisSerializer<Object> serializer = config.redisSerializer();

        RedisTemplate<String, Object> template = config.redisTemplate(factory, serializer);

        assertSame(factory, template.getConnectionFactory());
        assertInstanceOf(StringRedisSerializer.class, template.getKeySerializer());
        assertInstanceOf(StringRedisSerializer.class, template.getHashKeySerializer());
        assertSame(serializer, template.getValueSerializer());
        assertSame(serializer, template.getHashValueSerializer());
    }

    @Test
    void redisCacheManager_entryTtlIsOneDay() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);

        RedisCacheManager manager = config.redisCacheManager(factory);
        RedisCache cache = (RedisCache) manager.getCache("test-cache");

        assertNotNull(cache);
        assertEquals(Duration.ofDays(1),
                cache.getCacheConfiguration().getTtlFunction().getTimeToLive("key", "value"));
    }

    @Test
    void redisService_returnsRedisServiceImpl() {
        RedisService service = config.redisService();

        assertInstanceOf(RedisServiceImpl.class, service);
    }
}
