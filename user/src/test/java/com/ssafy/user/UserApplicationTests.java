package com.ssafy.user;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootTest
@EntityScan(basePackages = {"com.ssafy.user.infrastructure.write.persistence.jpa.entity"})
class UserApplicationTests {

	@Test
	void contextLoads() {
	}

}
