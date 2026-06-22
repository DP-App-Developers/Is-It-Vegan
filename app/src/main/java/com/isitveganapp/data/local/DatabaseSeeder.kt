package com.isitveganapp.data.local

import android.content.Context
import com.isitveganapp.data.model.Ingredient
import com.isitveganapp.data.model.VeganStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: IngredientDao
) {
    suspend fun seedIfEmpty() {
        val json = context.assets.open("ingredients.json")
            .bufferedReader().readText()
        val items = Json.decodeFromString<List<IngredientJsonModel>>(json)
            .filter { it.veganStatus != "VEGAN" }

        if (dao.count() == items.size) return  // already in sync

        dao.deleteAll()
        dao.insertAll(items.map { it.toEntity() })
    }
}

@Serializable
data class IngredientJsonModel(
    val displayName: String,
    val normalizedName: String,
    val aliases: String,
    val veganStatus: String,
    val reason: String,
    val category: String
) {
    fun toEntity() = Ingredient(
        displayName = displayName,
        normalizedName = normalizedName,
        aliases = aliases,
        veganStatus = VeganStatus.valueOf(veganStatus),
        reason = reason,
        category = category
    )
}
