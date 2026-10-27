@file:OptIn(ExperimentalLayoutApi::class)

package com.example.problemlog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.problemlog.data.Status
import com.example.problemlog.data.Verdict
import com.example.problemlog.logic.FilterState
import com.example.problemlog.logic.MAX_RATING
import com.example.problemlog.logic.MIN_RATING
import com.example.problemlog.logic.SortKey
import com.example.problemlog.logic.SortOption
import com.example.problemlog.logic.TagMode
import com.example.problemlog.logic.toggle
import kotlin.math.roundToInt

// stateless: gets filter, sends back a changed copy
@Composable
fun FilterSheetContent(
    filter: FilterState,
    allTags: List<String>,
    onFilterChange: (FilterState) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Filters & sort", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = onClearFilters, enabled = filter.activeCount > 0) { Text("Clear all") }
        }

        SectionTitle("Status")
        ChipRow {
            Status.entries.forEach { status ->
                FilterChip(
                    selected = status in filter.statuses,
                    onClick = { onFilterChange(filter.copy(statuses = filter.statuses.toggle(status))) },
                    label = { Text(status.label) }
                )
            }
        }

        SectionTitle("Tags")
        ChipRow {
            TagMode.entries.forEach { mode ->
                FilterChip(
                    selected = filter.tagMode == mode,
                    onClick = { onFilterChange(filter.copy(tagMode = mode)) },
                    label = { Text(mode.label) }
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        ChipRow {
            allTags.forEach { tag ->
                FilterChip(
                    selected = tag in filter.tags,
                    onClick = { onFilterChange(filter.copy(tags = filter.tags.toggle(tag))) },
                    label = { Text(tag) }
                )
            }
        }

        SectionTitle("Rating: ${filter.ratingRange.first} – ${filter.ratingRange.last}")
        RangeSlider(
            value = filter.ratingRange.first.toFloat()..filter.ratingRange.last.toFloat(),
            onValueChange = { range ->
                onFilterChange(filter.copy(ratingRange = range.start.toRating()..range.endInclusive.toRating()))
            },
            valueRange = MIN_RATING.toFloat()..MAX_RATING.toFloat(),
            // one step per 100 rating
            steps = (MAX_RATING - MIN_RATING) / 100 - 1
        )

        SectionTitle("Last verdict")
        ChipRow {
            Verdict.entries.forEach { verdict ->
                FilterChip(
                    selected = verdict in filter.verdicts,
                    onClick = { onFilterChange(filter.copy(verdicts = filter.verdicts.toggle(verdict))) },
                    label = { Text(verdict.name) }
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Text("Needs review only", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(
                checked = filter.reviewOnly,
                onCheckedChange = { onFilterChange(filter.copy(reviewOnly = it)) }
            )
        }

        SectionTitle("Sort by")
        SortPicker(
            selected = filter.primarySort,
            allowNone = false,
            onSelect = { option -> option?.let { onFilterChange(filter.copy(primarySort = it)) } }
        )

        SectionTitle("Then by")
        SortPicker(
            selected = filter.secondarySort,
            allowNone = true,
            onSelect = { onFilterChange(filter.copy(secondarySort = it)) }
        )
    }
}

@Composable
private fun SortPicker(
    selected: SortOption?,
    allowNone: Boolean,
    onSelect: (SortOption?) -> Unit
) {
    ChipRow {
        if (allowNone) {
            FilterChip(selected = selected == null, onClick = { onSelect(null) }, label = { Text("None") })
        }
        SortKey.entries.forEach { key ->
            FilterChip(
                selected = selected?.key == key,
                // keep direction when switching key
                onClick = { onSelect(SortOption(key, selected?.descending ?: false)) },
                label = { Text(key.label) }
            )
        }
    }
    if (selected != null) {
        ChipRow {
            FilterChip(
                selected = !selected.descending,
                onClick = { onSelect(selected.copy(descending = false)) },
                label = { Text("Ascending ↑") }
            )
            FilterChip(
                selected = selected.descending,
                onClick = { onSelect(selected.copy(descending = true)) },
                label = { Text("Descending ↓") }
            )
        }
    }
}

@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        content()
    }
}

private fun Float.toRating(): Int = (this / 100).roundToInt() * 100
