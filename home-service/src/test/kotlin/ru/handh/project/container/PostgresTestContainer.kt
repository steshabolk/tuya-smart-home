package ru.handh.project.container

import org.junit.ClassRule
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.PostgreSQLContainer

class PostgresTestContainer {

    companion object {

        @JvmField
        @ClassRule
        val postgresqlContainer: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:15.5")
            .withDatabaseName("integration-tests-db")
            .withUsername("test")
            .withPassword("test")
            .also { it.start() }
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

        override fun initialize(configurableApplicationContext: ConfigurableApplicationContext) {
            TestPropertyValues.of(
                "spring.datasource.url=${postgresqlContainer.jdbcUrl}",
                "spring.datasource.username=${postgresqlContainer.username}",
                "spring.datasource.password=${postgresqlContainer.password}"
            ).applyTo(configurableApplicationContext.environment)
        }
    }
}
