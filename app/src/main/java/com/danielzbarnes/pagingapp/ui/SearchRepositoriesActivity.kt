package com.danielzbarnes.pagingapp.ui

import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.danielzbarnes.pagingapp.databinding.ActivitySearchRepositoriesBinding
import com.danielzbarnes.pagingapp.Injection
import com.danielzbarnes.pagingapp.model.RepoSearchResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

private const val TAG = "PagingActivity"

class SearchRepositoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchRepositoriesBinding

    private var searchJob: Job? = null

    private val viewModel: SearchRepositoriesViewModel by lazy {
        ViewModelProvider(this, Injection.provideViewModelFactory(this))
                                .get(SearchRepositoriesViewModel::class.java) }
    private val adapter = ReposAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate()")
        binding = ActivitySearchRepositoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // add decorations between recycler items
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.list.addItemDecoration(decoration)

        binding.retryButton.setOnClickListener { adapter.retry() }

        initAdapter()
        val query = savedInstanceState?.getString(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY
        search(query)
        initSearch(query)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LAST_SEARCH_QUERY, binding.searchRepo.text.trim().toString())
    }

    private fun initAdapter() {
        binding.list.adapter = adapter.withLoadStateHeaderAndFooter(
            header = ReposLoadStateAdapter { adapter.retry() },
            footer = ReposLoadStateAdapter { adapter.retry() }
        )

        adapter.addLoadStateListener { loadState ->

            // when the state is NotLoading and the list is empty, set an empty list
            val isListEmpty = loadState.refresh is LoadState.NotLoading && adapter.itemCount == 0
            showEmptyList(isListEmpty)

            // show list if refreshed, spinner while loading, retry on fail
            binding.list.isVisible = loadState.mediator?.refresh is LoadState.NotLoading
            binding.progressBar.isVisible = loadState.mediator?.refresh is LoadState.Loading
            binding.retryButton.isVisible = loadState.mediator?.refresh is LoadState.Error

            val errorState = loadState.source.append as? LoadState.Error
                ?: loadState.source.prepend as? LoadState.Error
                ?: loadState.append as? LoadState.Error
                ?: loadState.prepend as? LoadState.Error

            errorState?.let {
                Toast.makeText(this, "error occurred: ${it.error}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initSearch(query: String) {

        binding.searchRepo.setText(query)

        binding.searchRepo.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateRepoListFromInput()
                true
            } else { false }
        }

        binding.searchRepo.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateRepoListFromInput()
                true
            } else { false }
        }

        // scroll to top when refreshed
        lifecycleScope.launch {
            adapter.loadStateFlow.distinctUntilChangedBy { it.refresh } // emit on Refresh LoadState change
                // only react when refresh has completed
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binding.list.scrollToPosition(0) }
        }
    }

    private fun search(query: String) {

        searchJob?.cancel() // cancel the previous job before starting a new one
        searchJob = lifecycleScope.launch {
            viewModel.searchRepo(query).collectLatest { adapter.submitData(it) }
        }
    }

    private fun updateRepoListFromInput() {
        binding.searchRepo.text.trim().let {
            if (it.isNotEmpty()) {
                search(it.toString())
            }
        }
    }

    private fun showEmptyList(show: Boolean) {
        if (show) {
            binding.emptyList.visibility = View.VISIBLE
            binding.list.visibility = View.GONE
        } else {
            binding.emptyList.visibility = View.GONE
            binding.list.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val LAST_SEARCH_QUERY: String = "last_search_query"
        private const val DEFAULT_QUERY = "Android"
    }
}
