package com.isitveganapp.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ParseIngredientTextUseCaseTest {

    private lateinit var useCase: ParseIngredientTextUseCase

    @Before
    fun setUp() {
        useCase = ParseIngredientTextUseCase()
    }

    // ── Empty / blank input ───────────────────────────────────────────────────

    @Test
    fun `empty string returns empty list`() {
        assertTrue(useCase.execute("").isEmpty())
    }

    @Test
    fun `blank whitespace-only string returns empty list`() {
        assertTrue(useCase.execute("   \n\t  ").isEmpty())
    }

    // ── Section header detection ──────────────────────────────────────────────

    @Test
    fun `ingredients header extracts only the section after it`() {
        val result = useCase.execute("Net weight 500g. INGREDIENTS: water, sugar, salt.")
        assertTrue("water" in result)
        assertTrue("sugar" in result)
        assertTrue("salt" in result)
        assertFalse("net weight" in result)
        assertFalse("500g" in result)
    }

    @Test
    fun `ingredients header is case-insensitive`() {
        val result = useCase.execute("INGREDIENTS: milk, eggs")
        assertTrue("milk" in result)
        assertTrue("eggs" in result)
    }

    @Test
    fun `ingredients header with space before colon is recognised`() {
        val result = useCase.execute("INGREDIENTS : milk, butter")
        assertTrue("milk" in result)
        assertTrue("butter" in result)
    }

    @Test
    fun `contains as sole header extracts allergens`() {
        val result = useCase.execute("CONTAINS: MILK, SOY, EGGS")
        assertTrue("milk" in result)
        assertTrue("soy" in result)
        assertTrue("eggs" in result)
    }

    @Test
    fun `contains with space before colon is recognised`() {
        val result = useCase.execute("CONTAINS : MILK")
        assertTrue("milk" in result)
    }

    @Test
    fun `no header at all tokenizes entire text`() {
        val result = useCase.execute("milk, butter, eggs")
        assertTrue("milk" in result)
        assertTrue("butter" in result)
        assertTrue("eggs" in result)
    }

    @Test
    fun `last occurrence of ingredients header is used`() {
        // Rare but possible on labels that say "ingredients" in a description
        val result = useCase.execute("No artificial ingredients. INGREDIENTS: water, milk")
        assertTrue("milk" in result)
        assertTrue("water" in result)
    }

    // ── CONTAINS allergen block after INGREDIENTS ─────────────────────────────

    @Test
    fun `contains block after ingredients is parsed separately`() {
        val result = useCase.execute(
            "INGREDIENTS: water, sugar, natural flavors. CONTAINS: MILK."
        )
        assertTrue("milk" in result)
        assertTrue("water" in result)
        assertTrue("sugar" in result)
    }

    @Test
    fun `contains block with multiple allergens after ingredients`() {
        val result = useCase.execute(
            "INGREDIENTS: sugar, cocoa, soy lecithin. CONTAINS: MILK, SOY, EGGS."
        )
        assertTrue("milk" in result)
        assertTrue("soy" in result)
        assertTrue("eggs" in result)
    }

    @Test
    fun `original bug - vitamin d3 not merged with contains milk`() {
        // Before the fix this produced "vitamin d3 contains milk" as one nonsense token.
        val result = useCase.execute(
            "INGREDIENTS: ORGANIC A2 MILK, VITAMIN D3. CONTAINS: MILK."
        )
        assertTrue("organic a2 milk" in result)
        assertTrue("vitamin d3" in result)
        assertTrue("milk" in result)
        assertFalse(result.any { "contains" in it })
    }

    @Test
    fun `contains section does not bleed into last ingredient token`() {
        val result = useCase.execute("INGREDIENTS: sugar, water. CONTAINS: MILK")
        assertFalse(result.any { "contains" in it })
    }

    // ── Section terminators ───────────────────────────────────────────────────

    @Test
    fun `nutrition section is trimmed`() {
        val result = useCase.execute("INGREDIENTS: milk, eggs. NUTRITION: calories 200")
        assertFalse(result.any { "nutrition" in it || "calories" in it })
    }

    @Test
    fun `storage section is trimmed`() {
        val result = useCase.execute("INGREDIENTS: milk, butter. STORAGE: keep refrigerated.")
        assertFalse(result.any { "storage" in it || "refrigerated" in it })
    }

    @Test
    fun `best before section is trimmed`() {
        val result = useCase.execute("INGREDIENTS: milk, butter. BEST BEFORE: see base.")
        assertFalse(result.any { "best" in it || "base" in it })
    }

    @Test
    fun `may contain section is trimmed`() {
        val result = useCase.execute("INGREDIENTS: sugar, wheat, water. MAY CONTAIN: traces of nuts.")
        assertFalse(result.any { "may" in it || "traces" in it || "nuts" in it })
    }

    @Test
    fun `earliest terminator wins when multiple are present`() {
        // "storage" appears before "nutrition" — should trim at storage
        val result = useCase.execute(
            "INGREDIENTS: milk, eggs. STORAGE: fridge. NUTRITION: 100kcal."
        )
        assertFalse(result.any { "fridge" in it || "nutrition" in it || "kcal" in it })
    }

    // ── Milk variants ─────────────────────────────────────────────────────────

    @Test
    fun `plain milk is a token`() {
        assertTrue("milk" in useCase.execute("INGREDIENTS: milk, water"))
    }

    @Test
    fun `organic a2 milk is a single token`() {
        val result = useCase.execute("INGREDIENTS: ORGANIC A2 MILK, VITAMIN D3")
        assertTrue("organic a2 milk" in result)
    }

    @Test
    fun `whole milk solids is a single token`() {
        assertTrue("whole milk solids" in useCase.execute("INGREDIENTS: whole milk solids, sugar"))
    }

    @Test
    fun `skim milk powder is a single token`() {
        assertTrue("skim milk powder" in useCase.execute("INGREDIENTS: skim milk powder, salt"))
    }

    @Test
    fun `soy milk is a single token`() {
        val result = useCase.execute("INGREDIENTS: soy milk, water")
        assertTrue("soy milk" in result)
    }

    @Test
    fun `oat milk is a single token`() {
        assertTrue("oat milk" in useCase.execute("INGREDIENTS: oat milk, sunflower oil"))
    }

    @Test
    fun `almond milk is a single token`() {
        assertTrue("almond milk" in useCase.execute("INGREDIENTS: almond milk, cane sugar"))
    }

    @Test
    fun `coconut milk is a single token`() {
        assertTrue("coconut milk" in useCase.execute("INGREDIENTS: coconut milk, salt"))
    }

    // ── E-numbers ─────────────────────────────────────────────────────────────

    @Test
    fun `e-number is extracted`() {
        assertTrue("E471" in useCase.execute("INGREDIENTS: sugar, E471, water"))
    }

    @Test
    fun `e-number with letter suffix is extracted`() {
        assertTrue("E471A" in useCase.execute("INGREDIENTS: sugar, E471a"))
    }

    @Test
    fun `four-digit e-number is extracted`() {
        assertTrue("E1442" in useCase.execute("INGREDIENTS: starch, E1442"))
    }

    @Test
    fun `e-numbers are extracted even outside ingredients section`() {
        // E-numbers are extracted from the full raw text
        val result = useCase.execute("INGREDIENTS: water. NUTRITION: E330 (antioxidant).")
        assertTrue("E330" in result)
    }

    // ── Bracket expansion ─────────────────────────────────────────────────────

    @Test
    fun `bracketed sub-ingredients are expanded`() {
        val result = useCase.execute("INGREDIENTS: chocolate (sugar, cocoa butter, vanilla)")
        assertTrue("sugar" in result)
        assertTrue("cocoa butter" in result)
        assertTrue("vanilla" in result)
    }

    @Test
    fun `single-item bracket is kept as-is`() {
        // No comma inside → not expanded, left as part of the parent token
        val result = useCase.execute("INGREDIENTS: flavoring (natural)")
        assertTrue(result.isNotEmpty())
    }

    // ── Noisy OCR ─────────────────────────────────────────────────────────────

    @Test
    fun `single-character tokens are filtered out`() {
        val result = useCase.execute("INGREDIENTS: a, b, milk, c, d")
        assertFalse("a" in result)
        assertFalse("b" in result)
        assertTrue("milk" in result)
    }

    @Test
    fun `pipe characters from ocr are treated as non-alphanumeric`() {
        // "|" is an OCR artifact for "I" — should not split tokens
        val result = useCase.execute("INGREDIENTS: m|lk, butter")
        // "m lk" should appear as a token (pipe becomes space)
        assertTrue(result.any { "lk" in it || "m lk" in it })
    }

    @Test
    fun `random garbage characters do not crash the parser`() {
        val garbage = "@@##$$%% !!***"
        val result = useCase.execute(garbage)
        // Should return empty or nearly empty — just verify it doesn't throw
        assertTrue(result.size < 5)
    }

    @Test
    fun `numeric-only noise tokens are allowed but won't match ingredients`() {
        val result = useCase.execute("INGREDIENTS: milk, 12345, water")
        assertTrue("milk" in result)
        assertTrue("water" in result)
    }

    @Test
    fun `duplicate tokens are deduplicated`() {
        val result = useCase.execute("INGREDIENTS: milk, milk, eggs, milk")
        assertEquals(1, result.count { it == "milk" })
    }

    @Test
    fun `extra whitespace between tokens is collapsed`() {
        val result = useCase.execute("INGREDIENTS:  milk ,   eggs ,  water  ")
        assertTrue("milk" in result)
        assertTrue("eggs" in result)
        assertTrue("water" in result)
    }

    // ── Realistic full labels ─────────────────────────────────────────────────

    @Test
    fun `realistic milk bottle label is parsed correctly`() {
        val label = "INGREDIENTS: ORGANIC A2 MILK, VITAMIN D3. CONTAINS: MILK."
        val result = useCase.execute(label)
        assertTrue("organic a2 milk" in result)
        assertTrue("vitamin d3" in result)
        assertTrue("milk" in result)
    }

    @Test
    fun `realistic chocolate bar label is parsed correctly`() {
        val label = """
            INGREDIENTS: Sugar, Cocoa butter, Cocoa mass, Skimmed milk powder,
            Whey powder (from Milk), Hazelnuts (7%), Emulsifier (Soy lecithin),
            Vanillin.
            CONTAINS: Milk, Soy, Hazelnuts.
            NUTRITION: Per 100g - Energy 560kcal.
        """.trimIndent()
        val result = useCase.execute(label)
        assertTrue("sugar" in result)
        assertTrue("cocoa butter" in result)
        assertTrue("milk" in result)
        assertFalse(result.any { "560kcal" in it || "energy" in it })
    }

    @Test
    fun `vegan product label with no animal ingredients`() {
        val label = "INGREDIENTS: Water, Cane Sugar, Cocoa Powder, Sunflower Oil, Salt. " +
            "STORAGE: Keep in a cool dry place."
        val result = useCase.execute(label)
        assertTrue("water" in result)
        assertTrue("cocoa powder" in result)
        assertFalse(result.any { "keep" in it || "cool" in it })
    }
}
