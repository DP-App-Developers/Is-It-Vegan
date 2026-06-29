package com.isitveganapp.data.repository

import com.isitveganapp.data.local.DatabaseSeeder
import com.isitveganapp.data.local.IngredientDao
import com.isitveganapp.data.model.Ingredient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngredientRepository @Inject constructor(
    private val dao: IngredientDao,
    private val seeder: DatabaseSeeder
) {
    suspend fun ensureSeeded() = seeder.seedIfEmpty()

    suspend fun lookup(rawName: String): Ingredient? {
        val normalized = normalize(rawName)
        if (normalized.length < 2) return null

        dao.findByNormalizedName(normalized)?.let { return it }
        dao.findByAlias(normalized)?.let { return it }

        val prefix = normalized.take(6)
        val prefixResults = dao.searchByPrefix(prefix)

        if (prefixResults.size == 1) return prefixResults.first()

        prefixResults
            .minByOrNull { levenshtein(it.normalizedName, normalized) }
            ?.takeIf { levenshtein(it.normalizedName, normalized) <= 2 }
            ?.let { return it }

        // Sub-word fallback: for qualified names like "organic a2 milk" or "whole milk solids",
        // try each individual word so the meaningful ingredient name is still matched.
        // Guard: skip if the token contains a plant-base word — that signals the whole
        // compound is plant-derived (e.g. "almond milk", "coconut cream") and matching
        // "milk"/"cream" alone would be a false positive.
        val rawWords = normalized.split(" ")
        if (rawWords.any { it in PLANT_BASE_WORDS }) return null

        val words = rawWords.filter { it.length >= 4 }
        if (words.size > 1) {
            for (word in words) {
                dao.findByNormalizedName(word)?.let { return it }
                dao.findByAlias(word)?.let { return it }
            }
        }

        return null
    }

    companion object {
        // Words that signal a compound ingredient is plant-derived.
        // Presence of any of these blocks the sub-word fallback so "almond milk" never
        // matches "milk", "coconut cream" never matches "cream", etc.
        private val PLANT_BASE_WORDS = setOf(
            "soy", "soya", "almond", "oat", "oats", "rice", "hemp",
            "coconut", "cashew", "hazelnut", "macadamia", "walnut",
            "pea", "potato", "chickpea", "lentil", "plant", "vegan"
        )
    }

    fun normalize(raw: String): String =
        raw.lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length)
            for (j in 1..b.length)
                dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
                else 1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
        return dp[a.length][b.length]
    }
}
