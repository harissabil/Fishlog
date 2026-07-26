package com.harissabil.fisch.feature.map.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.harissabil.fisch.core.common.util.Resource
import com.harissabil.fisch.core.firebase.firestore.data.mapper.toLogbook
import com.harissabil.fisch.core.firebase.firestore.data.mapper.toMap
import com.harissabil.fisch.core.firebase.firestore.domain.model.Logbook
import com.harissabil.fisch.core.firebase.firestore.domain.usecase.GetLogbooks
import com.harissabil.fisch.core.firebase.firestore.domain.usecase.GetMaps
import com.harissabil.fisch.core.location.domain.LocationTracker
import com.harissabil.fisch.feature.logbook.catches.FilterState
import com.harissabil.fisch.feature.logbook.catches.ReleaseFilter
import com.harissabil.fisch.feature.map.data.toMapItem
import com.harissabil.fisch.feature.map.domain.MapItem
import com.harissabil.fisch.feature.map.domain.MapPin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getMaps: GetMaps,
    private val getLogbooks: GetLogbooks,
    private val locationTracker: LocationTracker,
) : ViewModel() {

    private val _mapItems = MutableStateFlow<List<MapItem>?>(null)
    private val _logbooks = MutableStateFlow<List<Logbook>?>(null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    val availableBaits = _logbooks.map { logbooks ->
        logbooks
            ?.mapNotNull { it.umpan?.trim()?.takeIf { bait -> bait.isNotEmpty() } }
            ?.distinct()
            ?.sorted()
            ?: emptyList()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val filteredPins: StateFlow<List<MapPin>> = combine(
        _mapItems,
        _logbooks,
        _searchQuery,
        _filterState,
    ) { mapItems, logbooks, query, filterState ->
        val logbooksById = logbooks?.associateBy { it.id } ?: emptyMap()

        val pins = mapItems
            ?.mapNotNull { mapItem ->
                val logbook = mapItem.logbookRef?.id?.let { logbooksById[it] }
                if (logbook == null) null else MapPin(mapItem = mapItem, logbook = logbook)
            }
            ?: emptyList()

        val queriedPins = if (query.isEmpty()) {
            pins
        } else {
            pins.filter { pin ->
                query in (pin.logbook.jenisIkan?.lowercase().orEmpty()) ||
                        query in (pin.logbook.tempatPenangkapan?.lowercase().orEmpty()) ||
                        query in (pin.logbook.umpan?.lowercase().orEmpty())
            }
        }

        queriedPins.filter { pin ->
            val matchesRelease = when (filterState.releaseFilter) {
                ReleaseFilter.ALL -> true
                ReleaseFilter.RELEASED -> pin.logbook.dilepaskan == true
                ReleaseFilter.KEPT -> pin.logbook.dilepaskan != true
            }
            val matchesBait = filterState.selectedBaits.isEmpty() ||
                    pin.logbook.umpan in filterState.selectedBaits

            matchesRelease && matchesBait
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow: SharedFlow<UIEvent> = _eventFlow.asSharedFlow()

    init {
        getMaps()
        getLogbooks()
    }

    fun onEvent(event: MapEvent) {
        when (event) {
            is MapEvent.UpdateSearchQuery -> updateSearchQuery(event.searchQuery)
            MapEvent.FilterIconClick -> Unit
            is MapEvent.FilterMaps -> filterMaps(event.filterState)
            MapEvent.RecenterToMyLocation -> recenterToMyLocation()
        }
    }

    private fun updateSearchQuery(newSearchQuery: String) {
        _searchQuery.update { newSearchQuery }
    }

    private fun filterMaps(filterState: FilterState) {
        _filterState.update { filterState }
    }

    private fun recenterToMyLocation() = viewModelScope.launch {
        val location = locationTracker.getCurrentLocation()
        if (location == null) {
            _eventFlow.emit(UIEvent.ShowSnackbar("Unable to get current location"))
            return@launch
        }
        _eventFlow.emit(UIEvent.CenterCamera(LatLng(location.latitude, location.longitude)))
    }

    private fun getMaps() = viewModelScope.launch {
        getMaps.invoke().collect { response ->
            when (response) {
                is Resource.Error -> {
                    _eventFlow.emit(
                        UIEvent.ShowSnackbar(
                            response.message ?: "Something went wrong!"
                        )
                    )
                }

                is Resource.Loading -> {}
                is Resource.Success -> {
                    Timber.d("Maps: ${response.data}")
                    _mapItems.update { response.data?.map { it.toMap().toMapItem() } }
                }
            }
        }
//        _maps.update {
//
//            fun getRandomPosition(): LatLng {
//                return LatLng(
//                    1.35 + Random.nextFloat(),
//                    103.87 + Random.nextFloat()
//                );
//            }
//
//            val placelist = mutableListOf<MapItem>()
//            for (i in 0..69) {
//                placelist.add(
//                    MapItem(
//                        id = i.toString(),
//                        logbookRef = null,
//                        placeName = "Place $i",
//                        latLong = getRandomPosition()
//                    )
//                )
//            }
//
//            placelist
//        }
    }

    private fun getLogbooks() = viewModelScope.launch {
        getLogbooks.invoke().collect { response ->
            when (response) {
                is Resource.Error -> {
                    _eventFlow.emit(
                        UIEvent.ShowSnackbar(
                            response.message ?: "Something went wrong!"
                        )
                    )
                }

                is Resource.Loading -> {}
                is Resource.Success -> {
                    _logbooks.update { response.data?.map { it.toLogbook() } }
                }
            }
        }
    }

    sealed class UIEvent {
        data class ShowSnackbar(val message: String) : UIEvent()
        data class CenterCamera(val latLng: LatLng) : UIEvent()
    }
}
