package com.danielzbarnes.pagingapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.danielzbarnes.pagingapp.data.GithubRepository


class ViewModelFactory(private val repository: GithubRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchRepositoriesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchRepositoriesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
