package org.eu.liaohongdong.eShop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.eu.liaohongdong.mpg.mapper")
public class EShopAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(EShopAdminApplication.class, args);
    }
}
