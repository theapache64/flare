package com.theapache64.flare.telegrambot

import com.teamxenox.telegramapi.Telegram
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@WebServlet(urlPatterns = ["/on_command"])
class OnCommandServlet : HttpServlet() {

    companion object {
        val telegram = Telegram(SecretConstants.TELEGRAM_TOKEN)
    }

    override fun doPost(req: HttpServletRequest?, resp: HttpServletResponse?) {
        println("Hit 1")
        val jsonReq = req!!.reader.readText()
        println("Hit 2")
    }
}