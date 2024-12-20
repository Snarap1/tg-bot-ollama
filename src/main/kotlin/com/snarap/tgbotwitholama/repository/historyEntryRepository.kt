package com.snarap.tgbotwitholama.repository

import com.snarap.tgbotwitholama.model.HistoryEntry
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface historyEntryRepository : MongoRepository<HistoryEntry, ObjectId> {
    fun findAllByChatId(chatId: String): List<HistoryEntry>
}