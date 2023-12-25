package ru.handh.project

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession
import ru.handh.project.listener.BotListener

@SpringBootApplication
@ConfigurationPropertiesScan
class ProjectApplication {

    @Bean
    fun initBot(botListener: BotListener): TelegramBotsApi {
        val telegramBotsApi = TelegramBotsApi(DefaultBotSession::class.java)
        telegramBotsApi.registerBot(botListener)
        return telegramBotsApi
    }

}

fun main(args: Array<String>) {
    runApplication<ProjectApplication>(*args)
}
