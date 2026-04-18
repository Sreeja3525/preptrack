package com.preptrack;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PreptrackApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the entire Spring context loads without errors
        // If any bean is misconfigured, this test will catch it
    }
}
