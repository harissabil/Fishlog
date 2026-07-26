package com.harissabil.fisch.feature.home.presentation.component

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MonitorWeight
import androidx.compose.material.icons.outlined.Recycling
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.harissabil.fisch.R
import com.harissabil.fisch.core.common.component.FishLoading
import com.harissabil.fisch.core.common.theme.FischTheme
import com.harissabil.fisch.core.common.theme.spacing
import com.harissabil.fisch.core.firebase.firestore.domain.model.Logbook
import java.util.Calendar
import kotlin.math.roundToInt

enum class TimeRange(val label: String) {
    THIS_WEEK("This week"),
    THIS_MONTH("This month"),
    THIS_YEAR("This year"),
    ALL_TIME("All time"),
}

private fun List<Logbook?>?.filterByRange(range: TimeRange): List<Logbook?>? {
    if (range == TimeRange.ALL_TIME) return this

    val cutoff = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (range == TimeRange.THIS_WEEK) {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        } else if (range == TimeRange.THIS_MONTH) {
            set(Calendar.DAY_OF_MONTH, 1)
        } else if (range == TimeRange.THIS_YEAR) {
            set(Calendar.DAY_OF_YEAR, 1)
        }
    }.time

    return this?.filter { logbook ->
        val caughtAt = logbook?.waktuPenangkapan?.toDate()
        caughtAt != null && !caughtAt.before(cutoff)
    }
}

data class LogbookStats(
    val totalCatches: Int,
    val speciesCount: Int,
    val releasedCount: Int,
    val bestWeight: Double?,
) {
    companion object {
        fun from(logbooks: List<Logbook?>?): LogbookStats {
            val nonNullLogbooks = logbooks?.filterNotNull() ?: emptyList()
            return LogbookStats(
                totalCatches = nonNullLogbooks.mapNotNull { it.jumlahIkan }.sum(),
                speciesCount = nonNullLogbooks.mapNotNull { it.jenisIkan }.distinct().size,
                releasedCount = nonNullLogbooks.count { it.dilepaskan == true },
                bestWeight = nonNullLogbooks.mapNotNull { it.beratIkan }.maxOrNull(),
            )
        }
    }
}

/**
 * Each tile takes on one of the theme's accent hues so the row reads like a set of
 * distinct tackle-box compartments rather than one flat panel.
 */
private enum class StatAccent { PRIMARY, SECONDARY, TERTIARY }

@Composable
private fun StatAccent.containerColor(): Color = when (this) {
    StatAccent.PRIMARY -> MaterialTheme.colorScheme.primaryContainer
    StatAccent.SECONDARY -> MaterialTheme.colorScheme.secondaryContainer
    StatAccent.TERTIARY -> MaterialTheme.colorScheme.tertiaryContainer
}

@Composable
private fun StatAccent.onContainerColor(): Color = when (this) {
    StatAccent.PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
    StatAccent.SECONDARY -> MaterialTheme.colorScheme.onSecondaryContainer
    StatAccent.TERTIARY -> MaterialTheme.colorScheme.onTertiaryContainer
}

private data class StatSpec(
    val icon: @Composable () -> Unit,
    val value: String,
    val label: String,
    val accent: StatAccent,
)

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeStats(
    modifier: Modifier = Modifier,
    logbooks: List<Logbook?>?,
    isLoading: Boolean = false,
) {
    var selectedRange by rememberSaveable { mutableStateOf(TimeRange.THIS_WEEK) }
    val stats = LogbookStats.from(logbooks.filterByRange(selectedRange))
    val gap = MaterialTheme.spacing.small

    val specs = listOf(
        StatSpec(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.SetMeal,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            value = stats.totalCatches.toString(),
            label = "Catches",
            accent = StatAccent.PRIMARY
        ),
        StatSpec(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_fishlog),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            value = stats.speciesCount.toString(),
            label = "Species",
            accent = StatAccent.TERTIARY
        ),
        StatSpec(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Recycling,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            value = stats.releasedCount.toString(),
            label = "Released",
            accent = StatAccent.SECONDARY
        ),
        StatSpec(
            icon = {
                Icon(
                    imageVector = Icons.Outlined.MonitorWeight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            value = stats.bestWeight?.let { "${roundToDisplay(it)} kg" } ?: "–",
            label = "Best catch",
            accent = StatAccent.PRIMARY
        ),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
        ) {
            TimeRange.entries.forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { selectedRange = range },
                    label = { Text(text = range.label) }
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val columns = if (maxWidth < 360.dp) 2 else 4
            val tileWidth = (maxWidth - gap * (columns - 1)) / columns

            Box {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalArrangement = Arrangement.spacedBy(gap),
                    maxItemsInEachRow = columns,
                ) {
                    specs.forEach { spec ->
                        StatTile(
                            modifier = Modifier.width(tileWidth),
                            icon = spec.icon,
                            value = if (isLoading) "–" else spec.value,
                            label = spec.label,
                            accent = spec.accent,
                        )
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        FishLoading(
                            circleSize = 8.dp,
                            spaceBetween = 6.dp,
                            travelDistance = 16.dp,
                        )
                    }
                }
            }
        }
    }
}

private fun roundToDisplay(value: Double): String {
    return if (value == value.roundToInt().toDouble()) {
        value.roundToInt().toString()
    } else {
        value.toString()
    }
}

@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    accent: StatAccent,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(
                PaddingValues(
                    horizontal = MaterialTheme.spacing.extraSmall,
                    vertical = MaterialTheme.spacing.small,
                )
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accent.containerColor()),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalContentColor provides accent.onContainerColor()) {
                    icon()
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeStatsPreview() {
    FischTheme {
        Surface {
            Box(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                HomeStats(
                    logbooks = listOf(
                        Logbook(
                            id = "1",
                            email = "tes@gmail.com",
                            jenisIkan = "Ikan Hiu",
                            jumlahIkan = 1000,
                            tempatPenangkapan = "Florida",
                            waktuPenangkapan = null,
                            fotoIkan = null,
                            catatan = null,
                            beratIkan = 12.5,
                            dilepaskan = true
                        ),
                        Logbook(
                            id = "2",
                            email = "tes@gmail.com",
                            jenisIkan = "Gurita",
                            jumlahIkan = 5,
                            tempatPenangkapan = "Laut Pasifik",
                            waktuPenangkapan = null,
                            fotoIkan = null,
                            catatan = null,
                            beratIkan = 10003.2,
                            dilepaskan = false
                        ),
                    ),
                    isLoading = false
                )
            }
        }
    }
}

@Preview(name = "Narrow (320dp)", widthDp = 320, showBackground = true)
@Composable
private fun HomeStatsNarrowPreview() {
    FischTheme {
        Surface {
            Box(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
                HomeStats(
                    logbooks = listOf(
                        Logbook(
                            id = "1",
                            email = "tes@gmail.com",
                            jenisIkan = "Ikan Hiu",
                            jumlahIkan = 10,
                            tempatPenangkapan = "Florida",
                            waktuPenangkapan = null,
                            fotoIkan = null,
                            catatan = null,
                            beratIkan = 12.5,
                            dilepaskan = true
                        ),
                    ),
                    isLoading = false
                )
            }
        }
    }
}
