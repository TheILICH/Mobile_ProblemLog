@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.problemlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.problemlog.data.Problem
import com.example.problemlog.logic.FilterState
import com.example.problemlog.logic.sortSummary
import com.example.problemlog.ui.ProblemsViewModel
import com.example.problemlog.ui.components.EmptyState
import com.example.problemlog.ui.components.FilterSheetContent
import com.example.problemlog.ui.components.ProblemCard
import com.example.problemlog.ui.components.SearchField

// route = connects viewmodel to the stateless screen
@Composable
fun ProblemListRoute(
    viewModel: ProblemsViewModel,
    onOpenProblem: (String) -> Unit,
    onAddProblem: () -> Unit
) {
    val problems by viewModel.visibleProblems.collectAsStateWithLifecycle()
    val allProblems by viewModel.allProblems.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()

    ProblemListScreen(
        problems = problems,
        totalCount = allProblems.size,
        query = query,
        filter = filter,
        allTags = allTags,
        onQueryChange = viewModel::onQueryChange,
        onFilterChange = viewModel::onFilterChange,
        onClearFilters = viewModel::clearFilters,
        onOpenProblem = onOpenProblem,
        onAddProblem = onAddProblem
    )
}

@Composable
fun ProblemListScreen(
    problems: List<Problem>,
    totalCount: Int,
    query: String,
    filter: FilterState,
    allTags: List<String>,
    onQueryChange: (String) -> Unit,
    onFilterChange: (FilterState) -> Unit,
    onClearFilters: () -> Unit,
    onOpenProblem: (String) -> Unit,
    onAddProblem: () -> Unit
) {
    // sheet open/closed is only ui state, rememberSaveable keeps it on rotation
    var showFilters by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("ProblemLog") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddProblem) {
                Icon(Icons.Default.Add, contentDescription = "Add problem")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SearchField(query, onQueryChange, Modifier.padding(horizontal = 16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text("${problems.size} of $totalCount problems", style = MaterialTheme.typography.labelLarge)
                    Text(
                        filter.sortSummary(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                BadgedBox(
                    badge = {
                        if (filter.activeCount > 0) Badge { Text(filter.activeCount.toString()) }
                    }
                ) {
                    FilledTonalButton(onClick = { showFilters = true }) { Text("Filters & sort") }
                }
            }

            if (problems.isEmpty()) {
                EmptyState(
                    title = "No problems match",
                    subtitle = "Try another search or clear filters",
                    action = {
                        TextButton(onClick = {
                            onQueryChange("")
                            onClearFilters()
                        }) { Text("Clear everything") }
                    }
                )
            } else {
                LazyColumn(
                    // bottom space so fab does not cover last card
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(problems, key = { it.id }) { problem ->
                        ProblemCard(problem = problem, onClick = { onOpenProblem(problem.id) })
                    }
                }
            }
        }
    }

    if (showFilters) {
        ModalBottomSheet(
            onDismissRequest = { showFilters = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            FilterSheetContent(
                filter = filter,
                allTags = allTags,
                onFilterChange = onFilterChange,
                onClearFilters = onClearFilters
            )
        }
    }
}
