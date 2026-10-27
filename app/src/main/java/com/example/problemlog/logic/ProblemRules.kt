package com.example.problemlog.logic

import com.example.problemlog.data.Attempt
import com.example.problemlog.data.Problem
import com.example.problemlog.data.Status
import com.example.problemlog.data.Verdict

const val REVIEW_AFTER_DAYS = 14
private const val DAY_MS = 24L * 60 * 60 * 1000

// main rule: ac -> solved, fail on todo -> attempted, otherwise keep status
fun nextStatus(current: Status, verdict: Verdict): Status = when {
    verdict.isAccepted -> Status.SOLVED
    current == Status.TODO -> Status.ATTEMPTED
    else -> current
}

fun Problem.withAttempt(attempt: Attempt): Problem =
    copy(attempts = attempts + attempt, status = nextStatus(status, attempt.verdict))

fun Problem.withAttempts(vararg list: Attempt): Problem =
    list.fold(this) { problem, attempt -> problem.withAttempt(attempt) }

fun daysBetween(from: Long, to: Long): Int = ((to - from) / DAY_MS).toInt()

fun daysAgoText(timestamp: Long, now: Long): String = when (val days = daysBetween(timestamp, now)) {
    0 -> "today"
    1 -> "yesterday"
    else -> "$days days ago"
}

// ---------- review (simple spaced repetition) ----------

sealed interface ReviewReason {
    data class LastFailed(val verdict: Verdict) : ReviewReason
    data class SolvedLongAgo(val days: Int) : ReviewReason
}

fun ReviewReason.text(): String = when (this) {
    is ReviewReason.LastFailed -> "Last attempt was ${verdict.name}"
    is ReviewReason.SolvedLongAgo -> "Solved $days days ago, time to redo"
}

fun Problem.reviewReason(now: Long): ReviewReason? {
    val last = lastAttempt ?: return null
    if (!last.verdict.isAccepted) return ReviewReason.LastFailed(last.verdict)
    val solved = solvedAt ?: return null
    val days = daysBetween(solved, now)
    return if (days > REVIEW_AFTER_DAYS) ReviewReason.SolvedLongAgo(days) else null
}

data class ReviewItem(val problem: Problem, val reason: ReviewReason)

fun buildReviewList(problems: List<Problem>, now: Long): List<ReviewItem> =
    problems
        .mapNotNull { p -> p.reviewReason(now)?.let { ReviewItem(p, it) } }
        // failed ones first, then oldest solved
        .sortedWith(
            compareBy<ReviewItem> { if (it.reason is ReviewReason.LastFailed) 0 else 1 }
                .thenByDescending { (it.reason as? ReviewReason.SolvedLongAgo)?.days ?: 0 }
        )

// ---------- add / edit form ----------

data class ProblemInput(
    val id: String,
    val title: String,
    val rating: String,
    val tags: String,
    val notes: String
)

sealed interface SaveResult {
    data object Saved : SaveResult
    data class Invalid(val message: String) : SaveResult
}

private val ID_REGEX = Regex("""\d+[A-Z]\d?""")

fun parseTags(text: String): List<String> =
    text.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct()

fun validateInput(input: ProblemInput, existingIds: Set<String>, editingId: String?): String? {
    val id = input.id.trim().uppercase()
    val rating = input.rating.trim().toIntOrNull()
    return when {
        !ID_REGEX.matches(id) -> "Id should look like 4A or 1850B"
        editingId == null && id in existingIds -> "$id is already in your list"
        input.title.isBlank() -> "Title is empty"
        rating == null -> "Rating must be a number"
        rating !in MIN_RATING..MAX_RATING || rating % 100 != 0 -> "Rating is 800..3500, step 100"
        parseTags(input.tags).isEmpty() -> "Add at least one tag"
        else -> null
    }
}

// keep old status and attempts when editing
fun ProblemInput.toProblem(base: Problem?): Problem {
    val fresh = Problem(
        id = id.trim().uppercase(),
        title = title.trim(),
        rating = rating.trim().toInt(),
        tags = parseTags(tags),
        notes = notes.trim()
    )
    return if (base == null) fresh else fresh.copy(status = base.status, attempts = base.attempts)
}
