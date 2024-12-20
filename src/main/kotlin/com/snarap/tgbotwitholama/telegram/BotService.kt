package com.snarap.tgbotwitholama.telegram

import com.github.kshashov.telegram.api.MessageType
import com.github.kshashov.telegram.api.TelegramMvcController
import com.github.kshashov.telegram.api.bind.annotation.BotController
import com.github.kshashov.telegram.api.bind.annotation.BotRequest
import com.github.kshashov.telegram.api.bind.annotation.request.CallbackQueryRequest
import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.request.GetChatMember
import com.pengrad.telegrambot.request.SendMessage
import com.pengrad.telegrambot.response.GetChatMemberResponse
import com.snarap.tgbotwitholama.service.ChatBotService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component



@BotController
@Component
class BotService(
    private val telegramBot: TelegramBot,
    private val aiService: ChatBotService
) : TelegramMvcController {
    private val logger: Logger = LoggerFactory.getLogger(BotService::class.java)

    override fun getToken(): String {
        return telegramBot.token
    }

    @BotRequest(value = ["/start"], type = [MessageType.CALLBACK_QUERY, MessageType.MESSAGE])
    fun onStartCommand(user: User, chat: Chat): SendMessage {
        return SendMessage(
            chat.id(),
            "Добро пожаловать! 🎉 Я твой Бот-помощник в изучении программирования! Задавай свои вопросы!"
        )
    }

    @MessageRequest("*")
    fun onUserMessage(user: User, chat: Chat, message: String): SendMessage {
        val chatId = chat.id()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val responseText = aiService.call(message, chatId.toString())
                telegramBot.execute(SendMessage(chatId, responseText))
                logger.info("ai response sent")
            } catch (e: Exception) {
                logger.error(e.message)
                telegramBot.execute(SendMessage(chatId, "❌ Произошла ошибка при генерации ответа. Попробуйте снова."))
            }
        }

        return SendMessage(chatId, "Генерирую ответ...")
    }



}