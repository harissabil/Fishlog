package com.harissabil.fisch.feature.logbook.catches.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.harissabil.fisch.core.common.component.FishButton
import com.harissabil.fisch.core.common.theme.spacing
import com.harissabil.fisch.feature.logbook.catches.FilterState
import com.harissabil.fisch.feature.logbook.catches.ReleaseFilter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterOptionsBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    filterState: FilterState,
    availableBaits: List<String>,
    onApply: (FilterState) -> Unit,
) {
    var localState by remember(filterState) { mutableStateOf(filterState) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(bottom = MaterialTheme.spacing.medium + MaterialTheme.spacing.small)
                .then(modifier),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(
                    onClick = { localState = FilterState() },
                    enabled = localState.isActive
                ) {
                    Text(text = "Reset")
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Text(
                text = "Status",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ReleaseFilter.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ReleaseFilter.entries.size
                        ),
                        selected = localState.releaseFilter == option,
                        onClick = { localState = localState.copy(releaseFilter = option) },
                        label = { Text(text = option.value) }
                    )
                }
            }

            if (availableBaits.isNotEmpty()) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                Text(
                    text = "Bait",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                ) {
                    availableBaits.forEach { bait ->
                        val selected = bait in localState.selectedBaits
                        FilterChip(
                            selected = selected,
                            onClick = {
                                localState = localState.copy(
                                    selectedBaits = if (selected) {
                                        localState.selectedBaits - bait
                                    } else {
                                        localState.selectedBaits + bait
                                    }
                                )
                            },
                            label = { Text(text = bait) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            FishButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Apply",
                onClick = { onApply(localState) }
            )
        }
    }
}
