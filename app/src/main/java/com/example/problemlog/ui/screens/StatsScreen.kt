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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.problemlog.logic.Stats
import com.example.problemlog.ui.ProblemsViewModel
import com.example.problemlog.ui.components.ProgressRow
import com.example.problemlog.ui.components.SectionTitle

@Composable
fun StatsRoute(viewModel: ProblemsViewModel) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    StatsScreen(stats)
}

@Composable
fun StatsScreen(stats: Stats) {
    Scaffold(topBar = { TopAppBar(title = { Text("Stats") }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StatTile("Solved", "${stats.solved}/${stats.total}", Modifier.weight(1f))
                    StatTile("Attempts", stats.attempts.toString(), Modifier.weight(1f))
                    StatTile("AC rate", "${stats.acRate}%", Modifier.weight(1f))
                }
            }
            item {
                Text(
                    "${stats.todo} todo · ${stats.attempted} attempted · ${stats.minutes / 60}h ${stats.minutes % 60}m practiced",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            stats.weakTag?.let { weak ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Weak spot: ${weak.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Solved only ${weak.solved} of ${weak.total}. Filter by this tag and practice more")
                        }
                    }
                }
            }

            item { SectionTitle("By tag") }
            items(stats.byTag, key = { "tag-${it.name}" }) { g ->
                ProgressRow(g.name, g.solved, g.total, g.ratio)
            }

            item { SectionTitle("By rating") }
            items(stats.byRating, key = { "rating-${it.name}" }) { g ->
                ProgressRow(g.name, g.solved, g.total, g.ratio)
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
