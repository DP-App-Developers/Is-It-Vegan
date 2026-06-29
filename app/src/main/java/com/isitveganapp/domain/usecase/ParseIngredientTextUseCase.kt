package com.isitveganapp.domain.usecase

import javax.inject.Inject

class ParseIngredientTextUseCase @Inject constructor() {

    private val eNumberRegex = Regex("\\bE\\d{3,4}[a-z]?\\b", RegexOption.IGNORE_CASE)
    private val delimiterRegex = Regex("[,;/•·]")
    private val bracketRegex = Regex("\\(([^)]+)\\)")

    fun execute(rawOcrText: String): List<String> {
        val lower = rawOcrText.lowercase()

        // 1. Find the start of the primary ingredient/contains section.
        val startMarkers = listOf("ingredients:", "ingredients :", "contains:", "contains :")
        var mainText = rawOcrText
        for (marker in startMarkers) {
            val idx = lower.lastIndexOf(marker)
            if (idx != -1) {
                mainText = rawOcrText.substring(idx + marker.length)
                break
            }
        }

        // 2. If a secondary "CONTAINS:" allergen block appears later in the extracted text,
        //    split it into its own segment rather than trimming it away entirely.
        //    This means "INGREDIENTS: sugar, water. CONTAINS: MILK" correctly yields
        //    both the ingredient tokens AND "milk" from the allergen declaration.
        var allergenText = ""
        val mainLower = mainText.lowercase()
        for (marker in listOf("contains:", "contains :")) {
            val idx = mainLower.indexOf(marker)
            if (idx > 10) {
                allergenText = mainText.substring(idx + marker.length)
                mainText = mainText.substring(0, idx)
                break
            }
        }

        // 3. Trim both segments at other known section terminators.
        val terminators = listOf(
            "nutrition", "per 100g", "per 100 g", "typical values",
            "storage", "best before", "suitable for", "allergen", "may contain"
        )

        mainText = trimAtTerminators(mainText, terminators)
        allergenText = trimAtTerminators(allergenText, terminators)

        // 4. Extract E-numbers from the full raw text (they appear anywhere inline).
        val eNumbers = eNumberRegex.findAll(rawOcrText).map { it.value.uppercase() }.toList()

        // 5. Tokenize both segments and combine.
        return (tokenize(mainText) + tokenize(allergenText) + eNumbers).distinct()
    }

    private fun trimAtTerminators(text: String, terminators: List<String>): String {
        val lower = text.lowercase()
        val cut = terminators.mapNotNull { lower.indexOf(it).takeIf { it > 10 } }.minOrNull()
        return if (cut != null) text.substring(0, cut) else text
    }

    private fun tokenize(text: String): List<String> {
        val normalized = text.lowercase().replace(Regex("\\s+"), " ").trim()
        val expanded = normalized.replace(bracketRegex) { match ->
            val inner = match.groupValues[1]
            if (inner.contains(",")) ", $inner," else match.value
        }
        return expanded.split(delimiterRegex)
            .map { it.trim().replace(Regex("[^a-z0-9 \\-]"), " ").replace(Regex("\\s+"), " ").trim() }
            .filter { it.length > 1 }
    }
}
