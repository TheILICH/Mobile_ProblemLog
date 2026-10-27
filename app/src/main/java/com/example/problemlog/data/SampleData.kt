package com.example.problemlog.data

import com.example.problemlog.logic.withAttempts

private const val DAY_MS = 24L * 60 * 60 * 1000

// some real codeforces problems so the app is not empty on first launch
fun sampleProblems(now: Long = System.currentTimeMillis()): List<Problem> {
    fun attempt(verdict: Verdict, minutes: Int, daysAgo: Int) =
        Attempt(verdict, minutes, now - daysAgo * DAY_MS)

    return listOf(
        Problem("4A", "Watermelon", 800, listOf("math", "brute force"), "even and bigger than 2")
            .withAttempts(attempt(Verdict.AC, 5, 30)),
        Problem("71A", "Way Too Long Words", 800, listOf("strings"))
            .withAttempts(attempt(Verdict.AC, 7, 25)),
        Problem("1A", "Theatre Square", 1000, listOf("math"), "ceil division, use long")
            .withAttempts(attempt(Verdict.WA, 10, 20), attempt(Verdict.AC, 6, 20)),
        Problem("231A", "Team", 800, listOf("brute force", "greedy"))
            .withAttempts(attempt(Verdict.AC, 4, 3)),
        Problem("189A", "Cut Ribbon", 1300, listOf("dp", "brute force"), "like unbounded knapsack")
            .withAttempts(attempt(Verdict.WA, 35, 12), attempt(Verdict.TLE, 20, 10)),
        Problem("455A", "Boredom", 1500, listOf("dp"), "count values first, then dp on value")
            .withAttempts(attempt(Verdict.WA, 40, 6), attempt(Verdict.AC, 25, 5)),
        Problem("1352C", "K-th Not Divisible by n", 1200, listOf("math", "binary search"))
            .withAttempts(attempt(Verdict.AC, 15, 2)),
        Problem("230B", "T-primes", 1300, listOf("math", "number theory", "binary search"), "square of a prime")
            .withAttempts(attempt(Verdict.TLE, 25, 4)),
        Problem("580C", "Kefa and Park", 1500, listOf("graphs", "trees", "dfs and similar"))
            .withAttempts(attempt(Verdict.AC, 30, 18)),
        Problem("466C", "Number of Ways", 1700, listOf("dp", "two pointers", "binary search")),
        Problem("339D", "Xenia and Bit Operations", 1700, listOf("data structures", "trees")),
        Problem("166E", "Tetrahedron", 1500, listOf("dp", "math"), "try matrix power later")
            .withAttempts(attempt(Verdict.RE, 30, 1)),
        Problem("4C", "Registration System", 1300, listOf("data structures", "hashing", "implementation"))
            .withAttempts(attempt(Verdict.AC, 12, 8))
    )
}
