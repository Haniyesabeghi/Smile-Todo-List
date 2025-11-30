package com.example.mytodolist

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TaskDbHelper(context: Context) : SQLiteOpenHelper(context, "tasks.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS tasks (
              id INTEGER PRIMARY KEY,
              title TEXT NOT NULL,
              time TEXT NOT NULL,
              colorHex INTEGER NOT NULL,
              completed INTEGER NOT NULL,
              dayIndex INTEGER,
              reminderHour INTEGER,
              reminderMinute INTEGER,
              dueEpochDay INTEGER,
              repeat TEXT,
              soundIndex INTEGER
            )
            """.trimIndent()
        )
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
}

object TaskDb {
    private var helper: TaskDbHelper? = null
    fun init(context: Context) {
        if (helper == null) helper = TaskDbHelper(context.applicationContext)
    }
    private fun db(): SQLiteDatabase { return requireNotNull(helper).writableDatabase }

    fun getAll(): List<TodoItem> {
        val list = mutableListOf<TodoItem>()
        val c: Cursor = db().query("tasks", null, null, null, null, null, "id DESC")
        c.use {
            while (it.moveToNext()) {
                list.add(
                    TodoItem(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        time = it.getString(it.getColumnIndexOrThrow("time")),
                        colorHex = it.getLong(it.getColumnIndexOrThrow("colorHex")),
                        completed = it.getInt(it.getColumnIndexOrThrow("completed")) != 0,
                        dayIndex = it.getInt(it.getColumnIndexOrThrow("dayIndex")).let { v -> if (it.isNull(it.getColumnIndexOrThrow("dayIndex"))) null else v },
                        reminderHour = it.getInt(it.getColumnIndexOrThrow("reminderHour")).let { v -> if (it.isNull(it.getColumnIndexOrThrow("reminderHour"))) null else v },
                        reminderMinute = it.getInt(it.getColumnIndexOrThrow("reminderMinute")).let { v -> if (it.isNull(it.getColumnIndexOrThrow("reminderMinute"))) null else v },
                        dueEpochDay = it.getLong(it.getColumnIndexOrThrow("dueEpochDay")).let { v -> if (it.isNull(it.getColumnIndexOrThrow("dueEpochDay"))) null else v },
                        repeat = it.getString(it.getColumnIndexOrThrow("repeat")).let { s -> s?.let { RepeatPattern.valueOf(s) } },
                        soundIndex = it.getInt(it.getColumnIndexOrThrow("soundIndex")).let { v -> if (it.isNull(it.getColumnIndexOrThrow("soundIndex"))) null else v }
                    )
                )
            }
        }
        return list
    }

    fun insert(item: TodoItem) {
        val cv = ContentValues().apply {
            put("id", item.id)
            put("title", item.title)
            put("time", item.time)
            put("colorHex", item.colorHex)
            put("completed", if (item.completed) 1 else 0)
            put("dayIndex", item.dayIndex)
            put("reminderHour", item.reminderHour)
            put("reminderMinute", item.reminderMinute)
            put("dueEpochDay", item.dueEpochDay)
            put("repeat", item.repeat?.name)
            put("soundIndex", item.soundIndex)
        }
        db().insert("tasks", null, cv)
    }

    fun update(item: TodoItem) {
        val cv = ContentValues().apply {
            put("title", item.title)
            put("time", item.time)
            put("colorHex", item.colorHex)
            put("completed", if (item.completed) 1 else 0)
            put("dayIndex", item.dayIndex)
            put("reminderHour", item.reminderHour)
            put("reminderMinute", item.reminderMinute)
            put("dueEpochDay", item.dueEpochDay)
            put("repeat", item.repeat?.name)
            put("soundIndex", item.soundIndex)
        }
        db().update("tasks", cv, "id=?", arrayOf(item.id.toString()))
    }

    fun updateCompleted(id: Long, completed: Boolean) {
        val cv = ContentValues().apply { put("completed", if (completed) 1 else 0) }
        db().update("tasks", cv, "id=?", arrayOf(id.toString()))
    }

    fun delete(id: Long) {
        db().delete("tasks", "id=?", arrayOf(id.toString()))
    }
}
