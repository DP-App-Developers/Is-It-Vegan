package com.isitveganapp.domain.usecase

import javax.inject.Inject

class ParseIngredientTextUseCase @Inject constructor() {

    private val eNumberRegex = Regex("\\bE\\d{3,4}[a-z]?\\b", RegexOption.IGNORE_CASE)
    private val delimiterRegex = Regex("[,;/•·]")
    private val bracketRegex = Regex("\\(([^)]+)\\)")

    fun execute(rawOcrText: String): List<String> {
        // Find the ingredients section — look for the last occurrence of the header
        val ingredientsMarkers = listOf("ingredients:", "ingredients :", "contains:", "contains :")
        var text = rawOcrText
        for (marker in ingredientsMarkers) {
            val idx = rawOcrText.lowercase().lastIndexOf(marker)
            if (idx != -1) {
                text = rawOcrText.substring(idx + marker.length)
                break
            }
        }

        // Trim at the earliest section terminator found in the text.
        // "contains" handles allergen-declaration lines like "CONTAINS: MILK." that appear
        // after the ingredient list — it's safe to include because if "contains:" was the
        // ingredient-section header, it was already consumed above and won't appear again.
        val terminators = listOf("nutrition", "per 100g", "per 100 g", "typical values",
            "storage", "best before", "suitable for", "allergen", "may contain", "contains")
        val textLower = text.lowercase()
        val cutAt = terminators
            .mapNotNull { term -> textLower.indexOf(term).takeIf { it > 10 } }
            .minOrNull()
        if (cutAt != null) text = text.substring(0, cutAt)

        val normalized = text.lowercase().replace(Regex("\\s+"), " ").trim()

        // Extract E-numbers separately before splitting (they appear inline)
        val eNumbers = eNumberRegex.findAll(rawOcrText).map { it.value.uppercase() }.toList()

        // Expand bracket sub-ingredients then split
        val expanded = normalized.replace(bracketRegex) { match ->
            val inner = match.groupValues[1]
            if (inner.contains(",")) ", $inner," else match.value
        }

        val tokens = expanded.split(delimiterRegex)
            .map { it.trim().replace(Regex("[^a-z0-9 \\-]"), " ").replace(Regex("\\s+"), " ").trim() }
            .filter { it.length > 1 }

        return (tokens + eNumbers).distinct()
    }
}
