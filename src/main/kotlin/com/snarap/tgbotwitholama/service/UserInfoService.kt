package com.snarap.tgbotwitholama.service

import com.snarap.tgbotwitholama.model.UserInfo
import com.snarap.tgbotwitholama.repository.UserInfoRepository
import com.snarap.tgbotwitholama.telegram.BotService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class UserInfoService (
  private val userInfoRepository: UserInfoRepository,
  private val mongoTemplate: MongoTemplate
){
  private val logger: Logger = LoggerFactory.getLogger(BotService::class.java)

  fun findUserInfoByChatId(chatId: String): UserInfo {
    return userInfoRepository.findUserInfoByChatId(chatId)
  }

  fun updateLanguage(userInfo: UserInfo) {
    val query = Query(Criteria.where("chatId").`is`(userInfo.chatId))
    val update = Update()
      .set("languageForLearning", userInfo.languageForLearning)
    mongoTemplate.upsert(query, update, UserInfo::class.java)
    logger.info("Language updated for ${userInfo.chatId}")
  }

  fun updateLevel(userInfo: UserInfo) {
    val query = Query(Criteria.where("chatId").`is`(userInfo.chatId))
    val update = Update()
      .set("languageLevel", userInfo.languageLevel)
    mongoTemplate.upsert(query, update, UserInfo::class.java)
    logger.info("Level updated for ${userInfo.chatId}")
  }

}