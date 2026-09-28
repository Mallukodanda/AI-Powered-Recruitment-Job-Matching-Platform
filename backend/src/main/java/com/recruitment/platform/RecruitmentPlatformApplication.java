package com.recruitment.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RecruitmentPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecruitmentPlatformApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" AI-Powered Recruitment Platform is running on http://localhost:8080");
        System.out.println(" Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println(" H2 Console: http://localhost:8080/h2-console");
        System.out.println("==================================================================");
    }
}
