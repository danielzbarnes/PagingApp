package com.danielzbarnes.pagingapp.ui

import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter

// adapter for the LoadState ViewHolder
class ReposLoadStateAdapter(private val retry: () -> Unit): LoadStateAdapter<ReposLoadStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ReposLoadStateViewHolder {
        return ReposLoadStateViewHolder.create(parent, retry)
    }

    override fun onBindViewHolder(holder: ReposLoadStateViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }


}