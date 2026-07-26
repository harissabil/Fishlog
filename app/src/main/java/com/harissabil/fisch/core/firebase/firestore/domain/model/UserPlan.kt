package com.harissabil.fisch.core.firebase.firestore.domain.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

@Keep
data class UserPlan(
    @get:PropertyName("is_plus") @set:PropertyName("is_plus") var isPlus: Boolean = false,
    @get:PropertyName("plan_product_id")
    @set:PropertyName("plan_product_id")
    var planProductId: String? = null,
    @get:PropertyName("plan_expiry_at")
    @set:PropertyName("plan_expiry_at")
    var planExpiryAt: Timestamp? = null,
)
