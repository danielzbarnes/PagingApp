package com.danielzbarnes.pagingapp.data

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.danielzbarnes.pagingapp.api.GithubService
import com.danielzbarnes.pagingapp.database.RepoDatabase
import com.danielzbarnes.pagingapp.model.Repo
import kotlinx.coroutines.flow.Flow

private const val TAG = "PagingGithubRepo"

class GithubRepository(private val service: GithubService,
                       private val database: RepoDatabase) {

    // search repositories and return in a Flow
    fun getSearchResultStream(query: String): Flow<PagingData<Repo>> {
        Log.d(TAG, "New query: $query")

        val dbQuery = "%${query.replace(' ', '%')}%"
        val pagingSourceFactory = { database.reposDao().reposByName(dbQuery)}

        @OptIn(ExperimentalPagingApi::class)
        return Pager( config = PagingConfig(pageSize = NETWORK_PAGE_SIZE, enablePlaceholders = false),
            remoteMediator = GithubRemoteMediator(query, service, database),
            pagingSourceFactory = pagingSourceFactory).flow
    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 50
    }
}
