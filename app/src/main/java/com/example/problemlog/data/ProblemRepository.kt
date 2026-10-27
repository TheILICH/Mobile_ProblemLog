package com.example.problemlog.data

import com.example.problemlog.logic.withAttempt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

// single source of truth for problems, in memory for now
object ProblemRepository {

    private val _problems = MutableStateFlow(sampleProblems())
    val problems: StateFlow<List<Problem>> = _problems.asStateFlow()

    fun find(id: String): Problem? = _problems.value.find { it.id == id }

    suspend fun upsert(problem: Problem) {
        withContext(Dispatchers.IO) {
            _problems.update { list ->
                if (list.any { it.id == problem.id }) {
                    list.map { if (it.id == problem.id) problem else it }
                } else {
                    list + problem
                }
            }
        }
    }

    suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            _problems.update { list -> list.filterNot { it.id == id } }
        }
    }

    suspend fun logAttempt(id: String, verdict: Verdict, minutes: Int) {
        withContext(Dispatchers.IO) {
            val attempt = Attempt(verdict, minutes, System.currentTimeMillis())
            _problems.update { list ->
                list.map { if (it.id == id) it.withAttempt(attempt) else it }
            }
        }
    }
}
