package ru.handh.project

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import ru.handh.project.util.JWKServiceMock

@SpringBootTest
@ContextConfiguration(classes = [JWKServiceMock::class])
class ProjectApplicationTests {

	@Test
	fun contextLoads() {
	}

}
