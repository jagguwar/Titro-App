package com.example.titro.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.titro.data.models.ToDoNote

@Database(entities = [ToDoNote::class], version = 1, exportSchema = false)
abstract class ToDoDatabase: RoomDatabase() {

    abstract fun toDoDao(): ToDoDao

}