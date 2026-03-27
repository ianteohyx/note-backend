package com.yx.note_app;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Requires a running MySQL + Kafka — only run as part of full integration testing
@Disabled("Integration test — requires DB and Kafka connections")
@SpringBootTest
class NoteAppApplicationTests {

	@Test
	void contextLoads() {
	}

}
