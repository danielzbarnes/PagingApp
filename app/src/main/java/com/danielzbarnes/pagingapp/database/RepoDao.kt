package com.danielzbarnes.pagingapp.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.danielzbarnes.pagingapp.model.Repo

@Dao
interface RepoDao {

    @Query("SELECT * FROM repos WHERE name LIKE :queryString OR description LIKE :queryString " +
            "ORDER BY stars DESC, name ASC")
    fun reposByName(queryString: String): PagingSource<Int, Repo>

    @Query("DELETE FROM repos")
    suspend fun clearRepos()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(repos: List<Repo>)

}