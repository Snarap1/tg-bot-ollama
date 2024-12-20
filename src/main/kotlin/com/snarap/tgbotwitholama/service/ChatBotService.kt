package com.snarap.tgbotwitholama.service

import com.snarap.tgbotwitholama.model.HistoryEntry
import com.snarap.tgbotwitholama.repository.historyEntryRepository
import org.springframework.ai.chat.messages.SystemMessage
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.ollama.OllamaChatModel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service


@Service
class ChatBotService(
    @Qualifier("ollamaChatModel")
    private val ollamaChatClient: OllamaChatModel,
    private val historyRepo: historyEntryRepository
) {

    public fun call(userMessage: String, historyId: String): String {
        val currentHistory = historyRepo.findAllByChatId(historyId)
        val historyPrompt = StringBuilder(PROMPT_CONVERSATION_HISTORY_INSTRUCTIONS)
        currentHistory.forEach { entry -> historyPrompt.append(entry.toString()) }


        val contextSystemMessage = SystemMessage(historyPrompt.toString());
        val generalInstructionsSystemMessage = SystemMessage(PROMPT_GENERAL_INSTRUCTIONS)
        val currentPromptMessage = UserMessage(CURRENT_PROMPT_INSTRUCTIONS.plus(userMessage))
        val prompt = Prompt(listOf(generalInstructionsSystemMessage, contextSystemMessage, currentPromptMessage))
        val response = ollamaChatClient.call(prompt).result.output.content

        historyRepo.save(HistoryEntry(historyId, userMessage, response))
        return response
    }

    private companion object {
        const val CURRENT_PROMPT_INSTRUCTIONS = """
            here's the `user_main_prompt`:
        """

        const val PROMPT_GENERAL_INSTRUCTIONS = """
    Here are the general guidelines to answer the `user_main_prompt`
        
You are a friendly and attentive programming learning assistant. Your goal is to help users understand the fundamentals of programming and learn how to write code. You explain complex concepts in simple words, using analogies and step-by-step examples.

Your Role:

Ask users about their current knowledge level and learning goals.
Explain theoretical concepts in plain language, avoiding complex terminology.
Provide code examples in popular programming languages (like Python, JavaScript, C++, etc.).
Support users by asking questions like, "Does this make sense?", "Would you like a simpler example?", or "Would you like to try solving a task on your own?".
Provide feedback and encourage the user.
If the user makes mistakes, help them correct them by explaining the reason for the mistake and how to avoid it in the future.
Communication Style:

Be polite, positive, and patient.
Avoid using overly complex terms without providing an explanation.
Act as a mentor who supports and motivates the user.
Special Features:

If the user wants to study a specific topic (like loops, functions, or classes), start with a simple explanation and then offer a practical task.
If the user doesn't understand part of your explanation, rephrase it using simpler words.
When explaining code, always point out the purpose of each part of the code.
        """

        const val PROMPT_CONVERSATION_HISTORY_INSTRUCTIONS = """        
    The object `conversational_history` below represents the past interaction between the user and you (the LLM).
    Each `history_entry` is represented as a pair of `prompt` and `response`.
    `prompt` is a past user prompt and `response` was your response for that `prompt`.
        
    Use the information in `conversational_history` if you need to recall things from the conversation
    , or in other words, if the `user_main_prompt` needs any information from past `prompt` or `response`.
    If you don't need the `conversational_history` information, simply respond to the prompt with your built-in knowledge.
                
    `conversational_history`:
        
"""
    }
}