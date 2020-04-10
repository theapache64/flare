package com.theapache64.flare.telegrambot

import com.teamxenox.telegramapi.Telegram
import com.teamxenox.telegramapi.models.SendMessageRequest
import com.teamxenox.telegramapi.models.Update
import java.lang.Exception
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(urlPatterns = ["/on_command"])
class OnCommandServlet : HttpServlet() {

    companion object {
        val telegram = Telegram(SecretConstants.TELEGRAM_TOKEN)
        private const val COMMAND_HELP = "/help"
        private const val COMMAND_START = "/start"
        private const val COMMAND_SET_GROUP = "/set"
        private const val COMMAND_ON = "/on"
        private const val MSG_SET_GROUP_HELP = "Please reply to this message with your group name"
    }

    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse?) {
        println("---------------------------------------")
        val jsonReq = req!!.reader.readText()
        println(jsonReq)
        val update = Telegram.parseUpdate(jsonReq)
        val message = update.message!!
        val text = message.text.trim()
        val chatId = message.chat.id
        val replyMsgId = message.messageId
        try {
            when (text) {
                COMMAND_HELP, COMMAND_START -> {
                    telegram.sendMessage(
                        SendMessageRequest(
                            chatId, """
                            /start - To get help 🤗
                            /set - To set group name 👪
                            /on - To turn on the flash light 💡
                            /off - To turn off the flash light 📴
                        """.trimIndent()
                        )
                    )
                }

                COMMAND_SET_GROUP -> {
                    telegram.sendMessage(
                        SendMessageRequest(
                            chatId,
                            MSG_SET_GROUP_HELP,
                            replyMarkup = SendMessageRequest.ReplyMarkup(isForceReply = true)
                        )
                    )
                }

                else -> {
                    if (message.replyToMessage != null) {
                        handleMessageReply(message, text, chatId, replyMsgId)
                    } else {
                        sendinvalidCommand(chatId, text, replyMsgId)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

            // Sending raw error to bot
            telegram.sendMessage(
                SendMessageRequest(
                    chatId = chatId,
                    replyMsgId = replyMsgId,
                    text = "ERROR : ${e.message}"
                )
            )
        }
    }

    private fun handleMessageReply(
        message: Update.Message,
        text: String,
        chatId: Long,
        replyMsgId: Long
    ) {

        val fromUserId = message.from.id.toString()

        when (message.replyToMessage!!.text) {

            MSG_SET_GROUP_HELP -> {
                println("Setting group name")
                // Setting group
                val user = Users.get(Users.COLUMN_TGM_ID, fromUserId)
                try {
                    if (user == null) {
                        println("Adding user...")
                        // adding user and setting group name
                        Users.addv3(
                            User(
                                tgmId = fromUserId,
                                groupName = text
                            )
                        )
                    } else {
                        println("Updating user...")
                        // setting group name
                        Users.update(
                            Users.COLUMN_TGM_ID,
                            fromUserId,
                            Users.COLUMN_GROUP_NAME,
                            text
                        )
                    }

                    telegram.sendMessage(
                        SendMessageRequest(
                            chatId = chatId,
                            text = "👍 Group name set to `$text`, Now you can call /on 😉"
                        )
                    )
                } catch (e: DuplicateGroupNameException) {
                    println("Group name already exists")
                    telegram.sendMessage(
                        SendMessageRequest(
                            chatId = chatId,
                            text = "Group name `$text` exists. Try something else",
                            replyMsgId = message.replyToMessage!!.messageId,
                            replyMarkup = SendMessageRequest.ReplyMarkup(isForceReply = true)
                        )
                    )
                }
            }

            COMMAND_ON -> {
                val user = Users.get(Users.COLUMN_TGM_ID, fromUserId)
                if (user == null) {
                    // no group attached
                    telegram.sendMessage(
                        SendMessageRequest(
                            chatId,
                            text = "No group associated with your account. Set your group name by /set command",
                            replyMsgId = replyMsgId
                        )
                    )
                }else{
                    // turn on the bulb

                }
            }

            else -> {
                sendinvalidCommand(chatId, text, replyMsgId)
            }
        }
    }

    private fun sendinvalidCommand(chatId: Long, text: String, replyMsgId: Long) {
        telegram.sendMessage(
            SendMessageRequest(
                chatId,
                "Invalid command `$text`",
                replyMsgId = replyMsgId
            )
        )
    }
}