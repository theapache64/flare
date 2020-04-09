package com.theapache64.flare.telegrambot

import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener


@WebListener
class OnDeployedListener : ServletContextListener {
    override fun contextInitialized(servletContextEvent: ServletContextEvent) {
        println("WebEngine config initialized")
    }
}