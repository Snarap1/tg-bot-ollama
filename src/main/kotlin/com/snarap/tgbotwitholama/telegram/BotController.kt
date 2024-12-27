package com.snarap.tgbotwitholama.telegram

import com.github.kshashov.telegram.api.MessageType
import com.github.kshashov.telegram.api.TelegramMvcController
import com.github.kshashov.telegram.api.bind.annotation.BotController
import com.github.kshashov.telegram.api.bind.annotation.BotRequest
import com.github.kshashov.telegram.api.bind.annotation.request.MessageRequest
import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.request.SendMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@BotController
@Component
class BotController (
  private val telegramBot: TelegramBot,
  private val botService: BotService
): TelegramMvcController {

  override fun getToken(): String {
    return telegramBot.token
  }

  @BotRequest(value = ["/start"], type = [MessageType.MESSAGE])
  fun onStartCommand(user: User, chat: Chat): SendMessage {
    botService.sendStartMessage(chat)
    botService.choseLanguage(user,chat)
    botService.choseLevel(user, chat)
    return SendMessage(chat.id(), "")
  }

  @MessageRequest("*")
  fun onUserMessage(user: User, chat: Chat, message: String): SendMessage {
    return botService.onUserMessage(user,chat, message)
  }

  @BotRequest(type = [MessageType.CALLBACK_QUERY])
  fun handleUpdate(update: Update) {
    if (update.callbackQuery() != null) {
      botService.handleCallback(update)
    }
  }
}