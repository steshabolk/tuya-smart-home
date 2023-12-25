package ru.handh.project

import com.tuya.connector.spring.annotations.ConnectorScan
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConnectorScan(basePackages = ["ru.handh.project.connector"])
@SpringBootApplication
@ConfigurationPropertiesScan
class ProjectApplication

fun main(args: Array<String>) {
	runApplication<ProjectApplication>(*args)
}
