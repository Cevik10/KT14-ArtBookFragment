package com.hakancevik.artbookfragment.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hakancevik.artbookfragment.model.Art

@Database(entities = [Art::class], version = 1)
abstract class ArtDatabase : RoomDatabase() {
    abstract fun artDao(): ArtDao
}