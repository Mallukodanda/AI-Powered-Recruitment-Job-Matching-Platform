package com.recruitment.platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class RecruitmentPlatformApplicationTests {

    @Test
    void contextLoads() {
        // Validates that the Spring ApplicationContext loads without dependency injection or configuration errors
    }
}
