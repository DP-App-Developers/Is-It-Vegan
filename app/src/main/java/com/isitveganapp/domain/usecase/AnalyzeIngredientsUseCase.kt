package com.isitveganapp.domain.usecase

import com.isitveganapp.data.model.VeganStatus
import com.isitveganapp.data.repository.IngredientRepository
import com.isitveganapp.domain.model.AnalysisResult
import com.isitveganapp.domain.model.IngredientFinding
import javax.inject.Inject

class AnalyzeIngredientsUseCase @Inject constructor(
    private val repository: IngredientRepository,
    private val parseUseCase: ParseIngredientTextUseCase
) {
    suspend fun execute(rawOcrText: String): AnalysisResult {
        val candidateNames = parseUseCase.execute(rawOcrText)

        val findings = candidateNames.mapNotNull { raw ->
            repository.lookup(raw)?.let { ingredient ->
                IngredientFinding(
                    rawText = raw,
                    ingredient = ingredient,
                    veganStatus = ingredient.veganStatus
                )
            }
        }.distinctBy { it.ingredient.normalizedName }

        val overallStatus = when {
            candidateNames.isEmpty() -> VeganStatus.UNCERTAIN
            findings.any { it.veganStatus == VeganStatus.NOT_VEGAN } -> VeganStatus.NOT_VEGAN
            findings.any { it.veganStatus == VeganStatus.UNCERTAIN } -> VeganStatus.UNCERTAIN
            findings.isEmpty() -> VeganStatus.UNCERTAIN
            else -> VeganStatus.VEGAN
        }

        return AnalysisResult(
            overallStatus = overallStatus,
            flaggedIngredients = findings.filter { it.veganStatus != VeganStatus.VEGAN },
            allIngredients = findings,
            parsedTokens = candidateNames,
            rawText = rawOcrText
        )
    }
}
