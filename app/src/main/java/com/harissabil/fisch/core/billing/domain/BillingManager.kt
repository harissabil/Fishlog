package com.harissabil.fisch.core.billing.domain

import android.app.Activity
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.flow.StateFlow

const val PLUS_MONTHLY_PRODUCT_ID = "fisch_plus_monthly"

interface BillingManager {
    val isPlus: StateFlow<Boolean>
    val planProductDetails: StateFlow<ProductDetails?>

    suspend fun refreshEntitlement()

    fun clearLocalEntitlement()

    fun launchPurchaseFlow(activity: Activity)
}
