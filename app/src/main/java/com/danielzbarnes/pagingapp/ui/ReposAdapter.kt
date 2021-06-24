package com.danielzbarnes.pagingapp.ui

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.danielzbarnes.pagingapp.R

// RecyclerView adapter using PagingDataAdapter
class ReposAdapter : PagingDataAdapter<UiModel, RecyclerView.ViewHolder>(UIMODEL_COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == R.layout.item_repo_view) RepoViewHolder.create(parent)
               else SeparatorViewHolder.create(parent)
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)){
            is UiModel.RepoItem -> R.layout.item_repo_view
            is UiModel.SeparatorItem -> R.layout.item_separator_view
            null -> throw UnsupportedOperationException("Unknown view")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val uiModel = getItem(position)
        uiModel.let {
            when (uiModel) {
                is UiModel.RepoItem -> (holder as RepoViewHolder).bind(uiModel.repo)
                is UiModel.SeparatorItem -> (holder as SeparatorViewHolder).bind(uiModel.description)
            }
        }
    }

    companion object {
        private val UIMODEL_COMPARATOR = object : DiffUtil.ItemCallback<UiModel>() {

            override fun areItemsTheSame(oldItem: UiModel, newItem: UiModel): Boolean {
                return (oldItem is UiModel.RepoItem && newItem is UiModel.RepoItem &&
                        oldItem.repo.fullName == newItem.repo.fullName) ||
                        (oldItem is UiModel.SeparatorItem && newItem is UiModel.SeparatorItem &&
                                oldItem.description == newItem.description)
            }
            override fun areContentsTheSame(oldItem: UiModel, newItem: UiModel): Boolean =
                oldItem == newItem
        }
    }
}
