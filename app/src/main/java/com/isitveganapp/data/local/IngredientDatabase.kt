package com.isitveganapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.isitveganapp.data.model.Ingredient

@Database(entities = [Ingredient::class], version = 1, exportSchema = false)
@TypeConverters(VeganStatusConverter::class)
abstract class IngredientDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
}
