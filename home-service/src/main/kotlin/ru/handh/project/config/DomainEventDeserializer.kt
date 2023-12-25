package ru.handh.project.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Deserializer
import ru.handh.project.model.DomainEvent

class DomainEventDeserializer(
) : Deserializer<DomainEvent> {

    private val mapper = jacksonObjectMapper()

    override fun deserialize(topic: String, data: ByteArray): DomainEvent {
        return mapper.readValue(String(data), DomainEvent::class.java)
    }
}
