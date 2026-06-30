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

    private fun ingredient(name: String, status: VeganStatus, aliases: String = "") =
        Ingredient(id = 0, displayName = name, normalizedName = name, aliases = aliases, veganStatus = status, reason = "")

    private val milkIngredient = ingredient("milk", VeganStatus.NOT_VEGAN)
    private val sugarIngredient = ingredient("sugar", VeganStatus.VEGAN)

    @Before
    fun setUp() {
        dao = mockk()
        seeder = mockk(relaxed = true)
        repo = IngredientRepository(dao, seeder)
        coEvery { dao.findByNormalizedName(any()) } returns null
        coEvery { dao.findByAlias(any()) } returns null
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
    fun `lookup normalizes input before matching`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        assertNotNull(repo.lookup("MILK"))
        assertNotNull(repo.lookup("  milk  "))
        assertNotNull(repo.lookup("Milk!"))
    }

    @Test
    fun `lookup returns null for unknown ingredient`() = runTest {
        assertNull(repo.lookup("quinoa"))
    }

    @Test
    fun `lookup returns null for very short input`() = runTest {
        assertNull(repo.lookup("a"))
        assertNull(repo.lookup(" "))
    }

    @Test
    fun `lookup does not match partial words`() = runTest {
        coEvery { dao.findByNormalizedName("eggs") } returns ingredient("eggs", VeganStatus.NOT_VEGAN)

        // "eg" is not "eggs" — no fuzzy matching
        assertNull(repo.lookup("eg"))
    }

    @Test
    fun `lookup does not match typos`() = runTest {
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        // "mlk" is not "milk" — no fuzzy matching
        assertNull(repo.lookup("mlk"))
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

    @Test
    fun `lookup prefers exact name match over alias`() = runTest {
        val byName = ingredient("sugar", VeganStatus.VEGAN)
        val byAlias = ingredient("other", VeganStatus.UNCERTAIN)
        coEvery { dao.findByNormalizedName("sugar") } returns byName
        coEvery { dao.findByAlias("sugar") } returns byAlias

        assertEquals(VeganStatus.VEGAN, repo.lookup("sugar")!!.veganStatus)
    }

    @Test
    fun `lookup returns null when multi-word phrase has no exact match`() = runTest {
        // N-gram scanning generates "milk" separately; the repo does not split phrases
        coEvery { dao.findByNormalizedName("milk") } returns milkIngredient

        assertNull(repo.lookup("whole milk solids"))
        assertNull(repo.lookup("soy milk"))
        assertNull(repo.lookup("organic a2 milk"))
    }
}
