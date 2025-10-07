package org.siriusxi.htec.fa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.main.banner-mode=off",
        "logging.level.org.springframework=WARN",
        "logging.level.org.hibernate=WARN"
    }
)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yaml")
class FlightAdvisorApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring application context loads successfully
		// with the test configuration and in-memory H2 database
	}

}
