package com.snarap.tgbotwitholama.model

class HistoryEntry(
    private val chatId: String,
    private val prompt: String,
    private val response: String
) {
    override fun toString(): String {
        return String.format(
            """
                        `history_entry`:
                            `prompt`: %s
                        
                            `response`: %s
                        -----------------        
                        \n
            """, prompt, response
        )
    }
}