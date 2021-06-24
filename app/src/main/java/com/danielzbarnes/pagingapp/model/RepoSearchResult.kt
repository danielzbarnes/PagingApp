package com.danielzbarnes.pagingapp.model

import java.lang.Exception

// search result from a search
sealed class RepoSearchResult {
    data class Success(val data: List<Repo>) : RepoSearchResult()
    data class Error(val error: Exception) : RepoSearchResult()
}
