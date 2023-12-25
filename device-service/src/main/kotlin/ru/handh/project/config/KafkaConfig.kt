package ru.handh.project.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaAdmin
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import ru.handh.project.model.DomainEvent

@Configuration
@EnableKafka
@ConditionalOnProperty(name = ["kafka.enabled"], havingValue = "true")
class KafkaConfig(
    private val topicsProps: KafkaTopicsProperties,

    @Value(value = "\${spring.kafka.bootstrap-servers}")
    private val bootstrapServer: String,

    @Value("\${spring.application.name}")
    private val appName: String
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
    fun kafkaDomainEventListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, DomainEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, DomainEvent>()
        factory.consumerFactory = kafkaDomainEventConsumerFactory()
        return factory
    }

    @Bean
    fun kafkaDomainEventConsumerFactory(): ConsumerFactory<String, DomainEvent> {
        val props: Map<String, Any> = mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServer,
            ConsumerConfig.GROUP_ID_CONFIG to appName,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to ErrorHandlingDeserializer::class.java,
            ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS to DomainEventDeserializer::class.java,
        )
        return DefaultKafkaConsumerFactory(props)
    }
}
