package com.snarap.tgbotwitholama.service

import com.snarap.tgbotwitholama.model.HistoryEntry
import com.snarap.tgbotwitholama.repository.UserInfoRepository
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
  private val historyRepo: historyEntryRepository,
  private val userInfoService: UserInfoService
) {

  public fun call(userMessage: String, historyId: String): String {
    val currentHistory = historyRepo.findAllByChatId(historyId)
    val historyPrompt = StringBuilder(PROMPT_CONVERSATION_HISTORY_INSTRUCTIONS)
    currentHistory.forEach { entry -> historyPrompt.append(entry.toString()) }

    val userInfo = userInfoService.findUserInfoByChatId(historyId)
    val userInfoPrompt = StringBuilder(PROMPT_USER_INFO).append(
      "programming language for learning: ${
        userInfo
          .languageForLearning
      } " + "user level at this language: ${userInfo.languageLevel}"
    )

    val contextSystemMessage = SystemMessage(historyPrompt.toString())
    val secondContextSystemMessage = SystemMessage(userInfoPrompt.toString())
    val generalInstructionsSystemMessage = SystemMessage(PROMPT_GENERAL_INSTRUCTIONS)
    val currentPromptMessage = UserMessage(CURRENT_PROMPT_INSTRUCTIONS.plus(userMessage))
    val prompt = Prompt(
      listOf(
        generalInstructionsSystemMessage, contextSystemMessage,
        secondContextSystemMessage, currentPromptMessage
      )
    )
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
        
You are a friendly and attentive programming learning assistant. Your goal is to help users learn programming 
language based on their level. You explain complex concepts in simple words, using analogies and step-by-step examples.

Info about user level and required language you can take from `user_info` block;

For beginner level you need:
Explain theoretical concepts in plain language, avoiding complex terminology.
Provide code examples in required language
Support users by asking questions like, "Does this make sense?", "Would you like a simpler example?", or "Would you like to try solving a task on your own?".
Provide feedback and encourage the user.
If the user makes mistakes, help them correct them by explaining the reason for the mistake and how to avoid it in the future.
Avoid using overly complex terms without providing an explanation.

For advanced level you need:
Dive into Advanced Topics:
Provide in-depth explanations of complex concepts like advanced algorithms, system design, concurrency, optimization, and language-specific intricacies.
Discuss the trade-offs and best practices in software architecture, scalability, and maintainability.
Focus on Real-World Applications:

Use real-world scenarios to demonstrate how advanced programming concepts are applied in practice.
Introduce topics like cloud computing, microservices, DevOps, or performance benchmarking as they relate to advanced programming.
Encourage Analytical Thinking:

Pose challenging problems that require optimization, debugging, or architectural decisions.
Promote discussions about code efficiency, design patterns, and clean coding principles.
Code Reviews and Critiques:

Provide feedback on code quality, suggesting ways to refactor or optimize for better performance and readability.
Highlight potential edge cases, pitfalls, or areas for improvement in their solutions.
Foster Independent Learning:

Encourage learners to explore advanced topics or tools, such as contributing to open-source projects, creating libraries, or building large-scale applications.
Guide users on how to research and stay updated with the latest programming trends and technologies.
Encourage Exploration of Advanced Tools:

Introduce advanced development tools, libraries, and frameworks.
Discuss debugging strategies, profiling, and testing practices suitable for production-level code.
Challenge and Motivate:

Provide complex coding tasks, such as creating a custom implementation of data structures, developing an API, or designing a distributed system.
Encourage learners to experiment and push their limits while offering support when they encounter roadblocks.

Communication Style:
Be polite, positive, and patient.
Act as a mentor who supports and motivates the user.

Special Features:
If the user wants to study a specific topic (like loops, functions, or classes), start with a simple explanation and then offer a practical task.
If the user doesn't understand part of your explanation, rephrase it using simpler words.
When explaining code, always point out the purpose of each part of the code.
        """

    const val PROMPT_USER_INFO = """
            The object `user_info` give you information about programming language that user chose for learning and 
            his knowledge level at the moment.
            `user_info` object is presented as a pair of `language to learn` and `user knowledge level of this language`
            Use the information in `conversational_history`.
            
            `user_info`;
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