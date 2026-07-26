package com.harissabil.fisch.feature.paywall.presentation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.harissabil.fisch.core.common.component.FishButton
import com.harissabil.fisch.core.common.component.FishTextButton
import com.harissabil.fisch.core.common.theme.FischTheme
import com.harissabil.fisch.core.common.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    sheetState: SheetState,
    trigger: PaywallTrigger = PaywallTrigger.GENERAL,
    priceLabel: String?,
    onSubscribeClick: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.medium)
                    .padding(bottom = MaterialTheme.spacing.medium + MaterialTheme.spacing.small)
                    .then(modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.WorkspacePremium,
                contentDescription = null,
                modifier = Modifier.height(48.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Text(
                text =
                    when (trigger) {
                        PaywallTrigger.QUOTA_EXCEEDED -> "You've reached your free monthly limit"
                        PaywallTrigger.GENERAL -> "Upgrade to Fishlog Plus"
                    },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            Text(
                text =
                    when (trigger) {
                        PaywallTrigger.QUOTA_EXCEEDED ->
                            "Free accounts can log 5 catches a month. Upgrade to Fishlog Plus for unlimited logging."
                        PaywallTrigger.GENERAL ->
                            "Free accounts can log up to 5 catches a month. Go unlimited with Fishlog Plus."
                    },
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small),
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            listOf(
                    "Unlimited catch logging",
                    "No monthly limits, ever",
                    "Support ongoing app development",
                )
                .forEach { benefit ->
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(vertical = MaterialTheme.spacing.extraSmall),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Text(text = benefit, style = MaterialTheme.typography.bodyMedium)
                    }
                }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            FishButton(
                modifier = Modifier.fillMaxWidth(),
                text =
                    if (priceLabel != null) "Upgrade for $priceLabel"
                    else "Upgrade to Fishlog Plus",
                onClick = onSubscribeClick,
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

            FishTextButton(text = "Not now", onClick = onDismissRequest)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaywallBottomSheetQuotaExceededPreview() {
    FischTheme {
        Surface {
            PaywallBottomSheet(
                onDismissRequest = {},
                sheetState = rememberModalBottomSheetState(),
                trigger = PaywallTrigger.QUOTA_EXCEEDED,
                priceLabel = "$1.99/month",
                onSubscribeClick = {},
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PaywallBottomSheetGeneralPreview() {
    FischTheme {
        Surface {
            PaywallBottomSheet(
                onDismissRequest = {},
                sheetState = rememberModalBottomSheetState(),
                trigger = PaywallTrigger.GENERAL,
                priceLabel = null,
                onSubscribeClick = {},
            )
        }
    }
}
