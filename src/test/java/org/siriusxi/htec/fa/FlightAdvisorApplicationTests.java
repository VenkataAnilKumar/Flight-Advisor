package org.siriusxi.htec.fa;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Disable Flyway for this test to avoid attempting to open the file-backed H2 DB
@SpringBootTest(properties = "spring.flyway.enabled=false")
class FlightAdvisorApplicationTests {

	@Test
	void contextLoads() {
	}

}
