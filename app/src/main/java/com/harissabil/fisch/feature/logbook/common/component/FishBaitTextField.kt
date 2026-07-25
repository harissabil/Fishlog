package com.harissabil.fisch.feature.logbook.common.component

import android.content.res.Configuration
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.SetMeal
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import com.harissabil.fisch.core.common.component.FishTextField
import com.harissabil.fisch.core.common.theme.FischTheme
import com.harissabil.fisch.core.common.util.AnimatedTextFieldTrailingIcon

/**
 * Free-text bait/lure field with an autocomplete dropdown suggesting values the user has
 * already logged before, so common baits (e.g. "Cacing", "Umpan Jitu", "Soft Plastic") don't
 * need to be retyped and stay consistent for later filtering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishBaitTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    isInEditMode: Boolean = true,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    var expanded by remember { mutableStateOf(false) }
    val filteredSuggestions = remember(value, suggestions) {
        suggestions.filter { it.contains(value, ignoreCase = true) && it != value }
    }
    val menuVisible = expanded && filteredSuggestions.isNotEmpty() && isInEditMode

    ExposedDropdownMenuBox(
        expanded = menuVisible,
        onExpandedChange = { if (isInEditMode) expanded = it },
    ) {
        FishTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = isInEditMode),
            interactionSource = interactionSource,
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = "Bait / Lure",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.SetMeal,
                    contentDescription = null,
                )
            },
            trailingIcon = {
                AnimatedTextFieldTrailingIcon(
                    visible = value.isNotEmpty() && isFocused,
                ) {
                    IconButton(
                        onClick = {
                            onValueChange("")
                            keyboardController?.hide()
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Outlined.Cancel,
                                contentDescription = "Clear",
                            )
                        }
                    )
                }
            },
            readOnly = !isInEditMode
        )

        ExposedDropdownMenu(
            expanded = menuVisible,
            onDismissRequest = { expanded = false },
        ) {
            filteredSuggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FishBaitTextFieldPreview() {
    FischTheme {
        Surface {
            FishBaitTextField(
                value = "Cac",
                onValueChange = {},
                suggestions = listOf("Cacing", "Udang", "Soft Plastic")
            )
        }
    }
}
