package com.isitveganapp.di

import android.content.Context
import androidx.room.Room
import com.isitveganapp.data.local.IngredientDao
import com.isitveganapp.data.local.IngredientDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): IngredientDatabase =
        Room.databaseBuilder(context, IngredientDatabase::class.java, "ingredients.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideDao(db: IngredientDatabase): IngredientDao = db.ingredientDao()
}
