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
import com.snarap.tgbotwitholama.service.UserInfoService
import org.springframework.stereotype.Component

@BotController
@Component
class BotController(
  private val telegramBot: TelegramBot,
  private val botService: BotService,
  private val userInfoService: UserInfoService
) : TelegramMvcController {

  override fun getToken(): String {
    return telegramBot.token
  }

  @BotRequest(value = ["/start"], type = [MessageType.MESSAGE])
  fun onStartCommand(user: User, chat: Chat): SendMessage {
    botService.sendStartMessage(chat)
    botService.choseLanguage(chat.id().toString())
    return SendMessage(chat.id(), "")
  }

  @MessageRequest("*")
  fun onUserMessage(user: User, chat: Chat, message: String){
    val chatId: String = chat.id().toString()
    val userInfo = userInfoService.findUserInfoByChatId(chatId)
    if (userInfo.languageLevel.isNullOrEmpty() || userInfo.languageForLearning.isNullOrEmpty())
      botService.repeatSettingsMessage(chatId)
    else
      botService.onUserMessage(chatId, message)
  }

  @BotRequest(type = [MessageType.CALLBACK_QUERY])
  fun handleUpdate(update: Update) {
    if (update.callbackQuery() != null) {
      botService.handleCallback(update)
    }
  }
}