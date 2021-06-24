package com.danielzbarnes.pagingapp.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.danielzbarnes.pagingapp.api.GithubService
import com.danielzbarnes.pagingapp.api.IN_QUALIFIER
import com.danielzbarnes.pagingapp.database.RemoteKeys
import com.danielzbarnes.pagingapp.database.RepoDatabase
import com.danielzbarnes.pagingapp.model.Repo
import retrofit2.HttpException
import java.io.IOException

private const val GITHUB_STARTING_PAGE_INDEX = 1

// the mediator is used to request more dta from the network
@OptIn(ExperimentalPagingApi::class)
class GithubRemoteMediator(private val query: String, private val service: GithubService,
            private val repoDatabase: RepoDatabase): RemoteMediator<Int, Repo>()  {

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Repo>): MediatorResult {
        val page: Int = when(loadType){
            LoadType.REFRESH ->{ // gets called the first time when loading data
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: GITHUB_STARTING_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                // if remoteKeys == null then refresh result is not in the db
                val remoteKeys = getRemoteKeyForFirstItem(state)
                // if remoteKeys != null, but nextKey == null, then its at the end of the pagination.prepend
                val prevKey = remoteKeys?.prevKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null )
                prevKey
            }
            LoadType.APPEND -> {
                // if remoteKeys == null then refresh result is not in the db
                val remoteKeys = getRemoteKeyForLastItem(state)
                // if remoteKeys != null, but nextKey == null, then its at the end of the pagination.append
                val nextKey = remoteKeys?.nextKey
                        ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }
        val apiQuery = query + IN_QUALIFIER

        try {
            val apiResponse = service.searchRepos(apiQuery, page, state.config.pageSize)

            val repos = apiResponse.items
            val endOfPaginationReached = repos.isEmpty()
            repoDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    repoDatabase.apply {
                        remoteKeysDao().clearRemoteKeys()
                        reposDao().clearRepos()
                    }

                    val prevKey = if (page == GITHUB_STARTING_PAGE_INDEX) null else page -1
                    val nextKey = if (endOfPaginationReached) null else page +1
                    val keys = repos.map {
                        RemoteKeys(repoId = it.id, prevKey = prevKey, nextKey = nextKey)
                    }
                    repoDatabase.apply {
                        remoteKeysDao().insertAll(keys)
                        reposDao().insertAll(repos)
                    }
                }
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException){ return MediatorResult.Error(e) }
        catch (e: HttpException){ return MediatorResult.Error(e) }
    }

    // get the last page that contained items
    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Repo>): RemoteKeys?{
        return state.pages.lastOrNull() { it.data.isNotEmpty() }?.data?.lastOrNull()
                ?.let { repo -> repoDatabase.remoteKeysDao().remoteKeysRepoId(repo.id) }
    }

    // get the first page that contains items
    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Repo>): RemoteKeys?{
        return state.pages.firstOrNull{ it.data.isNotEmpty() }?.data?.firstOrNull()
                ?.let { repo -> repoDatabase.remoteKeysDao().remoteKeysRepoId(repo.id) }
    }

    // get the item closest to the anchor point
    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, Repo>): RemoteKeys?{
        return state.anchorPosition?.let { pos ->
            state.closestItemToPosition(pos)?.id?.let { repoId ->
                repoDatabase.remoteKeysDao().remoteKeysRepoId(repoId)
            }
        }
    }
}
