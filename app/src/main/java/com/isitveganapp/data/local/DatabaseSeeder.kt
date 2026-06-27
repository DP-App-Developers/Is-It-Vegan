package com.isitveganapp.data.local

import android.content.Context
import android.content.SharedPreferences
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
    private val prefs: SharedPreferences =
        context.getSharedPreferences("db_seeder", Context.MODE_PRIVATE)

    suspend fun seedIfEmpty() {
        val json = context.assets.open("ingredients.json")
            .bufferedReader().readText()
        val jsonHash = json.hashCode().toString()

        if (prefs.getString("ingredients_hash", null) == jsonHash) return

        val items = Json.decodeFromString<List<IngredientJsonModel>>(json)
            .filter { it.veganStatus != "VEGAN" }

        dao.deleteAll()
        dao.insertAll(items.map { it.toEntity() })

        prefs.edit().putString("ingredients_hash", jsonHash).apply()
    }
}

@Serializable
data class IngredientJsonModel(
    val displayName: String,
    val normalizedName: String,
    val aliases: String,
    val veganStatus: String,
    val reason: String,
    val category: String = ""
) {
    fun toEntity() = Ingredient(
        displayName = displayName,
        normalizedName = normalizedName,
        aliases = aliases,
        veganStatus = VeganStatus.valueOf(veganStatus),
        reason = reason
    )
}
