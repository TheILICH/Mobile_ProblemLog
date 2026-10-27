package com.example.problemlog.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.problemlog.data.Problem
import com.example.problemlog.data.ProblemRepository
import com.example.problemlog.data.Verdict
import com.example.problemlog.logic.FilterState
import com.example.problemlog.logic.ProblemInput
import com.example.problemlog.logic.ReviewItem
import com.example.problemlog.logic.SaveResult
import com.example.problemlog.logic.Stats
import com.example.problemlog.logic.applyFilterAndSort
import com.example.problemlog.logic.buildReviewList
import com.example.problemlog.logic.computeStats
import com.example.problemlog.logic.toProblem
import com.example.problemlog.logic.validateInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val KEY_QUERY = "query"

class ProblemsViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val repository = ProblemRepository

    val allProblems: StateFlow<List<Problem>> = repository.problems

    // search text lives in saved state so it survives even process death
    val query: StateFlow<String> = savedStateHandle.getStateFlow(KEY_QUERY, "")

    private val _filter = MutableStateFlow(FilterState())
    val filter: StateFlow<FilterState> = _filter.asStateFlow()

    // problems + debounced query + filter -> one list for the screen
    @OptIn(FlowPreview::class)
    val visibleProblems: StateFlow<List<Problem>> =
        combine(repository.problems, query.debounce(300), _filter) { list, q, f ->
            applyFilterAndSort(list, q, f, System.currentTimeMillis())
        }
            .flowOn(Dispatchers.Default)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = applyFilterAndSort(
                    repository.problems.value, query.value, _filter.value, System.currentTimeMillis()
                )
            )

    val allTags: StateFlow<List<String>> = repository.problems
        .map { tagsOf(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), tagsOf(repository.problems.value))

    val stats: StateFlow<Stats> = repository.problems
        .map { computeStats(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), computeStats(repository.problems.value))

    val reviewItems: StateFlow<List<ReviewItem>> = repository.problems
        .map { buildReviewList(it, System.currentTimeMillis()) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            buildReviewList(repository.problems.value, System.currentTimeMillis())
        )

    private fun tagsOf(list: List<Problem>): List<String> = list.flatMap { it.tags }.distinct().sorted()

    fun onQueryChange(value: String) {
        savedStateHandle[KEY_QUERY] = value
    }

    fun onFilterChange(newFilter: FilterState) {
        _filter.value = newFilter
    }

    // clear filters but keep the chosen sort
    fun clearFilters() {
        val old = _filter.value
        _filter.value = FilterState(primarySort = old.primarySort, secondarySort = old.secondarySort)
    }

    fun logAttempt(problemId: String, verdict: Verdict, minutes: Int) {
        viewModelScope.launch {
            repository.logAttempt(problemId, verdict, minutes)
        }
    }

    fun deleteProblem(problemId: String) {
        viewModelScope.launch {
            repository.delete(problemId)
        }
    }

    fun saveProblem(input: ProblemInput, editingId: String?): SaveResult {
        val existingIds = allProblems.value.map { it.id }.toSet()
        val error = validateInput(input, existingIds, editingId)
        if (error != null) return SaveResult.Invalid(error)

        val base = editingId?.let { repository.find(it) }
        viewModelScope.launch {
            repository.upsert(input.toProblem(base))
        }
        return SaveResult.Saved
    }
}
