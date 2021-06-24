package com.danielzbarnes.pagingapp.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.danielzbarnes.pagingapp.api.GithubService
import com.danielzbarnes.pagingapp.api.IN_QUALIFIER
import com.danielzbarnes.pagingapp.model.Repo
import retrofit2.HttpException
import java.io.IOException

private const val GITHUB_STARTING_PAGE_INDEX = 1
private const val NETWORK_PAGE_SIZE = 50

// source for paging github data
class GithubPagingSource(private val service: GithubService, private val query: String):
    PagingSource<Int, Repo>() {

    // fetches data async to load
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> {

        val pos = params.key ?: GITHUB_STARTING_PAGE_INDEX // if params.key is null use the starting index
        val apiQuery = query + IN_QUALIFIER

        return try {
            val response = service.searchRepos(apiQuery, pos, params.loadSize)
            val repos = response.items
            val nextKey = if (repos.isEmpty()) null
                          // prevent requesting duplicate items
                          else pos + (params.loadSize / NETWORK_PAGE_SIZE)
            LoadResult.Page(data = repos, prevKey = if (pos == GITHUB_STARTING_PAGE_INDEX) null else pos-1,
                            nextKey = nextKey)
        } catch(e: IOException) {
            return LoadResult.Error(e)
        } catch(e: HttpException){
            return LoadResult.Error(e)
        }
    }

    // refresh key is used for refresh calls to PagingSource.load()
    override fun getRefreshKey(state: PagingState<Int, Repo>): Int? {

        // return the previous key, unless its null, then the next key of the closest page to the pos
        return state.anchorPosition?.let {
            anchorPos -> state.closestPageToPosition(anchorPos)?.prevKey?.plus(1)
            ?: state.closestPageToPosition(anchorPos)?.nextKey?.minus(1)
        }
    }

}