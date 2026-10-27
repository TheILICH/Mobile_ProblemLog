@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.example.problemlog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.problemlog.data.Problem
import com.example.problemlog.logic.ProblemInput
import com.example.problemlog.logic.SaveResult
import com.example.problemlog.logic.parseTags
import com.example.problemlog.ui.ProblemsViewModel

@Composable
fun EditProblemRoute(
    viewModel: ProblemsViewModel,
    editingId: String?,
    onDone: () -> Unit
) {
    val problems by viewModel.allProblems.collectAsStateWithLifecycle()
    val allTags by viewModel.allTags.collectAsStateWithLifecycle()
    val existing = editingId?.let { id -> problems.find { it.id == id } }

    EditProblemScreen(
        existing = existing,
        suggestedTags = allTags,
        onSave = { input -> viewModel.saveProblem(input, editingId) },
        onDone = onDone
    )
}

@Composable
fun EditProblemScreen(
    existing: Problem?,
    suggestedTags: List<String>,
    onSave: (ProblemInput) -> SaveResult,
    onDone: () -> Unit
) {
    val isEdit = existing != null

    // form fields survive rotation, start from existing problem when editing
    var id by rememberSaveable { mutableStateOf(existing?.id ?: "") }
    var title by rememberSaveable { mutableStateOf(existing?.title ?: "") }
    var rating by rememberSaveable { mutableStateOf(existing?.rating?.toString() ?: "") }
    var tags by rememberSaveable { mutableStateOf(existing?.tags?.joinToString(", ") ?: "") }
    var notes by rememberSaveable { mutableStateOf(existing?.notes ?: "") }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun save() {
        when (val result = onSave(ProblemInput(id, title, rating, tags, notes))) {
            SaveResult.Saved -> onDone()
            is SaveResult.Invalid -> error = result.message
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit ${existing?.id}" else "Add problem") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = id,
                onValueChange = { id = it.uppercase().trim() },
                label = { Text("Codeforces id (like 4A)") },
                // id is the key, so it can not change on edit
                enabled = !isEdit,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = rating,
                onValueChange = { new -> rating = new.filter { it.isDigit() }.take(4) },
                label = { Text("Rating (800 - 3500)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags, comma separated") },
                modifier = Modifier.fillMaxWidth()
            )

            // tap a known tag to add it, no typos this way
            val current = parseTags(tags)
            val unused = suggestedTags.filter { it !in current }
            if (unused.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    unused.forEach { tag ->
                        AssistChip(
                            onClick = { tags = (current + tag).joinToString(", ") },
                            label = { Text("+ $tag") }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (idea, mistakes...)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Button(onClick = { save() }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Text(if (isEdit) "Save changes" else "Add problem")
            }
        }
    }
}
