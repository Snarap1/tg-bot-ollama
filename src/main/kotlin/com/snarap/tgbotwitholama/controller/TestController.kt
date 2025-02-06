package com.snarap.tgbotwitholama.controller

import com.snarap.tgbotwitholama.model.RequestModel
import com.snarap.tgbotwitholama.model.ResponseModel
import com.snarap.tgbotwitholama.service.ChatBotService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/test")
class TestController (private val chatBot: ChatBotService) {

    @PostMapping("/chat")
    public fun chat(@RequestBody request: RequestModel): ResponseEntity<ResponseModel> {
        val response = chatBot.call(request.promptMessage, request.historyId)
        return ResponseEntity(ResponseModel(response), HttpStatus.OK)
    }

    @GetMapping("/test")
    fun  testCall(): ResponseEntity<String> {
        return ResponseEntity("CallbackDone", HttpStatus.OK)
    }

}