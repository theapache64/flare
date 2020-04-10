package com.theapache64.flare.telegrambot.servlets

import com.teamxenox.telegramapi.Telegram
import com.teamxenox.telegramapi.models.SendMessageRequest
import com.teamxenox.telegramapi.models.Update
import com.theapache64.flare.telegrambot.utils.DuplicateGroupNameException
import com.theapache64.flare.telegrambot.utils.SimpleCommandExecutor
import com.theapache64.flare.telegrambot.database.Users
import com.theapache64.flare.telegrambot.models.User
import com.theapache64.flare.telegrambot.utils.SecretConstants
import java.lang.Exception
import java.lang.IllegalArgumentException
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
        private const val COMMAND_OFF = "/off"
        private const val SWITCH_ON = "💡 ON"
        private const val SWITCH_OFF = "📴 OFF"
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
        println("Text is $text")
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

                COMMAND_ON,
                SWITCH_ON,
                SWITCH_OFF,
                COMMAND_OFF -> {
                    val fromUserId = message.from.id.toString()
                    val user = Users.get(
                        Users.COLUMN_TGM_ID, fromUserId)
                    if (user == null) {
                        // no group attached
                        telegram.sendMessage(
                            SendMessageRequest(
                                chatId,
                                text = "No group associated with your account. Set your group name by /set command",
                                replyMsgId = replyMsgId
                            )
                        )
                    } else {
                        // turn on the bulb
                        val isFlash = text == COMMAND_ON || text == SWITCH_ON
                        setFlash(chatId, replyMsgId, isFlash, user.groupName)
                    }
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
        when (val replyMsgFor = message.replyToMessage!!.text) {

            MSG_SET_GROUP_HELP -> {
                setGroupName(fromUserId, text, chatId, message)
            }

            else -> {
                if (replyMsgFor.startsWith("Group name ") and replyMsgFor.endsWith(" Try something else")) {
                    setGroupName(fromUserId, text, chatId, message)
                } else {
                    sendinvalidCommand(chatId, text, replyMsgId)
                }
            }
        }
    }

    private fun setGroupName(
        fromUserId: String,
        _text: String,
        chatId: Long,
        message: Update.Message
    ) {
        val text = _text.toLowerCase()
        if (text.contains(" ")) {
            throw IllegalArgumentException("Group name can't have spaces 😥")
        }

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

    private fun setFlash(chatId: Long, replyMsgId: Long, isFlash: Boolean, groupName: String) {
        val command = """
            curl -s -X POST \
              https://fcm.googleapis.com/fcm/send \
              -H 'authorization: key=AAAAwLZSTlE:APA91bG8NK2kF5BpoqM-EDsq1wMt8CfKdYr-QU-aEUfHEsMr7LKeAaHh17L-Lj3KL_zRgnz6I2N1fAtg-kyRyYzjJpXrcdpveQLdLawWBZQhvla4xhINBeUOSm2HH55F8Q2VUSZPluCUoq5ELT_4TA1UvNfzkQbYZg' \
              -H 'cache-control: no-cache' \
              -H 'content-type: application/json' \
              -H 'postman-token: a428dbbf-1f71-5016-9251-a4dce9773e5a' \
              -d '{
              "to": "/topics/$groupName",
              "data": {
                "is_flash":$isFlash
               }
            }'
        """.trimIndent()

        val result =
            SimpleCommandExecutor.executeCommand(command, true)
        if (result.startsWith("{\"message_id\":")) {
            // success
            val nextAction = if (isFlash) {
                SWITCH_OFF
            } else {
                SWITCH_ON
            }

            telegram.sendMessage(
                SendMessageRequest(
                    chatId,
                    text = "👉 Flash -> ${if (isFlash) "ON" else "OFF"}",
                    replyMsgId = replyMsgId,
                    replyMarkup = SendMessageRequest.ReplyMarkup(
                        inlineKeyboard = null,
                        isForceReply = false,
                        isOneTime = true,
                        keyboard = listOf(
                            listOf(
                                SendMessageRequest.KeyboardButton(nextAction)
                            )
                        )
                    )
                )
            )

        } else {
            // failed
            telegram.sendMessage(
                SendMessageRequest(
                    chatId = chatId,
                    text = "Ohh no, it failed. \nAre you sure you installed the Android app and you subscribed to the group ? 🤔"
                )
            )
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