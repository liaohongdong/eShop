package org.eu.liaohongdong.security.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * IgnoreUrlsConfig 的单元测试。
 * 通过 @SpringBootTest 加载 src/test/resources/application.yml，
 * 让 Spring 把 secure.ignored.urls 绑定到 IgnoreUrlsConfig。
 */
@SpringBootTest(classes = IgnoreUrlsConfigTest.TestConfig.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class IgnoreUrlsConfigTest {

    @SpringBootConfiguration
    @EnableConfigurationProperties(IgnoreUrlsConfig.class)
    static class TestConfig {
    }

    @Autowired
    private IgnoreUrlsConfig config;

    @Test
    void bindsUrlsFromApplicationYml() {
        assertEquals(List.of("/auth/login", "/doc.html"), config.getUrls());
    }

    @Test
    void defaultUrls_isEmptyNotNull() {
        IgnoreUrlsConfig fresh = new IgnoreUrlsConfig();

        assertNotNull(fresh.getUrls());
        assertTrue(fresh.getUrls().isEmpty());
    }

    @Test
    void setUrls_thenGetUrls_returnsSameList() {
        IgnoreUrlsConfig fresh = new IgnoreUrlsConfig();
        List<String> urls = List.of("/a", "/b");

        fresh.setUrls(urls);

        assertEquals(urls, fresh.getUrls());
    }
}
