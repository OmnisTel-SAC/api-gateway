package com.omnistel.apigateway;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires RSA key file - tested via JwtAuthFilterTest")
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
