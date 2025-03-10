package com.example.wayhome;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@MapperScan("com.example.wayhome.mapper")
public class WayHomeApplication {

    public static void main(String[] args) {
        SpringApplication.run(WayHomeApplication.class, args);
    }

}
