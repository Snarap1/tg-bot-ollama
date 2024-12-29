package com.snarap.tgbotwitholama.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.SendMessage
import com.snarap.tgbotwitholama.model.UserInfo
import com.snarap.tgbotwitholama.service.ChatBotService
import com.snarap.tgbotwitholama.service.UserInfoService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BotService(
  private val telegramBot: TelegramBot,
  private val aiService: ChatBotService,
  private val userInfoService: UserInfoService
) {
  private val logger: Logger = LoggerFactory.getLogger(BotService::class.java)

  fun sendStartMessage(chat: Chat) {
    val chatId = chat.id()
    val welcomeMessage = "Добро пожаловать! 🎉 Я твой Бот-помощник в изучении программирования! Для начала давай " +
        "настроим меня!"
    telegramBot.execute(SendMessage(chatId, welcomeMessage))
  }

  fun repeatSettingsMessage(chatId: String) {
    telegramBot.execute(SendMessage(chatId, "Вы еще не прошли настройку. Давайте приступим:"))
    choseLanguage(chatId)
  }

  fun onUserMessage(chatId: String, message: String) {
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
    telegramBot.execute(SendMessage(chatId, "Генерирую ответ..."))
  }

  fun choseLanguage(chatId: String) {
    val button1 = InlineKeyboardButton("Java").callbackData("language: Java")
    val button2 = InlineKeyboardButton("C++").callbackData("language: C++")
    val button3 = InlineKeyboardButton("Python").callbackData("language: Python")

    val keyboard = InlineKeyboardMarkup(arrayOf(button1, button2), arrayOf(button3))

    telegramBot.execute(
      SendMessage(chatId, "Выберите язык программирования для изучения:")
        .replyMarkup(keyboard)
    )
  }

  private fun choseLevel(chatId: String) {
    val button1 = InlineKeyboardButton("Начальный").callbackData("level: beginner")
    val button2 = InlineKeyboardButton("Продвинутый").callbackData("level: advanced")

    val keyboard = InlineKeyboardMarkup(arrayOf(button1, button2))

    telegramBot.execute(
      SendMessage(chatId, "Выберите ваш уровень владения:")
        .replyMarkup(keyboard)
    )
  }

  fun handleCallback(update: Update) {
    val callbackQuery = update.callbackQuery()
    val queryData = callbackQuery.data()
    val chatId = callbackQuery.message().chat().id().toString()

    if (queryData.contains("language"))
      handleLanguageCallback(chatId, queryData)
    else if (queryData.contains("level"))
      handleLevelCallback(chatId, queryData)
  }

  private fun handleLanguageCallback(chatId: String, language: String) {
    userInfoService.updateLanguage(UserInfo(chatId, language))
    telegramBot.execute(SendMessage(chatId, "Ваш ответ сохранён"))
    choseLevel(chatId)
  }

  private fun handleLevelCallback(chatId: String, level: String) {
    userInfoService.updateLevel(UserInfo(chatId = chatId, languageLevel = level))
    telegramBot.execute(SendMessage(chatId, "Ваш ответ сохранён"))
  }
}