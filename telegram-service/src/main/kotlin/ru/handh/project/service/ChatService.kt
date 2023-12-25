package ru.handh.project.service

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.handh.device.client.model.CommandDtoGen
import ru.handh.device.client.model.DeviceControlRequestGen
import ru.handh.project.client.DeviceClient
import ru.handh.project.client.HomeClient
import ru.handh.project.client.UserClient
import ru.handh.project.dto.DeviceCommandDto
import ru.handh.project.dto.ReplyMessageDto
import ru.handh.project.entity.ChatEntity
import ru.handh.project.enum.ActionType
import ru.handh.project.enum.DeviceCommand
import ru.handh.project.repository.ChatRepository
import ru.handh.user.client.model.SimpleTelegramTokenDtoGen

private val log = KotlinLogging.logger() {}

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val userClient: UserClient,
    private val homeClient: HomeClient,
    private val deviceClient: DeviceClient,
) {

    companion object {
        private val reply = ThreadLocal<ReplyMessageDto>()
        fun getReply(): ReplyMessageDto? = reply.get()
        fun setReply(messageId: Int, action: ActionType) =
            reply.set(ReplyMessageDto(messageId = messageId + 1, actionType = action))

        fun removeReply() = reply.remove()

        private val home = ThreadLocal<Int>()
        fun getHome(): Int? = home.get()
        fun setHome(value: Int) = home.set(value)
        fun removeHome() = home.remove()

        private val device = ThreadLocal<Int>()
        fun getDevice(): Int? = device.get()
        fun setDevice(value: Int) = device.set(value)
        fun removeDevice() = device.remove()

        private val deviceCommand = ThreadLocal<DeviceCommandDto>()
        fun getDeviceCommand(): DeviceCommandDto? = deviceCommand.get()
        fun setDeviceCommand(messageId: Int, command: DeviceCommand) =
            deviceCommand.set(
                DeviceCommandDto(
                    messageId = messageId + 1,
                    deviceCommand = command
                )
            )

        fun removeDeviceCommand() = deviceCommand.remove()
    }

    @Transactional(readOnly = true)
    fun findByChatId(chatId: Long) =
        chatRepository.findByChatId(chatId)

    @Transactional(readOnly = true)
    fun existsByChatId(chatId: Long) =
        chatRepository.existsByChatId(chatId)

    @Transactional
    fun deleteByChatId(chatId: Long) =
        chatRepository.deleteByChatId(chatId)
            .also {
                log.debug { "user logged out of the chat{id=$chatId}" }
            }

    fun authByToken(token: String, chatId: Long) =
        userClient.authByTelegramToken(SimpleTelegramTokenDtoGen(token))
            .let {
                chatRepository.save(ChatEntity(chatId = chatId, userId = it.userId))
            }
            .also {
                log.debug { "user logged in to the chat{id=$chatId}" }
            }

    fun getHomes(userId: Int) =
        homeClient.getHomes(userId)

    fun getDevices(userId: Int, homeId: Int) =
        deviceClient.getDevices(userId = userId, homeId = homeId)

    fun getDevice(userId: Int, deviceId: Int) =
        deviceClient.getDevice(userId = userId, deviceId = deviceId)

    fun editDeviceStatus(userId: Int, deviceId: Int, command: CommandDtoGen) =
        deviceClient.editDeviceStatus(
            userId = userId, deviceId = deviceId, deviceControlRequestGen = DeviceControlRequestGen(
                listOf(command)
            )
        )
}
