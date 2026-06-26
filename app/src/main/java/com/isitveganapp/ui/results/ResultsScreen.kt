package com.isitveganapp.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import com.isitveganapp.data.model.VeganStatus
import com.isitveganapp.domain.model.AnalysisResult
import com.isitveganapp.domain.model.IngredientFinding
import com.isitveganapp.ui.theme.AmberContainer
import com.isitveganapp.ui.theme.Amber40
import com.isitveganapp.ui.theme.GreenContainer
import com.isitveganapp.ui.theme.Green40
import com.isitveganapp.ui.theme.Red40
import com.isitveganapp.ui.theme.RedContainer

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ResultsScreen(
    result: AnalysisResult,
    onScanAgain: () -> Unit,
    onFeedback: (isCorrect: Boolean) -> Unit = {}
) {
    val (bannerColor, bannerText, bannerIcon) = when (result.overallStatus) {
        VeganStatus.VEGAN -> Triple(Green40, "Vegan", Icons.Default.CheckCircle)
        VeganStatus.NOT_VEGAN -> Triple(Red40, "Not Vegan", Icons.Default.Error)
        VeganStatus.UNCERTAIN -> Triple(Amber40, "Uncertain — Check Ingredients", Icons.Default.Warning)
    }

    var showRawText by remember { mutableStateOf(false) }
    var feedbackGiven by remember { mutableStateOf<Boolean?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Results") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bannerColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Verdict banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bannerColor)
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = bannerIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = bannerText,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Feedback row — only when there are actual scanned tokens to give feedback on
            if (result.parsedTokens.isNotEmpty()) item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (feedbackGiven == null) {
                        Text(
                            text = "Was this result correct?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            IconButton(onClick = {
                                feedbackGiven = true
                                onFeedback(true)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp,
                                    contentDescription = "Correct",
                                    tint = Green40,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            IconButton(onClick = {
                                feedbackGiven = false
                                onFeedback(false)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ThumbDown,
                                    contentDescription = "Incorrect",
                                    tint = Red40,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Thanks for your feedback!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Flagged ingredients section
            if (result.flaggedIngredients.isNotEmpty()) {
                item {
                    Text(
                        text = "Flagged Ingredients (${result.flaggedIngredients.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(result.flaggedIngredients) { finding ->
                    IngredientFindingCard(
                        finding = finding,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // No flags message — show when OCR found tokens but none are non-vegan
            if (result.flaggedIngredients.isEmpty() && result.parsedTokens.isNotEmpty()) {
                item {
                    Text(
                        text = "No non-vegan ingredients detected.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green40,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // All scanned ingredients — DB-matched ones as colored chips, unmatched as plain chips
            if (result.parsedTokens.isNotEmpty()) {
                val matchedNames = result.allIngredients.map { it.rawText }.toSet()
                val unmatchedTokens = result.parsedTokens.filter { it !in matchedNames }
                item {
                    Text(
                        text = "Scanned Ingredients (${result.parsedTokens.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        result.allIngredients.forEach { finding ->
                            IngredientChip(finding)
                        }
                        unmatchedTokens.forEach { token ->
                            FilterChip(
                                selected = false,
                                onClick = {},
                                label = { Text(token) }
                            )
                        }
                    }
                }
            }

            // OCR found nothing at all
            if (result.parsedTokens.isEmpty()) {
                item {
                    Text(
                        text = if (result.rawText.isBlank())
                            "No text could be read. Try again with better lighting or move closer to the label."
                        else
                            "Text was read but no ingredient tokens could be parsed. Try pointing at the ingredients list specifically.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Raw OCR text (collapsible)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showRawText = !showRawText },
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(if (showRawText) "Hide Raw OCR Text" else "Show Raw OCR Text")
                }
                if (showRawText) {
                    Text(
                        text = result.rawText.ifBlank { "(no text recognized)" },
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .fillMaxWidth()
                    )
                }
            }

            // Scan again
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onScanAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Scan Another Product")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun IngredientFindingCard(finding: IngredientFinding, modifier: Modifier = Modifier) {
    val containerColor = when (finding.veganStatus) {
        VeganStatus.NOT_VEGAN -> RedContainer
        VeganStatus.UNCERTAIN -> AmberContainer
        VeganStatus.VEGAN -> GreenContainer
    }
    val accentColor = when (finding.veganStatus) {
        VeganStatus.NOT_VEGAN -> Red40
        VeganStatus.UNCERTAIN -> Amber40
        VeganStatus.VEGAN -> Green40
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val scannedLabel = finding.rawText
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase(Locale.getDefault()) } }
            Text(
                text = scannedLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )
            if (!finding.rawText.equals(finding.ingredient.normalizedName, ignoreCase = true)) {
                Text(
                    text = "a.k.a. ${finding.ingredient.displayName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = finding.ingredient.reason,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = finding.ingredient.category,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun IngredientChip(finding: IngredientFinding) {
    val chipColor = when (finding.veganStatus) {
        VeganStatus.NOT_VEGAN -> Red40
        VeganStatus.UNCERTAIN -> Amber40
        VeganStatus.VEGAN -> Green40
    }
    FilterChip(
        selected = finding.veganStatus != VeganStatus.VEGAN,
        onClick = {},
        label = {
            Text(
                finding.rawText.split(" ")
                    .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase(Locale.getDefault()) } }
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = chipColor.copy(alpha = 0.15f),
            selectedLabelColor = chipColor
        )
    )
}
