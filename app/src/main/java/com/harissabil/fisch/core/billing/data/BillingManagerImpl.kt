package com.harissabil.fisch.core.billing.data

import android.app.Activity
import android.app.Application
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.harissabil.fisch.core.billing.domain.BillingManager
import com.harissabil.fisch.core.billing.domain.PLUS_MONTHLY_PRODUCT_ID
import com.harissabil.fisch.core.firebase.firestore.domain.model.UserPlan
import com.harissabil.fisch.core.firebase.firestore.domain.usecase.UpdateUserPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManagerImpl @Inject constructor(
    application: Application,
    private val updateUserPlan: UpdateUserPlan,
) : BillingManager, PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isPlus = MutableStateFlow(false)
    override val isPlus: StateFlow<Boolean> = _isPlus.asStateFlow()

    private val _planProductDetails = MutableStateFlow<ProductDetails?>(null)
    override val planProductDetails: StateFlow<ProductDetails?> = _planProductDetails.asStateFlow()

    private val billingClient = BillingClient.newBuilder(application)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    init {
        scope.launch { refreshEntitlement() }
    }

    override suspend fun refreshEntitlement() {
        if (!ensureConnected()) return
        queryPlanProductDetails()
        queryActivePurchases()
    }

    override fun launchPurchaseFlow(activity: Activity) {
        val productDetails = _planProductDetails.value ?: return
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: return

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
            return
        }
        purchases?.forEach { handlePurchase(it) }
    }

    private suspend fun ensureConnected(): Boolean {
        if (billingClient.isReady) return true
        return suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    cont.resume(billingResult.responseCode == BillingClient.BillingResponseCode.OK)
                }

                override fun onBillingServiceDisconnected() {
                    if (cont.isActive) cont.resume(false)
                }
            })
        }
    }

    private suspend fun queryPlanProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PLUS_MONTHLY_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        val productDetailsList = suspendCancellableCoroutine { cont ->
            billingClient.queryProductDetailsAsync(params) { _, result ->
                cont.resume(result.productDetailsList)
            }
        }
        _planProductDetails.value = productDetailsList.firstOrNull()
    }

    private suspend fun queryActivePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        val purchases = suspendCancellableCoroutine { cont ->
            billingClient.queryPurchasesAsync(params) { _, result ->
                cont.resume(result)
            }
        }

        val activePurchase = purchases.firstOrNull {
            it.products.contains(PLUS_MONTHLY_PRODUCT_ID) &&
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
        }

        if (activePurchase != null) {
            handlePurchase(activePurchase)
        } else {
            _isPlus.value = false
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(ackParams) { }
        }

        _isPlus.value = true

        scope.launch {
            val result = updateUserPlan(
                UserPlan(
                    isPlus = true,
                    planProductId = purchase.products.firstOrNull(),
                )
            )
            Timber.d("UpdateUserPlan result: $result")
        }
    }
}
