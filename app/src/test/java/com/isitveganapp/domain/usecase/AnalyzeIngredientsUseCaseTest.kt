package com.isitveganapp.domain.usecase

import com.isitveganapp.data.local.DatabaseSeeder
import com.isitveganapp.data.local.IngredientDao
import com.isitveganapp.data.model.Ingredient
import com.isitveganapp.data.model.VeganStatus
import com.isitveganapp.data.repository.IngredientRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * End-to-end pipeline tests: real ParseIngredientTextUseCase + real IngredientRepository
 * (with a mocked DAO) + real AnalyzeIngredientsUseCase.
 *
 * This catches integration bugs where parsing and lookup interact unexpectedly.
 */
class AnalyzeIngredientsUseCaseTest {

    private lateinit var dao: IngredientDao
    private lateinit var repo: IngredientRepository
    private lateinit var parseUseCase: ParseIngredientTextUseCase
    private lateinit var analyzeUseCase: AnalyzeIngredientsUseCase

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun makeIngredient(name: String, status: VeganStatus, aliases: String = "") =
        Ingredient(
            id = 0,
            displayName = name,
            normalizedName = name,
            aliases = aliases,
            veganStatus = status,
            reason = ""
        )

    private val milkIngredient = makeIngredient("milk", VeganStatus.NOT_VEGAN)
    private val eggsIngredient = makeIngredient("eggs", VeganStatus.NOT_VEGAN)
    private val honeyIngredient = makeIngredient("honey", VeganStatus.NOT_VEGAN)
    private val gelatinIngredient = makeIngredient("gelatin", VeganStatus.NOT_VEGAN, "|gelatine|")
    private val sugarIngredient = makeIngredient("sugar", VeganStatus.VEGAN)
    private val waterIngredient = makeIngredient("water", VeganStatus.VEGAN)
    private val saltIngredient = makeIngredient("salt", VeganStatus.VEGAN)
    private val cocoaIngredient = makeIngredient("cocoa", VeganStatus.VEGAN)

    @Before
    fun setUp() {
        dao = mockk()
        val seeder: DatabaseSeeder = mockk(relaxed = true)
        repo = IngredientRepository(dao, seeder)
        parseUseCase = ParseIngredientTextUseCase()
        analyzeUseCase = AnalyzeIngredientsUseCase(repo, parseUseCase)

        // Default: nothing found
        coEvery { dao.findByNormalizedName(any()) } returns null
        coEvery { dao.findByAlias(any()) } returns null
        coEvery { dao.searchByPrefix(any()) } returns emptyList()
    }

    // ── Empty / uncertain ─────────────────────────────────────────────────────

    @Test
    fun `empty OCR text returns UNCERTAIN`() = runTest {
        val result = analyzeUseCase.execute("")
        assertEquals(VeganStatus.UNCERTAIN, result.overallStatus)
        assertTrue(result.flaggedIngredients.isEmpty())
    }

    @Test
    fun `OCR text with no recognizable ingredients returns VEGAN`() = runTest {
        // Some tokens are present but none match anything in DB → VEGAN (no flags)
        val result = analyzeUseCase.execute("INGREDIENTS: water, salt")
        assertEquals(VeganStatus.VEGAN, result.overallStatus)
        assertTrue(result.flaggedIngredients.isEmpty())
    }

    // ── Vegan products ────────────────────────────────────────────────────────

    @Test
    fun `fully vegan ingredients list returns VEGAN`() = runTest {
        coEvery { dao.findByNormalizedName("sugar") } returns sugarIngredient
        coEvery { dao.findByNormalizedName("cocoa") } returns cocoaIngredient
        coEvery { dao.findByNormalizedName("salt") } returns saltIngredient

        val result = analyzeUseCase.execute("INGREDIENTS: sugar, cocoa, salt")
        assertEquals(VeganStatus.VEGAN, result.overallStatus)
        assertTrue(result.flaggedIngredients.isEmpty())
    }

    @Test
    fun `soy milk label returns VEGAN (plant-base guard prevents false positive)`() = runTest {
        // "soy milk" token → PLANT_BASE_WORDS guard → lookup returns null → no NOT_VEGAN flag
        coEvery { dao.findByNormalizedName("water") } returns waterIngredient

        val result = analyzeUseCase.execute("INGREDIENTS: soy milk, water")
        assertEquals(VeganStatus.VEGAN, result.overallStatus)
        assertTrue(result.flaggedIngredients.isEmpty())
    }

    @Test
    fun `oat milk label returns VEGAN (plant-base guard)`() = runTest {
        val result = analyzeUseCase.execute("INGREDIENTS: oat milk, sunflower oil")
        assertEquals(VeganStatus.VEGAN, result.overallStatus)
    }

    @Test
    fun `almond milk label returns VEGAN (plant-base guard)`() = runTest {
        val result = analyzeUseCase.execute("INGREDIENTS: almond milk, cane sugar")
        assertEquals(VeganStatus.VEGAN, result.overallStatus)
    }

    @Test
    fun `coconut milk label returns VEGAN (plant-base guard)`() = runTest {
        val result = analyzeUseCase.execute("INGREDIENTS: coconut milk, salt")
        assertEquals(VeganStatus.VEGAN, result.overallStatus)
    }

    // ── Not vegan products ────────────────────────────────────────────────────

    @Test
    fun `plain milk returns NOT_VEGAN`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = analyzeUseCase.execute("INGREDIENTS: milk, water")
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
        assertTrue(result.flaggedIngredients.any { it.ingredient.normalizedName == "milk" })
    }

    @Test
    fun `cow milk bottle (organic a2 milk) returns NOT_VEGAN`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = analyzeUseCase.execute(
            "INGREDIENTS: ORGANIC A2 MILK, VITAMIN D3. CONTAINS: MILK."
        )
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
        assertTrue(result.flaggedIngredients.any { it.ingredient.normalizedName == "milk" })
    }

    @Test
    fun `contains milk allergen declaration triggers NOT_VEGAN`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = analyzeUseCase.execute(
            "INGREDIENTS: natural flavors, water. CONTAINS: MILK."
        )
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
        assertTrue(result.flaggedIngredients.any { it.ingredient.normalizedName == "milk" })
    }

    @Test
    fun `eggs in ingredients list returns NOT_VEGAN`() = runTest {
        coEvery { dao.findByNormalizedName("eggs") } returns eggsIngredient

        val result = analyzeUseCase.execute("INGREDIENTS: flour, eggs, sugar")
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
        assertTrue(result.flaggedIngredients.any { it.ingredient.normalizedName == "eggs" })
    }

    @Test
    fun `honey returns NOT_VEGAN`() = runTest {
        coEvery { dao.findByNormalizedName("honey") } returns honeyIngredient

        val result = analyzeUseCase.execute("INGREDIENTS: oats, honey, almonds")
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
        assertTrue(result.flaggedIngredients.any { it.ingredient.normalizedName == "honey" })
    }

    @Test
    fun `gelatin alias match triggers NOT_VEGAN`() = runTest {
        coEvery { dao.findByAlias("gelatine") } returns gelatinIngredient

        val result = analyzeUseCase.execute("INGREDIENTS: sugar, gelatine, water")
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
    }

    @Test
    fun `multiple non-vegan ingredients all appear in flagged list`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient
        coEvery { dao.findByNormalizedName("eggs") } returns eggsIngredient

        val result = analyzeUseCase.execute("INGREDIENTS: milk, eggs, sugar")
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
        assertEquals(2, result.flaggedIngredients.size)
    }

    // ── Sub-word fallback end-to-end ──────────────────────────────────────────

    @Test
    fun `whole milk solids returns NOT_VEGAN via sub-word fallback`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = analyzeUseCase.execute("INGREDIENTS: sugar, whole milk solids")
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
    }

    @Test
    fun `skim milk powder returns NOT_VEGAN via sub-word fallback`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = analyzeUseCase.execute("INGREDIENTS: skim milk powder, cocoa")
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
    }

    // ── CONTAINS block after INGREDIENTS ──────────────────────────────────────

    @Test
    fun `contains block alone (no ingredients header) extracts allergens`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = analyzeUseCase.execute("CONTAINS: MILK, SOY, EGGS")
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
    }

    @Test
    fun `ingredients header followed by contains block both parsed`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient
        coEvery { dao.findByNormalizedName("sugar") } returns sugarIngredient

        val result = analyzeUseCase.execute(
            "INGREDIENTS: sugar, cocoa butter, natural flavors. CONTAINS: MILK."
        )
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
        // Ingredients section tokens should also be present
        assertTrue(result.parsedTokens.contains("sugar"))
    }

    @Test
    fun `milk in contains block is not confused with vitamin d3 token`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = analyzeUseCase.execute(
            "INGREDIENTS: ORGANIC A2 MILK, VITAMIN D3. CONTAINS: MILK."
        )
        // The parser must not produce a token like "vitamin d3 contains milk"
        assertTrue(result.parsedTokens.none { "contains" in it })
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
    }

    // ── Noisy OCR simulation ──────────────────────────────────────────────────

    @Test
    fun `noisy OCR with mlk typo fuzzy-matches milk`() = runTest {
        coEvery { dao.searchByPrefix("mlk".take(3)) } returns listOf(milkIngredient)

        val result = analyzeUseCase.execute("INGREDIENTS: mlk, butter")
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
    }

    @Test
    fun `noisy OCR with garbage characters does not crash`() = runTest {
        val noisyText = "!@#%^INGREDIENTS: @@milk&&, waterXX%%"

        // Shouldn't throw
        val result = analyzeUseCase.execute(noisyText)
        // Can't assert status definitively — just verify it completes without crash
        assertTrue(
            result.overallStatus == VeganStatus.VEGAN ||
                result.overallStatus == VeganStatus.NOT_VEGAN ||
                result.overallStatus == VeganStatus.UNCERTAIN
        )
    }

    @Test
    fun `label with only nutrition info returns VEGAN (no ingredients found)`() = runTest {
        // Nutrition block should be trimmed; nothing left to parse
        val result = analyzeUseCase.execute(
            "INGREDIENTS: water. NUTRITION: Per 100ml - Energy 42kJ, Protein 0g."
        )
        // Nutrition content not tokenized; only "water" is present, matches nothing → VEGAN
        assertEquals(VeganStatus.VEGAN, result.overallStatus)
    }

    @Test
    fun `realistic vegan energy bar with many ingredients returns VEGAN`() = runTest {
        coEvery { dao.findByNormalizedName("sugar") } returns sugarIngredient
        coEvery { dao.findByNormalizedName("salt") } returns saltIngredient
        coEvery { dao.findByNormalizedName("water") } returns waterIngredient

        val label = """
            INGREDIENTS: Dates (40%), Almonds (20%), Cashews (15%), Oats, Brown Rice Syrup,
            Cocoa Powder, Sunflower Seeds, Chia Seeds, Salt, Natural Vanilla Extract.
            STORAGE: Store in a cool dry place. BEST BEFORE: See packaging.
        """.trimIndent()

        val result = analyzeUseCase.execute(label)
        assertEquals(VeganStatus.VEGAN, result.overallStatus)
        assertTrue(result.flaggedIngredients.isEmpty())
    }

    @Test
    fun `realistic chocolate with milk returns NOT_VEGAN and trims nutrition`() = runTest {
        coEvery { dao.findByNormalizedName("sugar") } returns sugarIngredient
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val label = """
            INGREDIENTS: Sugar, Cocoa Butter, Cocoa Mass, Skimmed Milk Powder.
            CONTAINS: Milk, Soy.
            NUTRITION: Per 100g Energy 560kcal, Fat 33g.
        """.trimIndent()

        val result = analyzeUseCase.execute(label)
        assertEquals(VeganStatus.NOT_VEGAN, result.overallStatus)
        // Nutrition section must not appear as tokens
        assertTrue(result.parsedTokens.none { "kcal" in it || "560" in it })
    }

    // ── Deduplication ─────────────────────────────────────────────────────────

    @Test
    fun `milk mentioned in both ingredients and contains is not double-counted in findings`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        val result = analyzeUseCase.execute(
            "INGREDIENTS: organic a2 milk, vitamin d3. CONTAINS: MILK."
        )
        // distinctBy normalizedName ensures only one milk finding
        assertEquals(1, result.allIngredients.count { it.ingredient.normalizedName == "milk" })
    }
}
