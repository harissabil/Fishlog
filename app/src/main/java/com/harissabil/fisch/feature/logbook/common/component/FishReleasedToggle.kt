package com.harissabil.fisch.feature.logbook.common.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.harissabil.fisch.core.common.theme.FischTheme
import com.harissabil.fisch.core.common.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishReleasedToggle(
    modifier: Modifier = Modifier,
    isReleased: Boolean,
    onValueChange: (Boolean) -> Unit,
    isInEditMode: Boolean = true,
) {
    val options = listOf("Kept", "Released")

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().then(modifier)) {
        options.forEachIndexed { index, label ->
            val selected = if (index == 0) !isReleased else isReleased
            SegmentedButton(
                enabled = isInEditMode,
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                selected = selected,
                onClick = { onValueChange(index == 1) },
                icon = {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier,
                        )
                    }
                },
                label = { Text(label) },
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FishReleasedTogglePreview() {
    FischTheme {
        Surface {
            FishReleasedToggle(
                modifier = Modifier.padding(MaterialTheme.spacing.medium),
                isReleased = false,
                onValueChange = {},
            )
        }
    }
}
