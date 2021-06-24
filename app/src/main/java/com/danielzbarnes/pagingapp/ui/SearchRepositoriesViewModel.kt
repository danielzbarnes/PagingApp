package com.danielzbarnes.pagingapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.danielzbarnes.pagingapp.data.GithubRepository
import com.danielzbarnes.pagingapp.model.Repo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ViewModel for SearchRepositoryActivity using GithubRepository class
class SearchRepositoriesViewModel(private val repository: GithubRepository) : ViewModel() {

    private var currentQueryValue: String? = null
    private var currentSearchResult: Flow<PagingData<UiModel>>? = null

    fun searchRepo(queryString: String): Flow<PagingData<UiModel>>{

        val lastResult = currentSearchResult
        if(queryString == currentQueryValue && lastResult != null) return lastResult

        currentQueryValue = queryString
        val newResult: Flow<PagingData<UiModel>> = repository.getSearchResultStream(queryString)
            .map { pagingData -> pagingData.map { UiModel.RepoItem(it) } }
            .map {
                it.insertSeparators { before, after ->

                    // end of list
                    if (after == null) return@insertSeparators null

                    // beginning of list
                    if (before== null) return@insertSeparators  UiModel.SeparatorItem("${after.roundedStarCount}0.000+ stars")

                    // if between 2 items
                    if (before.roundedStarCount > after.roundedStarCount){
                        if (after.roundedStarCount >= 1)
                            UiModel.SeparatorItem("${after.roundedStarCount}0.000+ stars")
                        else UiModel.SeparatorItem("< 10.000+ stars")
                    } else null
                }
            }.cachedIn(viewModelScope)

        currentSearchResult = newResult
        return newResult
    }
}

sealed class UiModel{
    data class RepoItem(val repo:Repo): UiModel()
    data class SeparatorItem(val description: String): UiModel()
}

val UiModel.RepoItem.roundedStarCount: Int
    get() = this.repo.stars / 10_000