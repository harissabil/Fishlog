package com.harissabil.fisch.feature.map.domain

import com.google.maps.android.clustering.ClusterItem
import com.harissabil.fisch.core.firebase.firestore.domain.model.Logbook

data class MapPin(
    val mapItem: MapItem,
    val logbook: Logbook,
) : ClusterItem by mapItem
