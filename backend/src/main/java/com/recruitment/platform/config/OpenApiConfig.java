package com.recruitment.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI-Powered Recruitment & Job Matching Platform API")
                        .version("1.0.0")
                        .description("REST API documentation for Resume Parsing, Intelligent Job Matching, Candidate Ranking, and Hiring Workflow Automation.")
                        .contact(new Contact()
                                .name("Platform Engineering Team")
                                .email("recruitment-ai@example.com"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));
    }
}
