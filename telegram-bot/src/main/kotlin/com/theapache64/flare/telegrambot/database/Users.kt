package com.theapache64.flare.telegrambot.database

import com.theapache64.dbbase.BaseTable
import com.theapache64.dbbase.querybuilders.AddQueryBuilder
import com.theapache64.dbbase.querybuilders.SelectQueryBuilder
import com.theapache64.flare.telegrambot.utils.DuplicateGroupNameException
import com.theapache64.flare.telegrambot.models.User
import java.sql.SQLException

object Users : BaseTable<User>("users") {
    const val COLUMN_TGM_ID = "tgm_id"
    const val COLUMN_GROUP_NAME = "group_name"

    @Throws(DuplicateGroupNameException::class, SQLException::class)
    override fun addv3(user: User): String? {
        return try {
            AddQueryBuilder.Builder(this.tableName)
                .add(COLUMN_TGM_ID, user.tgmId)
                .add(COLUMN_GROUP_NAME, user.groupName)
                .doneAndReturn()
                .toString()
        } catch (e: SQLException) {
            e.printStackTrace()
            handleDuplicateGroupName(e)
            return null
        }
    }

    @Throws(DuplicateGroupNameException::class, SQLException::class)
    private fun handleDuplicateGroupName(e: Exception) {
        val msg = e.message!!.toLowerCase()
        if ((msg.contains("duplicate entry") && msg.contains(COLUMN_GROUP_NAME) ||
                    msg == ("failed to update $COLUMN_GROUP_NAME"))
        ) {
            throw DuplicateGroupNameException()
        } else {
            throw e
        }
    }

    override fun update(
        whereColumn: String?,
        whereColumnValue: String?,
        updateColumn: String?,
        newUpdateColumnValue: String?
    ) {
        try {
            super.update(whereColumn, whereColumnValue, updateColumn, newUpdateColumnValue)
        } catch (e: Exception) {
            handleDuplicateGroupName(e)
        }
    }

    override fun get(column: String, value: String): User? {
        return SelectQueryBuilder.Builder<User>(
            this.tableName,
            SelectQueryBuilder.Callback<User> {
                return@Callback User(
                    it.getString(COLUMN_ID),
                    it.getString(COLUMN_TGM_ID),
                    it.getString(COLUMN_GROUP_NAME)
                )
            }
        ).select(arrayOf(COLUMN_ID,
            COLUMN_TGM_ID,
            COLUMN_GROUP_NAME
        ))
            .where(column, value)
            .limit(1)
            .build().get()
    }
}