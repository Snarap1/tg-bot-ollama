package com.snarap.tgbotwitholama.repository

import com.snarap.tgbotwitholama.model.HistoryEntry
import com.snarap.tgbotwitholama.model.UserInfo
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface UserInfoRepository : MongoRepository<UserInfo, ObjectId> {
    fun findUserInfoByChatId(chatId: String): UserInfo
}