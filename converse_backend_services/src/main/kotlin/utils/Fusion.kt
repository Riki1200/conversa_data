package utils

import models.Hit

fun rrfFuse(bm25: List<Hit>, knn: List<Hit>, k: Int = 60, topK: Int = 5): List<Hit> {
    val map = mutableMapOf<String, Pair<Double, Hit>>()
    fun add(list: List<Hit>) {
        list.forEachIndexed { idx, h ->
            val key = "${h.url}|${h.snippet.take(80)}"
            val score = 1.0 / (k + (idx + 1))
            val prev = map[key]
            if (prev == null) map[key] = score to h else map[key] = (prev.first + score) to h
        }
    }
    add(bm25); add(knn)
    return map.values.sortedByDescending { it.first }.map { it.second }.take(topK)
}