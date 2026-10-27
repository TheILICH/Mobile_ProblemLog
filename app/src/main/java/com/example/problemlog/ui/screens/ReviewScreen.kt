@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.problemlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.problemlog.logic.REVIEW_AFTER_DAYS
import com.example.problemlog.logic.ReviewItem
import com.example.problemlog.logic.text
import com.example.problemlog.ui.ProblemsViewModel
import com.example.problemlog.ui.components.EmptyState
import com.example.problemlog.ui.components.ProblemCard

@Composable
fun ReviewRoute(viewModel: ProblemsViewModel, onOpenProblem: (String) -> Unit) {
    val items by viewModel.reviewItems.collectAsStateWithLifecycle()
    ReviewScreen(items = items, onOpenProblem = onOpenProblem)
}

@Composable
fun ReviewScreen(items: List<ReviewItem>, onOpenProblem: (String) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Review (${items.size})") }) }
    ) { padding ->
        if (items.isEmpty()) {
            EmptyState(
                title = "Nothing to review",
                subtitle = "Failed problems and ones solved $REVIEW_AFTER_DAYS+ days ago show up here",
                modifier = Modifier.padding(padding)
            )
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Redo these to make sure you really know them",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(items, key = { it.problem.id }) { item ->
                ProblemCard(
                    problem = item.problem,
                    onClick = { onOpenProblem(item.problem.id) },
                    highlight = item.reason.text()
                )
            }
        }
    }
}
