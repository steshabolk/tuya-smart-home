package ru.handh.project

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@EnableAutoConfiguration(exclude = [KafkaAutoConfiguration::class])
class ProjectApplicationTests {

	@Test
	fun contextLoads() {
	}

}
