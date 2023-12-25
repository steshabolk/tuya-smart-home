package ru.handh.project.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
@EnableKafka
@ConditionalOnProperty(name = ["kafka.enabled"], havingValue = "true")
class KafkaConfig(
    private val topicsProps: KafkaTopicsProperties,

    @Value(value = "\${spring.kafka.bootstrap-servers}")
    private val bootstrapServer: String
) {
    @Bean
    fun createKafkaTopics() =
        topicsProps.topics
            .map {
                TopicBuilder
                    .name(it.value.name)
                    .partitions(it.value.partitions)
                    .replicas(it.value.replicas)
                    .build()
            }.let {
                KafkaAdmin.NewTopics(*it.toTypedArray())
            }

    @Bean
    fun producerFactory(): ProducerFactory<String, String> {
        val props: Map<String, Any> = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
        )
        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate(producerFactory())
    }
}
