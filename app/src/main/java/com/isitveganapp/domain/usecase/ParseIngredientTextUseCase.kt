package com.isitveganapp.domain.usecase

import javax.inject.Inject

class ParseIngredientTextUseCase @Inject constructor() {

    private val eNumberRegex = Regex("\\bE\\d{3,4}[a-z]?\\b", RegexOption.IGNORE_CASE)

    // Plant-based compound phrases whose constituent words would otherwise falsely match
    // animal-product DB entries. E.g. "milk" in "soy milk" should not flag dairy Milk.
    // Only includes phrases where a sub-word creates a false NOT_VEGAN/UNCERTAIN hit.
    private val veganCompounds: Set<String> = setOf(
        // Plant milks
        "soy milk", "oat milk", "almond milk", "coconut milk", "rice milk",
        "hemp milk", "cashew milk", "pea milk", "flax milk", "macadamia milk",
        "hazelnut milk", "walnut milk", "pistachio milk", "banana milk",
        "quinoa milk", "potato milk", "grain milk", "nut milk", "plant milk",
        "oat milk powder", "soy milk powder", "coconut milk powder", "almond milk powder",
        // Plant creams
        "coconut cream", "oat cream", "soy cream", "cashew cream", "coconut whipping cream",
        // Plant butters (peanut/nut/seed butters, cocoa/cacao butter from beans)
        "peanut butter", "almond butter", "cashew butter", "sunflower butter",
        "cocoa butter", "cacao butter", "nut butter", "seed butter",
        "walnut butter", "hazelnut butter", "pistachio butter", "macadamia butter",
        // Plant-based lecithins (lecithin alone is UNCERTAIN; these are vegan)
        "sunflower lecithin", "soy lecithin", "soya lecithin",
    )

    fun execute(rawOcrText: String): List<String> {
        val words = rawOcrText.lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .split(" ")
            .filter { it.length >= 2 }

        // Mark positions consumed by a vegan compound so their 1-gram is not emitted.
        // Longer n-grams spanning those positions are still emitted normally.
        val suppressed = BooleanArray(words.size)
        for (i in words.indices) {
            for (len in 2..3) {
                if (i + len > words.size) break
                val phrase = words.subList(i, i + len).joinToString(" ")
                if (phrase in veganCompounds) {
                    for (j in i until i + len) suppressed[j] = true
                }
            }
        }

        val candidates = ArrayList<String>(words.size * 3)
        for (i in words.indices) {
            for (len in 1..3) {
                if (i + len > words.size) break
                if (len == 1 && suppressed[i]) continue
                candidates.add(words.subList(i, i + len).joinToString(" "))
            }
        }

        val eNumbers = eNumberRegex.findAll(rawOcrText).map { it.value.uppercase() }.toList()

        return (candidates + eNumbers).distinct()
    }
}
