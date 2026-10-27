package com.example.problemlog.logic

import com.example.problemlog.data.Problem
import com.example.problemlog.data.Status
import com.example.problemlog.data.Verdict

const val MIN_RATING = 800
const val MAX_RATING = 3500

enum class TagMode(val label: String) {
    ANY("Any tag"),
    ALL("All tags")
}

enum class SortKey(val label: String) {
    STATUS("Status"),
    RATING("Rating"),
    LAST_ATTEMPT("Last attempt"),
    ATTEMPTS("Attempts"),
    MINUTES("Time spent"),
    ID("Problem id")
}

data class SortOption(val key: SortKey, val descending: Boolean = false)

data class FilterState(
    val statuses: Set<Status> = emptySet(),
    val tags: Set<String> = emptySet(),
    val tagMode: TagMode = TagMode.ANY,
    val ratingRange: IntRange = MIN_RATING..MAX_RATING,
    val verdicts: Set<Verdict> = emptySet(),
    val reviewOnly: Boolean = false,
    val primarySort: SortOption = SortOption(SortKey.STATUS),
    val secondarySort: SortOption? = SortOption(SortKey.RATING, descending = true)
) {
    // for the badge on filter button, sort is not counted
    val activeCount: Int
        get() = listOf(
            statuses.isNotEmpty(),
            tags.isNotEmpty(),
            ratingRange != MIN_RATING..MAX_RATING,
            verdicts.isNotEmpty(),
            reviewOnly
        ).count { it }
}

fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item

fun Problem.matchesQuery(query: String): Boolean {
    val q = query.trim()
    if (q.isEmpty()) return true
    return id.contains(q, ignoreCase = true) ||
        title.contains(q, ignoreCase = true) ||
        notes.contains(q, ignoreCase = true) ||
        tags.any { it.contains(q, ignoreCase = true) }
}

fun FilterState.matches(problem: Problem, now: Long): Boolean {
    if (statuses.isNotEmpty() && problem.status !in statuses) return false

    if (tags.isNotEmpty()) {
        val tagsOk = when (tagMode) {
            TagMode.ANY -> tags.any { it in problem.tags }
            TagMode.ALL -> problem.tags.containsAll(tags)
        }
        if (!tagsOk) return false
    }

    if (problem.rating !in ratingRange) return false

    if (verdicts.isNotEmpty()) {
        val last = problem.lastAttempt?.verdict ?: return false
        if (last !in verdicts) return false
    }

    if (reviewOnly && problem.reviewReason(now) == null) return false
    return true
}

fun SortOption.toComparator(): Comparator<Problem> {
    val base: Comparator<Problem> = when (key) {
        SortKey.STATUS -> compareBy<Problem> { it.status.ordinal }
        SortKey.RATING -> compareBy<Problem> { it.rating }
        SortKey.LAST_ATTEMPT -> compareBy<Problem> { it.lastAttempt?.timestamp ?: 0L }
        SortKey.ATTEMPTS -> compareBy<Problem> { it.attempts.size }
        SortKey.MINUTES -> compareBy<Problem> { it.totalMinutes }
        SortKey.ID -> compareBy<Problem> { it.contestNumber }.thenBy { it.index }
    }
    return if (descending) base.reversed() else base
}

// primary, then secondary, then id so order is always stable
fun FilterState.comparator(): Comparator<Problem> =
    listOfNotNull(primarySort, secondarySort)
        .map { it.toComparator() }
        .reduce { acc, next -> acc.then(next) }
        .thenBy { it.id }

fun applyFilterAndSort(
    problems: List<Problem>,
    query: String,
    filter: FilterState,
    now: Long
): List<Problem> =
    problems
        .filter { it.matchesQuery(query) && filter.matches(it, now) }
        .sortedWith(filter.comparator())

fun SortOption.text(): String = "${key.label} ${if (descending) "↓" else "↑"}"

fun FilterState.sortSummary(): String =
    if (secondarySort == null) primarySort.text()
    else "${primarySort.text()}, then ${secondarySort.text()}"
