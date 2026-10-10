package org.eu.liaohongdong;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneratorTest {

    @Test
    void resolveModuleDir_shouldReturnEShopMpgModuleRoot() {
        Path moduleDir = Paths.get(Generator.resolveModuleDir());

        assertTrue(moduleDir.isAbsolute(), "模块目录应为绝对路径");
        assertEquals("eShop-mpg", moduleDir.getFileName().toString(), "应定位到 eShop-mpg 模块根");
        assertTrue(Files.exists(moduleDir.resolve("pom.xml")), "模块根下应存在 pom.xml");
        assertTrue(Files.isDirectory(moduleDir.resolve("src/main/java")), "模块根下应存在 src/main/java");
    }

    @Test
    void resolveModuleDir_shouldNotDependOnWorkingDirectory() {
        String expected = Generator.resolveModuleDir();
        String original = System.getProperty("user.dir");
        try {
            System.setProperty("user.dir", System.getProperty("java.io.tmpdir"));
            assertEquals(expected, Generator.resolveModuleDir(), "结果不应随 user.dir 变化");
        } finally {
            System.setProperty("user.dir", original);
        }
    }
}
