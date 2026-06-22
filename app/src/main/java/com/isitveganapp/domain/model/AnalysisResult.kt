package com.isitveganapp.domain.model

import android.os.Parcelable
import com.isitveganapp.data.model.Ingredient
import com.isitveganapp.data.model.VeganStatus
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnalysisResult(
    val overallStatus: VeganStatus,
    val flaggedIngredients: List<IngredientFinding>,
    val allIngredients: List<IngredientFinding>,
    val parsedTokens: List<String>,
    val rawText: String
) : Parcelable

@Parcelize
data class IngredientFinding(
    val rawText: String,
    val ingredient: Ingredient,
    val veganStatus: VeganStatus
) : Parcelable
