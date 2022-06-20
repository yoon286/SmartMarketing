package com.ecnu.smartmarketing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ecnu.smartmarketing.mapper")
public class SmartMarketingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartMarketingApplication.class, args);
    }


}
