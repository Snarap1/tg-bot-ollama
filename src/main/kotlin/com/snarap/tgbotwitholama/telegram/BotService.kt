package com.snarap.tgbotwitholama.telegram

import com.pengrad.telegrambot.TelegramBot
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.request.SendMessage
import com.snarap.tgbotwitholama.model.UserInfo
import com.snarap.tgbotwitholama.repository.UserInfoRepository
import com.snarap.tgbotwitholama.service.ChatBotService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component

@Component
class BotService(
  private val telegramBot: TelegramBot,
  private val aiService: ChatBotService,
  private val userInfoRepository: UserInfoRepository,
  private val mongoTemplate: MongoTemplate
) {
  private val logger: Logger = LoggerFactory.getLogger(BotService::class.java)

  fun sendStartMessage(chat: Chat) {
    val chatId = chat.id()
    val welcomeMessage = "Добро пожаловать! 🎉 Я твой Бот-помощник в изучении программирования! Для начала давай " +
        "настроим меня!"
    telegramBot.execute(SendMessage(chatId, welcomeMessage))
  }

  fun choseLanguage(user: User, chat: Chat) {
    val chatId = chat.id()

    val button1 = InlineKeyboardButton("Java").callbackData("language: Java")
    val button2 = InlineKeyboardButton("C++").callbackData("language: C++")
    val button3 = InlineKeyboardButton("Python").callbackData("language: Python")

    val keyboard = InlineKeyboardMarkup(arrayOf(button1, button2), arrayOf(button3))

    telegramBot.execute(
      SendMessage(chatId, "Выберите язык программирования для изучения:")
        .replyMarkup(keyboard)
    )
  }

  //todo сделать вызов
  fun choseLevel(user: User, chat: Chat) {
    val chatId = chat.id()

    val button1 = InlineKeyboardButton("Начальный").callbackData("level: beginner")
    val button2 = InlineKeyboardButton("Продвинутый").callbackData("level: advanced")

    val keyboard = InlineKeyboardMarkup(arrayOf(button1, button2))

    telegramBot.execute(
      SendMessage(chatId, "Выберите язык программирования для изучения:")
        .replyMarkup(keyboard)
    )
  }

  fun handleCallback(update: Update){
    val callbackQuery = update.callbackQuery()
    val queryData = callbackQuery.data()
    val chatId = callbackQuery.message().chat().id().toString()
    //todo get userInfo to form
    if(queryData.contains("language"))
      upsertUserInfo(UserInfo(chatId, queryData))
    else if (queryData.contains("level"))
      upsertUserInfo(UserInfo(chatId = chatId, languageLevel = queryData))

    telegramBot.execute(SendMessage(chatId,"Ваш ответ сохранён"))
  }

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

  private fun upsertUserInfo(userInfo: UserInfo) {
    val query = Query(Criteria.where("chatId").`is`(userInfo.chatId))
    val update = org.springframework.data.mongodb.core.query.Update()
      .set("languageForLearning", userInfo.languageForLearning)
      .set("languageLevel", userInfo.languageLevel)
    mongoTemplate.upsert(query, update, UserInfo::class.java)
    logger.info("UserInfo updated ${userInfo.chatId}")
  }



}