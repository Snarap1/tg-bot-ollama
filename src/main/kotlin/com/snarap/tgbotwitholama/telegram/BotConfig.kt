package com.snarap.tgbotwitholama.telegram

import com.pengrad.telegrambot.TelegramBot
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class BotConfig(
    @Value("\${telegram.bots.myBot.token}") private val botToken: String
) {
    @Bean
    fun telegramBot(): TelegramBot {
        return TelegramBot(botToken)
    }
}