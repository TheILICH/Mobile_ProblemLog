package com.example.problemlog.data

enum class Status(val label: String) {
    TODO("Todo"),
    ATTEMPTED("Attempted"),
    SOLVED("Solved")
}

enum class Verdict(val label: String) {
    AC("Accepted"),
    WA("Wrong answer"),
    TLE("Time limit"),
    MLE("Memory limit"),
    RE("Runtime error"),
    CE("Compile error");

    val isAccepted: Boolean
        get() = this == AC
}

data class Attempt(
    val verdict: Verdict,
    val minutes: Int,
    val timestamp: Long
)

data class Problem(
    val id: String,
    val title: String,
    val rating: Int,
    val tags: List<String>,
    val notes: String = "",
    val status: Status = Status.TODO,
    val attempts: List<Attempt> = emptyList()
) {
    // attempts list is oldest first
    val lastAttempt: Attempt?
        get() = attempts.lastOrNull()

    val totalMinutes: Int
        get() = attempts.sumOf { it.minutes }

    val solvedAt: Long?
        get() = attempts.lastOrNull { it.verdict.isAccepted }?.timestamp

    // "1850B" -> contest 1850 and index "B", used for sorting by id
    val contestNumber: Int
        get() = id.takeWhile { it.isDigit() }.toIntOrNull() ?: 0

    val index: String
        get() = id.dropWhile { it.isDigit() }
}
