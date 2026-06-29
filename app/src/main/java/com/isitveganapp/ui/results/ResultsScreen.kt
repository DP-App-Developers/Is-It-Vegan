package com.isitveganapp.ui.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import com.isitveganapp.data.model.VeganStatus
import com.isitveganapp.domain.model.AnalysisResult
import com.isitveganapp.domain.model.IngredientFinding
import com.isitveganapp.ui.theme.Brand700
import com.isitveganapp.ui.theme.DangerRed
import com.isitveganapp.ui.theme.DangerRedSurface
import com.isitveganapp.ui.theme.Gray500
import com.isitveganapp.ui.theme.Gray700
import com.isitveganapp.ui.theme.VeganGreen
import com.isitveganapp.ui.theme.VeganGreenSurface
import com.isitveganapp.ui.theme.WarnAmber
import com.isitveganapp.ui.theme.WarnAmberSurface

@Composable
fun ResultsScreen(
    result: AnalysisResult,
    onScanAgain: () -> Unit,
    onFeedback: (isCorrect: Boolean) -> Unit = {}
) {
    val (heroTop, heroBottom, heroIcon, heroLabel, heroSub) = when (result.overallStatus) {
        VeganStatus.VEGAN -> ResultsHero(
            top    = Color(0xFF1A7A3C),
            bottom = Color(0xFF2DA055),
            icon   = Icons.Default.CheckCircle,
            label  = "Vegan",
            sub    = "No animal-derived ingredients detected"
        )
        VeganStatus.NOT_VEGAN -> ResultsHero(
            top    = Color(0xFFA93226),
            bottom = Color(0xFFC0392B),
            icon   = Icons.Default.Error,
            label  = "Not Vegan",
            sub    = "Contains animal-derived ingredients"
        )
        VeganStatus.UNCERTAIN -> ResultsHero(
            top    = Color(0xFFB7770D),
            bottom = Color(0xFFD97706),
            icon   = Icons.Default.Warning,
            label  = "Uncertain",
            sub    = "Some ingredients need manual verification"
        )
    }

    var showRawText by remember { mutableStateOf(false) }
    var feedbackGiven by remember { mutableStateOf<Boolean?>(null) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Hero ──────────────────────────────────────────────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(heroTop, heroBottom)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(top = 36.dp, bottom = 52.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = heroIcon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = heroLabel,
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = heroSub,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // ── Flagged ingredients ───────────────────────────────────────────
            if (result.flaggedIngredients.isNotEmpty()) {
                item { SectionHeader("Issues Found") }
                items(result.flaggedIngredients) { finding ->
                    IngredientFindingCard(
                        finding = finding,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                    )
                }
            }

            // ── Clean result message ──────────────────────────────────────────
            if (result.flaggedIngredients.isEmpty() && result.parsedTokens.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(VeganGreenSurface)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = VeganGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "No non-vegan ingredients detected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = VeganGreen,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── OCR empty state ───────────────────────────────────────────────
            if (result.parsedTokens.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Couldn't read ingredients",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (result.rawText.isBlank())
                                "No text was detected. Try again with better lighting or move the camera closer to the label."
                            else
                                "Text was detected but no ingredient list could be parsed. Point the camera directly at the ingredients section.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ── Feedback ──────────────────────────────────────────────────────
            if (result.parsedTokens.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (feedbackGiven == null) {
                            Text(
                                text = "Was this result correct?",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(
                                onClick = { feedbackGiven = true; onFeedback(true) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp,
                                    contentDescription = "Correct",
                                    tint = VeganGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(
                                onClick = { feedbackGiven = false; onFeedback(false) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbDown,
                                    contentDescription = "Incorrect",
                                    tint = DangerRed,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Text(
                                text = "Thanks for your feedback!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }
                    }
                }
            }

            // ── Raw OCR toggle ────────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    TextButton(onClick = { showRawText = !showRawText }) {
                        Text(
                            text = if (showRawText) "Hide Raw OCR Text" else "Show Raw OCR Text",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gray500
                        )
                    }
                    if (showRawText) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = result.rawText.ifBlank { "(no text recognized)" },
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = Gray700,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                        )
                    }
                }
            }

            // ── Scan Again CTA ────────────────────────────────────────────────
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onScanAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Brand700)
                ) {
                    Text(
                        text = "Scan Another Product",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ── Supporting composables ────────────────────────────────────────────────────

private data class ResultsHero(
    val top: Color,
    val bottom: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val sub: String
)

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.labelMedium,
        color = Gray500,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp)
    )
}

@Composable
private fun IngredientFindingCard(finding: IngredientFinding, modifier: Modifier = Modifier) {
    val (accentColor, surfaceColor) = when (finding.veganStatus) {
        VeganStatus.NOT_VEGAN -> DangerRed to DangerRedSurface
        VeganStatus.UNCERTAIN -> WarnAmber to WarnAmberSurface
        VeganStatus.VEGAN     -> VeganGreen to VeganGreenSurface
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .height(IntrinsicSize.Min)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(accentColor)
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            val label = finding.rawText
                .split(" ")
                .joinToString(" ") { it.replaceFirstChar { c -> c.titlecase(Locale.getDefault()) } }
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = accentColor
            )
            if (!finding.rawText.equals(finding.ingredient.normalizedName, ignoreCase = true)) {
                Text(
                    text = "a.k.a. ${finding.ingredient.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = finding.ingredient.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray700
            )
        }
    }
}
