package com.theapache64.flare.telegrambot

import java.sql.Connection
import java.sql.SQLException
import javax.naming.Context
import javax.naming.InitialContext
import javax.naming.NamingException
import javax.sql.DataSource

/**
 * Created by theapache64 on 10/13/2015.
 */
object Connection {

    private var ds: DataSource? = null
    private const val IS_DEBUG = true

    val connection: Connection
        get() = try {

            if (ds == null) {
                val initContext: Context = InitialContext()
                val envContext = initContext.lookup("java:/comp/env") as Context
                ds =
                    envContext.lookup("jdbc/flares") as DataSource?
            }

            ds!!.connection

        } catch (e: NamingException) {
            e.printStackTrace()
            throw IllegalArgumentException("Connection error : " + e.message)
        } catch (e: SQLException) {
            e.printStackTrace()
            throw IllegalArgumentException("Connection error : " + e.message)
        }
}