package com.gymflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class GymFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(GymFlowApplication.class, args);
        System.out.println("GymFlow后端服务启动成功！");
        System.out.println("API文档地址：http://localhost:8080/api/doc.html");
    }
}