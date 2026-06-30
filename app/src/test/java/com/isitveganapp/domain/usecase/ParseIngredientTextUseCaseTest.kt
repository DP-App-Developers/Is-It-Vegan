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

    @Test
    fun `random garbage characters do not crash the parser`() {
        val result = useCase.execute("@@##$$%% !!***")
        assertTrue(result.size < 5)
    }

    // ── Full-text n-gram scanning ─────────────────────────────────────────────

    @Test
    fun `every word from the text appears as a 1-gram candidate`() {
        val result = useCase.execute("milk eggs water")
        assertTrue("milk" in result)
        assertTrue("eggs" in result)
        assertTrue("water" in result)
    }

    @Test
    fun `text before an ingredients header is also scanned`() {
        val result = useCase.execute("Net weight 500g. INGREDIENTS: water, sugar, salt.")
        assertTrue("water" in result)
        assertTrue("sugar" in result)
        assertTrue("salt" in result)
        // Pre-header words are also candidates
        assertTrue("ingredients" in result)
    }

    @Test
    fun `consecutive 2-word phrases are generated`() {
        val result = useCase.execute("soy lecithin water sugar")
        assertTrue("soy lecithin" in result)
        assertTrue("lecithin water" in result)
    }

    @Test
    fun `consecutive 3-word phrases are generated`() {
        val result = useCase.execute("whole milk solids sugar")
        assertTrue("whole milk solids" in result)
    }

    @Test
    fun `4-word phrases are not generated`() {
        val result = useCase.execute("organic whole milk solids")
        assertFalse(result.any { it.split(" ").size == 4 })
    }

    @Test
    fun `single-character tokens are filtered out`() {
        val result = useCase.execute("a b milk c d")
        assertFalse("a" in result)
        assertFalse("b" in result)
        assertTrue("milk" in result)
    }

    @Test
    fun `text without any ingredient header is fully scanned`() {
        val result = useCase.execute("milk, butter, eggs")
        assertTrue("milk" in result)
        assertTrue("butter" in result)
        assertTrue("eggs" in result)
    }

    // ── Multi-word ingredient detection ──────────────────────────────────────

    @Test
    fun `soy milk appears as a 2-gram`() {
        assertTrue("soy milk" in useCase.execute("soy milk water"))
    }

    @Test
    fun `oat milk appears as a 2-gram`() {
        assertTrue("oat milk" in useCase.execute("oat milk sunflower oil"))
    }

    @Test
    fun `almond milk appears as a 2-gram`() {
        assertTrue("almond milk" in useCase.execute("almond milk cane sugar"))
    }

    @Test
    fun `coconut milk appears as a 2-gram`() {
        assertTrue("coconut milk" in useCase.execute("coconut milk salt"))
    }

    @Test
    fun `whole milk solids appears as a 3-gram`() {
        assertTrue("whole milk solids" in useCase.execute("INGREDIENTS: whole milk solids, sugar"))
    }

    @Test
    fun `skim milk powder appears as a 3-gram`() {
        assertTrue("skim milk powder" in useCase.execute("INGREDIENTS: skim milk powder, salt"))
    }

    @Test
    fun `organic a2 milk appears as a 3-gram`() {
        assertTrue("organic a2 milk" in useCase.execute("INGREDIENTS: ORGANIC A2 MILK, VITAMIN D3"))
    }

    @Test
    fun `soy lecithin appears as a 2-gram`() {
        assertTrue("soy lecithin" in useCase.execute("chocolate (soy lecithin)"))
    }

    @Test
    fun `milk is also a 1-gram when inside a multi-word phrase`() {
        val result = useCase.execute("INGREDIENTS: sugar, whole milk solids")
        assertTrue("milk" in result)
        assertTrue("whole milk solids" in result)
    }

    // ── E-numbers ─────────────────────────────────────────────────────────────

    @Test
    fun `e-number is extracted`() {
        assertTrue("E471" in useCase.execute("sugar, E471, water"))
    }

    @Test
    fun `e-number with letter suffix is extracted`() {
        assertTrue("E471A" in useCase.execute("sugar, E471a"))
    }

    @Test
    fun `four-digit e-number is extracted`() {
        assertTrue("E1442" in useCase.execute("starch, E1442"))
    }

    @Test
    fun `e-numbers are extracted from anywhere in the label`() {
        val result = useCase.execute("INGREDIENTS: water. NUTRITION: E330 (antioxidant).")
        assertTrue("E330" in result)
    }

    // ── Deduplication and noise ───────────────────────────────────────────────

    @Test
    fun `duplicate tokens are deduplicated`() {
        val result = useCase.execute("milk milk eggs milk")
        assertEquals(1, result.count { it == "milk" })
    }

    @Test
    fun `extra whitespace between tokens is collapsed`() {
        val result = useCase.execute("  milk   eggs   water  ")
        assertTrue("milk" in result)
        assertTrue("eggs" in result)
        assertTrue("water" in result)
    }

    @Test
    fun `pipe characters from ocr are treated as non-alphanumeric`() {
        val result = useCase.execute("m|lk butter")
        assertTrue(result.any { "lk" in it })
    }

    @Test
    fun `numeric-only tokens are included (they will fail DB lookup harmlessly)`() {
        val result = useCase.execute("milk 12345 water")
        assertTrue("milk" in result)
        assertTrue("water" in result)
    }
}
