package com.harissabil.fisch.feature.logbook.common.component

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.canopas.lib.showcase.IntroShowcase
import com.canopas.lib.showcase.IntroShowcaseScope
import com.harissabil.fisch.R
import com.harissabil.fisch.core.common.component.FishTextField
import com.harissabil.fisch.core.common.theme.FischTheme
import com.harissabil.fisch.core.common.theme.spacing
import com.harissabil.fisch.core.common.util.AnimatedTextFieldTrailingIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntroShowcaseScope.FishTypeTextFieldWithAi(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    supportingText: String?,
    onIdentifyFishType: () -> Unit,
    isIdentifying: Boolean,
    suggestions: List<String> = emptyList(),
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        ExposedDropdownMenuBox(
            modifier = Modifier.weight(1f),
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
                label = "Fish Type",
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fishlog),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
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
                isError = isError,
                supportingText = {
                    AnimatedVisibility(visible = isError) {
                        supportingText?.let {
                            Column {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small + MaterialTheme.spacing.small))
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
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

//        if (isInEditMode) {
//            FloatingActionButton(
//                modifier = Modifier
//                    .padding(top = 8.dp)
//                    .introShowCaseTarget(
//                        index = 0,
//                        style = ShowcaseStyle.Default.copy(
//                            backgroundColor = MaterialTheme.colorScheme.tertiary,
//                            backgroundAlpha = 0.98f,
//                            targetCircleColor = MaterialTheme.colorScheme.inverseOnSurface
//                        ),
//                        content = {
//                            Column {
//                                Text(
//                                    text = "Identify Fish Type with AI",
//                                    color = MaterialTheme.colorScheme.onTertiary,
//                                    style = MaterialTheme.typography.headlineSmall,
//                                    fontWeight = FontWeight.SemiBold
//                                )
//                                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))
//                                Text(
//                                    text = "Caught something new? Identify its type with AI powered by Gemini. It might be a new species!",
//                                    color = MaterialTheme.colorScheme.onTertiary,
//                                    style = MaterialTheme.typography.bodyLarge
//                                )
//                            }
//                        }
//                    ),
//                onClick = {
//                    keyboardController?.hide()
//                    onIdentifyFishType()
//                },
//                containerColor = MaterialTheme.colorScheme.surface,
//                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 5.dp),
//            ) {
//                val rotation: Float
//                if (isIdentifying) {
//                    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
//                    rotation = infiniteTransition.animateValue(
//                        0f,
//                        1f,
//                        Float.VectorConverter,
//                        infiniteRepeatable(
//                            animation = tween(
//                                durationMillis = 2000,
//                                easing = LinearEasing
//                            )
//                        ), label = "rotation"
//                    ).value * 360
//                } else {
//                    rotation = 0f
//                }
//
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_gemini_ai),
//                    tint = Color.Unspecified,
//                    contentDescription = "Identify Fish Type with AI",
//                    modifier = Modifier.rotate(rotation)
//                )
//            }
//        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishTypeTextFieldWithAi(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    supportingText: String?,
    onIdentifyFishType: () -> Unit,
    isIdentifying: Boolean,
    suggestions: List<String> = emptyList(),
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
    ) {
        ExposedDropdownMenuBox(
            modifier = Modifier.weight(1f),
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
                label = "Fish Type",
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fishlog),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
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
                isError = isError,
                supportingText = {
                    AnimatedVisibility(visible = isError) {
                        supportingText?.let {
                            Column {
                                Text(
                                    text = it,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small + MaterialTheme.spacing.small))
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
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

//        if (isInEditMode) {
//            FloatingActionButton(
//                modifier = Modifier
//                    .padding(top = 8.dp),
//                onClick = {
//                    keyboardController?.hide()
//                    onIdentifyFishType()
//                },
//                containerColor = MaterialTheme.colorScheme.surface,
//                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 5.dp),
//            ) {
//                val rotation: Float
//                if (isIdentifying) {
//                    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
//                    rotation = infiniteTransition.animateValue(
//                        0f,
//                        1f,
//                        Float.VectorConverter,
//                        infiniteRepeatable(
//                            animation = tween(
//                                durationMillis = 2000,
//                                easing = LinearEasing
//                            )
//                        ), label = "rotation"
//                    ).value * 360
//                } else {
//                    rotation = 0f
//                }
//
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_gemini_ai),
//                    tint = Color.Unspecified,
//                    contentDescription = "Identify Fish Type with AI",
//                    modifier = Modifier.rotate(rotation)
//                )
//            }
//        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FishTypeTextFieldWithAiPreview() {
    FischTheme {
        Surface(
            color = MaterialTheme.colorScheme.surface
        ) {
            IntroShowcase(showIntroShowCase = false, onShowCaseCompleted = { /*TODO*/ }) {
                FishTypeTextFieldWithAi(
                    value = "",
                    onValueChange = {},
                    isError = true,
                    supportingText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
                    onIdentifyFishType = {},
                    isIdentifying = false,
                    suggestions = listOf("Nila", "Mujair", "Lele"),
                    isInEditMode = false
                )
            }
        }
    }
}
