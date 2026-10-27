@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.problemlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.problemlog.data.Attempt
import com.example.problemlog.data.Problem
import com.example.problemlog.data.Status
import com.example.problemlog.data.Verdict
import com.example.problemlog.logic.daysAgoText
import com.example.problemlog.logic.nextStatus
import com.example.problemlog.logic.reviewReason
import com.example.problemlog.logic.text
import com.example.problemlog.ui.ProblemsViewModel
import com.example.problemlog.ui.components.EmptyState
import com.example.problemlog.ui.components.RatingText
import com.example.problemlog.ui.components.SectionTitle
import com.example.problemlog.ui.components.StatusBadge
import kotlinx.coroutines.launch

@Composable
fun ProblemDetailRoute(
    viewModel: ProblemsViewModel,
    problemId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val problems by viewModel.allProblems.collectAsStateWithLifecycle()
    val problem = problems.find { it.id == problemId }

    ProblemDetailScreen(
        problem = problem,
        onBack = onBack,
        onEdit = onEdit,
        onLogAttempt = { verdict, minutes -> viewModel.logAttempt(problemId, verdict, minutes) },
        onDelete = {
            viewModel.deleteProblem(problemId)
            onBack()
        }
    )
}

@Composable
fun ProblemDetailScreen(
    problem: Problem?,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onLogAttempt: (Verdict, Int) -> Unit,
    onDelete: () -> Unit
) {
    var showLogDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val now = remember { System.currentTimeMillis() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(problem?.id ?: "Problem") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (problem != null) {
                        IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (problem == null) {
            EmptyState("Problem not found", "It was probably deleted", Modifier.padding(padding))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item { ProblemHeader(problem, now) }
            item {
                Button(
                    onClick = { showLogDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) { Text("Log attempt") }
            }
            item { SectionTitle("History (${problem.attempts.size})") }
            if (problem.attempts.isEmpty()) {
                item { Text("No attempts yet. Go solve it!", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            // newest first
            items(problem.attempts.reversed()) { attempt ->
                AttemptRow(attempt, now)
                HorizontalDivider()
            }
        }
    }

    if (showLogDialog && problem != null) {
        LogAttemptDialog(
            onDismiss = { showLogDialog = false },
            onSave = { verdict, minutes ->
                showLogDialog = false
                // same rule as repository, just to tell user what changed
                val newStatus = nextStatus(problem.status, verdict)
                onLogAttempt(verdict, minutes)
                val message = when {
                    newStatus != problem.status && newStatus == Status.SOLVED -> "Accepted! Marked as solved"
                    newStatus != problem.status -> "Moved to ${newStatus.label.lowercase()}"
                    else -> "Attempt saved"
                }
                scope.launch { snackbarHostState.showSnackbar(message) }
            }
        )
    }

    if (showDeleteDialog && problem != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${problem.id}?") },
            text = { Text("All ${problem.attempts.size} attempts will be lost too.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ProblemHeader(problem: Problem, now: Long) {
    Column {
        Text(problem.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(problem.status)
            Spacer(Modifier.width(12.dp))
            RatingText(problem.rating)
            Spacer(Modifier.width(12.dp))
            Text(
                "${problem.attempts.size} tries · ${problem.totalMinutes} min",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            problem.tags.forEach { tag -> AssistChip(onClick = {}, label = { Text(tag) }) }
        }
        problem.reviewReason(now)?.let { reason ->
            Text(
                "Review: ${reason.text()}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (problem.notes.isNotBlank()) {
            SectionTitle("Notes")
            Card(Modifier.fillMaxWidth()) {
                Text(problem.notes, modifier = Modifier.padding(12.dp))
            }
        }
    }
}

@Composable
private fun AttemptRow(attempt: Attempt, now: Long) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            attempt.verdict.name,
            fontWeight = FontWeight.Bold,
            color = if (attempt.verdict.isAccepted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            modifier = Modifier.width(48.dp)
        )
        Text(attempt.verdict.label, modifier = Modifier.weight(1f))
        Text(
            "${attempt.minutes} min · ${daysAgoText(attempt.timestamp, now)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LogAttemptDialog(
    onDismiss: () -> Unit,
    onSave: (Verdict, Int) -> Unit
) {
    var verdict by rememberSaveable { mutableStateOf(Verdict.AC) }
    var minutesText by rememberSaveable { mutableStateOf("") }
    val minutes = minutesText.toIntOrNull()
    val minutesOk = minutes != null && minutes in 1..600

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log attempt") },
        text = {
            Column {
                Text("Verdict", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Verdict.entries.forEach { v ->
                        FilterChip(
                            selected = verdict == v,
                            onClick = { verdict = v },
                            label = { Text(v.name) }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = minutesText,
                    // digits only
                    onValueChange = { new -> minutesText = new.filter { it.isDigit() }.take(3) },
                    label = { Text("Minutes spent") },
                    singleLine = true,
                    isError = minutesText.isNotEmpty() && !minutesOk,
                    supportingText = { Text("1 to 600") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(enabled = minutesOk, onClick = { onSave(verdict, minutes ?: 0) }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
