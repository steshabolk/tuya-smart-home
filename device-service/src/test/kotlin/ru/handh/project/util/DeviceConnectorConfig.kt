package ru.handh.project.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import ru.handh.project.connector.DeviceConnector
import ru.handh.project.dto.tuya.TuyaCommand
import ru.handh.project.dto.tuya.TuyaDevice
import ru.handh.project.dto.tuya.TuyaSendCommandsRequest
import java.nio.charset.StandardCharsets

@Configuration
class DeviceConnectorConfig {

    companion object {
        private val mapper = jacksonObjectMapper()

        val deviceDetails: TuyaDevice = mapper.readValue(
            DeviceConnectorConfig::class.java.classLoader.getResource("deviceDetails")?.readText(StandardCharsets.UTF_8),
            TuyaDevice::class.java
        )
        val deviceStatus: List<TuyaCommand> = mapper.readValue(
            DeviceConnectorConfig::class.java.classLoader.getResource("deviceStatus")?.readText(StandardCharsets.UTF_8),
            object : TypeReference<List<TuyaCommand>>() {}
        )
        val sendCommands = true
    }

    @Primary
    @Bean
    fun deviceConnector(): DeviceConnector {
        return object : DeviceConnector {

            override fun getDeviceDetails(deviceId: String): TuyaDevice {
                return deviceDetails
            }

            override fun getDeviceStatus(deviceId: String): List<TuyaCommand> {
                return deviceStatus
            }

            override fun sendCommands(deviceId: String, body: TuyaSendCommandsRequest): Boolean {
                return sendCommands
            }
        }
    }
}
