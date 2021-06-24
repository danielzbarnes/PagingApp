package com.danielzbarnes.pagingapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.danielzbarnes.pagingapp.model.Repo

@Database(entities = [Repo::class, RemoteKeys::class], version = 1, exportSchema = false)
abstract class RepoDatabase: RoomDatabase() {

    abstract fun reposDao(): RepoDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {

        @Volatile
        private var instance: RepoDatabase? = null

        fun getInstance(context: Context): RepoDatabase =
                instance ?: synchronized(this){
                    instance ?: buildDatabase(context).also { instance = it}
                }

        private fun buildDatabase(context: Context) =
                Room.databaseBuilder(context.applicationContext, RepoDatabase::class.java, "Github.db").build()
    }
}