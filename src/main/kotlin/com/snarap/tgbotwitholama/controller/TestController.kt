package com.snarap.tgbotwitholama.controller

import com.snarap.tgbotwitholama.model.RequestModel
import com.snarap.tgbotwitholama.model.ResponseModel
import com.snarap.tgbotwitholama.service.ChatBotService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/test")
class TestController (private val chatBot: ChatBotService) {

    @PostMapping("/chat")
    public fun chat(@RequestBody request: RequestModel): ResponseEntity<ResponseModel> {
        val response = chatBot.call(request.promptMessage, request.historyId)
        return ResponseEntity(ResponseModel(response), HttpStatus.OK)
    }

}