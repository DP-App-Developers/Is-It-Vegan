package com.isitveganapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.isitveganapp.data.model.Ingredient

@Dao
interface IngredientDao {

    @Query("SELECT * FROM ingredients WHERE normalized_name = :name LIMIT 1")
    suspend fun findByNormalizedName(name: String): Ingredient?

    @Query("SELECT * FROM ingredients WHERE ('|' || aliases || '|') LIKE ('%|' || :alias || '|%') LIMIT 1")
    suspend fun findByAlias(alias: String): Ingredient?

    @Query("SELECT * FROM ingredients WHERE normalized_name LIKE :prefix || '%' LIMIT 10")
    suspend fun searchByPrefix(prefix: String): List<Ingredient>

    @Query("SELECT COUNT(*) FROM ingredients")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(ingredients: List<Ingredient>)

    @Query("DELETE FROM ingredients")
    suspend fun deleteAll()
}
