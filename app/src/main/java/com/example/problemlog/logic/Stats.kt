package com.example.problemlog.logic

import com.example.problemlog.data.Problem
import com.example.problemlog.data.Status

data class GroupStat(val name: String, val solved: Int, val total: Int) {
    val ratio: Float
        get() = if (total == 0) 0f else solved.toFloat() / total
}

data class Stats(
    val total: Int,
    val solved: Int,
    val attempted: Int,
    val todo: Int,
    val attempts: Int,
    val accepted: Int,
    val minutes: Int,
    val byTag: List<GroupStat>,
    val byRating: List<GroupStat>,
    val weakTag: GroupStat?
) {
    val acRate: Int
        get() = if (attempts == 0) 0 else accepted * 100 / attempts
}

private fun List<Problem>.solvedCount() = count { it.status == Status.SOLVED }

fun computeStats(problems: List<Problem>): Stats {
    val allAttempts = problems.flatMap { it.attempts }

    // one problem with 3 tags goes to 3 groups
    val byTag = problems
        .flatMap { p -> p.tags.map { tag -> tag to p } }
        .groupBy({ it.first }, { it.second })
        .map { (tag, list) -> GroupStat(tag, list.solvedCount(), list.size) }
        .sortedWith(compareByDescending<GroupStat> { it.total }.thenBy { it.name })

    // buckets of 200: 800-999, 1000-1199 ...
    val byRating = problems
        .groupBy { it.rating / 200 * 200 }
        .toSortedMap()
        .map { (start, list) -> GroupStat("$start-${start + 199}", list.solvedCount(), list.size) }

    // weak spot = lowest solved ratio, only tags with 2+ problems so it means something
    val weakTag = byTag.filter { it.total >= 2 }.minByOrNull { it.ratio }

    return Stats(
        total = problems.size,
        solved = problems.solvedCount(),
        attempted = problems.count { it.status == Status.ATTEMPTED },
        todo = problems.count { it.status == Status.TODO },
        attempts = allAttempts.size,
        accepted = allAttempts.count { it.verdict.isAccepted },
        minutes = allAttempts.sumOf { it.minutes },
        byTag = byTag,
        byRating = byRating,
        weakTag = weakTag
    )
}
