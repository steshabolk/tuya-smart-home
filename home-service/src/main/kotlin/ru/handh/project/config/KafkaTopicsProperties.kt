package ru.handh.project.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "kafka")
class KafkaTopicsProperties {
    val topics: Map<String, Topic> = HashMap()

    class Topic(
        val name: String,
        val partitions: Int,
        val replicas: Int
    )
}
