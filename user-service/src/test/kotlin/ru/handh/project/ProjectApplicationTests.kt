package ru.handh.project

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import ru.handh.project.util.JWKServiceMock

@SpringBootTest
@EnableAutoConfiguration(exclude = [KafkaAutoConfiguration::class])
@ContextConfiguration(classes = [JWKServiceMock::class])
class ProjectApplicationTests {

	@Test
	fun contextLoads() {
	}

}
