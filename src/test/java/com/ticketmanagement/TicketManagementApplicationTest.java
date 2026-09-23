package com.ticketmanagement;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic Spring context test to verify the application starts correctly.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TicketManagementApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring context loads successfully
        // and the application can start without errors.
        assertThat(true).isTrue();
    }

}
