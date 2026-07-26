package com.harissabil.fisch.feature.map.presentation

import com.harissabil.fisch.feature.logbook.catches.FilterState

sealed class MapEvent {

    data class UpdateSearchQuery(val searchQuery: String) : MapEvent()

    data object FilterIconClick : MapEvent()

    data class FilterMaps(val filterState: FilterState) : MapEvent()

    data object RecenterToMyLocation : MapEvent()
}
