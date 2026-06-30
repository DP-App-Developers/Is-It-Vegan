package com.isitveganapp.domain.usecase

import javax.inject.Inject

class ParseIngredientTextUseCase @Inject constructor() {

    private val eNumberRegex = Regex("\\bE\\d{3,4}[a-z]?\\b", RegexOption.IGNORE_CASE)

    fun execute(rawOcrText: String): List<String> {
        val words = rawOcrText.lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .split(" ")
            .filter { it.length >= 2 }

        val candidates = ArrayList<String>(words.size * 3)
        for (i in words.indices) {
            for (len in 1..3) {
                if (i + len > words.size) break
                candidates.add(words.subList(i, i + len).joinToString(" "))
            }
        }

        val eNumbers = eNumberRegex.findAll(rawOcrText).map { it.value.uppercase() }.toList()

        return (candidates + eNumbers).distinct()
    }
}
