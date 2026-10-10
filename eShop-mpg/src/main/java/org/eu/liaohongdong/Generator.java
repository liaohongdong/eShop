package org.eu.liaohongdong;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collections;

// MPG 代码生成工具
public class Generator {
    public static void main(String[] args) {
        YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
        try {
            PropertySource<?> source = loader.load("config file yml", new ClassPathResource("application.yml")).get(0);
            String dbUrl = (String) source.getProperty("generator.db.url");
            String dbUser = (String) source.getProperty("generator.db.username");
            String dbPwd = resolveDbPassword((String) source.getProperty("generator.db.password"));
            String author = (String) source.getProperty("generator.author");
            String parentPackage = (String) source.getProperty("generator.parentPackage");
            String moduleName = (String) source.getProperty("generator.moduleName");
            String tableStr = (String) source.getProperty("generator.tableList");
            String[] tables = tableStr.split(",");
            String moduleDir = resolveModuleDir();
            FastAutoGenerator.create(dbUrl, dbUser, dbPwd)
                    .globalConfig(builder -> {
                        builder.author(author)
                                .outputDir(moduleDir + "/src/main/java")
                                .disableOpenDir();
                    })
                    .packageConfig(builder -> {
                        builder.parent(parentPackage)
                                .moduleName(moduleName)
                                .pathInfo(Collections.singletonMap(
                                        OutputFile.xml,
                                        moduleDir + "/src/main/resources/mapper"
                                ));
                    })
                    .strategyConfig(builder -> {
                        builder.addInclude(tables)
                                .entityBuilder()
                                .enableLombok()
                                .enableTableFieldAnnotation()
                                .logicDeleteColumnName("deleted")
                                .versionColumnName("version");
                        builder.mapperBuilder().mapperAnnotation(Mapper.class);
//                        builder.controllerBuilder().enableRestStyle();
                    })
                    .execute();
            System.out.println("代码生成完成！");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 密码优先取环境变量 DB_PASSWORD；yml 中为占位符或为空时同样走环境变量，避免明文入库。
     */
    private static String resolveDbPassword(String configured) {
        if (configured == null || configured.isBlank() || configured.startsWith("${")) {
            String env = System.getenv("DB_PASSWORD");
            if (env == null || env.isBlank()) {
                throw new IllegalStateException("请在环境变量 DB_PASSWORD 中配置数据库密码");
            }
            return env;
        }
        return configured;
    }

    /**
     * 由本类的编译输出位置反推当前模块根目录，避免依赖运行工作目录。
     * 运行时 class 位于 .../eShop-mpg/target/classes/，向上两级即模块根。
     */
    static String resolveModuleDir() {
        try {
            return Paths.get(Generator.class.getProtectionDomain()
                            .getCodeSource().getLocation().toURI())
                    .getParent()
                    .getParent()
                    .toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("无法定位模块目录", e);
        }
    }
}
