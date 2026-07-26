package com.harissabil.fisch.core.firebase.firestore.domain.model

object Constant {

    const val LOGBOOKS = "logbooks"
    const val MAPS = "maps"
    const val USERS = "users"
    const val COUNTERS = "counters"

    const val EMAIL = "email"

    const val FREE_MONTHLY_LOGBOOK_LIMIT = 5
    const val QUOTA_EXCEEDED_MESSAGE =
        "You've used all $FREE_MONTHLY_LOGBOOK_LIMIT free logs this month. Upgrade to Fishlog Plus for unlimited logging."
}