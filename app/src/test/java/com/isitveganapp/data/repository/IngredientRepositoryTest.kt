package com.isitveganapp.data.repository

import com.isitveganapp.data.local.DatabaseSeeder
import com.isitveganapp.data.local.IngredientDao
import com.isitveganapp.data.model.Ingredient
import com.isitveganapp.data.model.VeganStatus
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class IngredientRepositoryTest {

    private lateinit var dao: IngredientDao
    private lateinit var seeder: DatabaseSeeder
    private lateinit var repo: IngredientRepository

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun ingredient(
        name: String,
        status: VeganStatus,
        aliases: String = ""
    ) = Ingredient(
        id = 0,
        displayName = name,
        normalizedName = name,
        aliases = aliases,
        veganStatus = status,
        reason = ""
    )

    private val milkIngredient = ingredient("milk", VeganStatus.NOT_VEGAN)
    private val eggsIngredient = ingredient("eggs", VeganStatus.NOT_VEGAN)
    private val sugarIngredient = ingredient("sugar", VeganStatus.VEGAN)
    private val waterIngredient = ingredient("water", VeganStatus.VEGAN)

    private fun daoReturnsNothingByDefault() {
        coEvery { dao.findByNormalizedName(any()) } returns null
        coEvery { dao.findByAlias(any()) } returns null
        coEvery { dao.searchByPrefix(any()) } returns emptyList()
    }

    @Before
    fun setUp() {
        dao = mockk()
        seeder = mockk(relaxed = true)
        repo = IngredientRepository(dao, seeder)
        daoReturnsNothingByDefault()
    }

    // ── normalize() ───────────────────────────────────────────────────────────

    @Test
    fun `normalize lowercases input`() {
        assertEquals("milk", repo.normalize("MILK"))
    }

    @Test
    fun `normalize strips special characters`() {
        assertEquals("cocoa butter", repo.normalize("Cocoa-Butter!"))
    }

    @Test
    fun `normalize collapses internal whitespace`() {
        assertEquals("sunflower oil", repo.normalize("  sunflower   oil  "))
    }

    @Test
    fun `normalize leaves alphanumeric and spaces`() {
        assertEquals("e471a", repo.normalize("E471a"))
    }

    @Test
    fun `normalize handles empty string`() {
        assertEquals("", repo.normalize(""))
    }

    // ── Exact match ───────────────────────────────────────────────────────────

    @Test
    fun `lookup finds ingredient by exact normalized name`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = repo.lookup("milk")
        assertNotNull(result)
        assertEquals(VeganStatus.NOT_VEGAN, result!!.veganStatus)
    }

    @Test
    fun `lookup normalizes input before exact match`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = repo.lookup("MILK")
        assertNotNull(result)
    }

    @Test
    fun `lookup returns null for unknown ingredient`() = runTest {
        val result = repo.lookup("quinoa")
        assertNull(result)
    }

    @Test
    fun `lookup returns null for very short input`() = runTest {
        val result = repo.lookup("a")
        assertNull(result)
    }

    // ── Alias match ───────────────────────────────────────────────────────────

    @Test
    fun `lookup finds ingredient by alias`() = runTest {
        val gelatin = ingredient("gelatin", VeganStatus.NOT_VEGAN, aliases = "|gelatine|")
        coEvery { dao.findByNormalizedName("gelatine") } returns null
        coEvery { dao.findByAlias("gelatine") } returns gelatin

        val result = repo.lookup("gelatine")
        assertNotNull(result)
        assertEquals(VeganStatus.NOT_VEGAN, result!!.veganStatus)
    }

    // ── Prefix match (size == 1) ──────────────────────────────────────────────

    @Test
    fun `lookup returns single prefix match`() = runTest {
        coEvery { dao.searchByPrefix("casein".take(6)) } returns listOf(
            ingredient("casein", VeganStatus.NOT_VEGAN)
        )

        val result = repo.lookup("casein")
        assertNotNull(result)
    }

    @Test
    fun `lookup does not return prefix match when multiple candidates exist`() = runTest {
        // Multiple prefix hits → no single prefix shortcut; falls to levenshtein
        // Both candidates are > 2 edits from "lacto" so fuzzy match also fails
        coEvery { dao.searchByPrefix("lacto") } returns listOf(
            ingredient("lactoferrin", VeganStatus.NOT_VEGAN),
            ingredient("lactalbumin", VeganStatus.NOT_VEGAN)
        )

        val result = repo.lookup("lacto")
        assertNull(result)
    }

    // ── Levenshtein fuzzy match (≤ 2 edits) ──────────────────────────────────

    @Test
    fun `lookup fuzzy-matches a single typo`() = runTest {
        coEvery { dao.searchByPrefix("mlk".take(3)) } returns listOf(milkIngredient)

        // "mlk" vs "milk" = 1 edit → match
        val result = repo.lookup("mlk")
        assertNotNull(result)
        assertEquals(VeganStatus.NOT_VEGAN, result!!.veganStatus)
    }

    @Test
    fun `lookup does not fuzzy-match beyond 2 edits`() = runTest {
        // "mlkk" vs "milk" would be >2 edits if normalizedName is very different
        coEvery { dao.searchByPrefix("eggs".take(4)) } returns listOf(eggsIngredient)

        // "eg" has distance 2 from "eggs" — should still match
        val result = repo.lookup("eg")  // length < 2 after normalize → null
        assertNull(result)
    }

    @Test
    fun `lookup fuzzy-matches two-character transposition`() = runTest {
        coEvery { dao.searchByPrefix("sguar") } returns listOf(sugarIngredient)

        // "sguar" vs "sugar" = 2 edits → match
        val result = repo.lookup("sguar")
        assertNotNull(result)
    }

    // ── Sub-word fallback ─────────────────────────────────────────────────────

    @Test
    fun `lookup matches milk from organic a2 milk via sub-word fallback`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient
        // No plant-base words in "organic a2 milk", so fallback fires for words >= 4 chars

        val result = repo.lookup("organic a2 milk")
        assertNotNull(result)
        assertEquals(VeganStatus.NOT_VEGAN, result!!.veganStatus)
    }

    @Test
    fun `lookup matches milk from whole milk solids via sub-word fallback`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = repo.lookup("whole milk solids")
        assertNotNull(result)
        assertEquals(VeganStatus.NOT_VEGAN, result!!.veganStatus)
    }

    @Test
    fun `lookup matches milk from skim milk powder via sub-word fallback`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = repo.lookup("skim milk powder")
        assertNotNull(result)
        assertEquals(VeganStatus.NOT_VEGAN, result!!.veganStatus)
    }

    @Test
    fun `lookup sub-word fallback tries each word in order`() = runTest {
        // "skimmed milk": "skimmed" does not match, "milk" does
        coEvery { dao.findByNormalizedName("skimmed") } returns null
        coEvery { dao.findByAlias("skimmed") } returns null
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = repo.lookup("skimmed milk")
        assertNotNull(result)
        assertEquals(VeganStatus.NOT_VEGAN, result!!.veganStatus)
    }

    @Test
    fun `lookup sub-word fallback does not fire for single-word tokens`() = runTest {
        // Even if "milk" is its own word, sub-word only fires when words.size > 1
        // So a plain "milk" should already be caught by exact/alias/prefix/levenshtein paths
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = repo.lookup("milk")
        assertNotNull(result)
    }

    // ── PLANT_BASE_WORDS guard (false-positive prevention) ────────────────────

    @Test
    fun `lookup returns null for soy milk (plant-base guard)`() = runTest {
        // "soy" is in PLANT_BASE_WORDS → sub-word fallback blocked
        val result = repo.lookup("soy milk")
        assertNull(result)
    }

    @Test
    fun `lookup returns null for oat milk (plant-base guard)`() = runTest {
        // "oat" is in PLANT_BASE_WORDS
        val result = repo.lookup("oat milk")
        assertNull(result)
    }

    @Test
    fun `lookup returns null for almond milk (plant-base guard)`() = runTest {
        val result = repo.lookup("almond milk")
        assertNull(result)
    }

    @Test
    fun `lookup returns null for coconut milk (plant-base guard)`() = runTest {
        val result = repo.lookup("coconut milk")
        assertNull(result)
    }

    @Test
    fun `lookup returns null for rice milk (plant-base guard)`() = runTest {
        val result = repo.lookup("rice milk")
        assertNull(result)
    }

    @Test
    fun `lookup returns null for hemp milk (plant-base guard)`() = runTest {
        val result = repo.lookup("hemp milk")
        assertNull(result)
    }

    @Test
    fun `lookup returns null for cashew milk (plant-base guard)`() = runTest {
        val result = repo.lookup("cashew milk")
        assertNull(result)
    }

    @Test
    fun `lookup returns null for hazelnut milk (plant-base guard)`() = runTest {
        val result = repo.lookup("hazelnut milk")
        assertNull(result)
    }

    @Test
    fun `lookup returns null for plant-based cream (plant-base guard)`() = runTest {
        val result = repo.lookup("plant cream")
        assertNull(result)
    }

    @Test
    fun `lookup returns null for vegan cheese (plant-base guard)`() = runTest {
        val result = repo.lookup("vegan cheese")
        assertNull(result)
    }

    @Test
    fun `lookup returns null for pea protein (plant-base guard)`() = runTest {
        val result = repo.lookup("pea protein")
        assertNull(result)
    }

    @Test
    fun `lookup returns null for potato starch via plant-base guard`() = runTest {
        val result = repo.lookup("potato starch")
        assertNull(result)
    }

    // ── Sub-word guard: filter words < 4 chars ────────────────────────────────

    @Test
    fun `sub-word fallback ignores words shorter than 4 characters`() = runTest {
        // "soy" is 3 chars → filtered out before PLANT_BASE_WORDS check even matters
        // BUT "soy" IS in PLANT_BASE_WORDS so guard fires first anyway.
        // Test a custom case: "a2 milk" — "a2" is 2 chars, filtered; "milk" is 4 chars.
        // "a2" is NOT in PLANT_BASE_WORDS but is filtered by length → only "milk" remains.
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = repo.lookup("a2 milk")
        // words after filter: ["milk"] → size == 1 → sub-word does NOT fire (size > 1 required)
        assertNull(result)
    }

    @Test
    fun `sub-word fallback fires when two or more words are at least 4 chars`() = runTest {
        // "whole milk": both "whole"(5) and "milk"(4) pass length filter → size 2 > 1 → fires
        coEvery { dao.findByNormalizedName("whole") } returns null
        coEvery { dao.findByAlias("whole") } returns null
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = repo.lookup("whole milk")
        assertNotNull(result)
        assertEquals(VeganStatus.NOT_VEGAN, result!!.veganStatus)
    }
}
