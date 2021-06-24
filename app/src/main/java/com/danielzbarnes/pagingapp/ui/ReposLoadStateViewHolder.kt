package com.danielzbarnes.pagingapp.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import com.danielzbarnes.pagingapp.R
import com.danielzbarnes.pagingapp.databinding.FooterReposLoadStateBinding

// ViewHolder for the LoadState
class ReposLoadStateViewHolder(private val binding: FooterReposLoadStateBinding, retry: () -> Unit):
        RecyclerView.ViewHolder(binding.root){

    init { binding.retryButton.setOnClickListener { retry.invoke() } }

    fun bind(loadState: LoadState){

        if (loadState is LoadState.Error) binding.errorMsg.text = loadState.error.localizedMessage

        binding.progressBar.isVisible = loadState is LoadState.Loading
        binding.retryButton.isVisible = loadState is LoadState.Error
        binding.errorMsg.isVisible    = loadState is LoadState.Error
    }

    companion object{

        fun create(parent: ViewGroup, retry: () -> Unit): ReposLoadStateViewHolder{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.footer_repos_load_state, parent, false)
            val binding = FooterReposLoadStateBinding.bind(view)
            return ReposLoadStateViewHolder(binding, retry)
        }
    }
}