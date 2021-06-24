package com.danielzbarnes.pagingapp

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import com.danielzbarnes.pagingapp.api.GithubService
import com.danielzbarnes.pagingapp.data.GithubRepository
import com.danielzbarnes.pagingapp.database.RepoDatabase
import com.danielzbarnes.pagingapp.ui.ViewModelFactory

// class to handle object creation
object Injection {

    // create instance of GithubRepository
    private fun provideGithubRepository(context: Context): GithubRepository {
        return GithubRepository(GithubService.create(), RepoDatabase.getInstance(context))
    }

    // provide the ViewModelProvider.Factory for ViewModels
    fun provideViewModelFactory(context: Context): ViewModelProvider.Factory {
        return ViewModelFactory(provideGithubRepository(context))
    }
}
