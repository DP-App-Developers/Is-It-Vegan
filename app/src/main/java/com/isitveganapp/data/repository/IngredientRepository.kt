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
        return null
    }

    fun normalize(raw: String): String =
        raw.lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}
