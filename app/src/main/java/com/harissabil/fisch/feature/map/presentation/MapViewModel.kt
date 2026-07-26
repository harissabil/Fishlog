package com.harissabil.fisch.feature.map.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
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
import kotlinx.coroutines.flow.Flow
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
import java.util.Date
import javax.inject.Inject
import kotlin.random.Random

private const val USE_DUMMY_DATA = false

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getMaps: GetMaps,
    private val getLogbooks: GetLogbooks,
    private val locationTracker: LocationTracker,
) : ViewModel() {

    private val _mapItems = MutableStateFlow<List<MapItem>?>(null)
    private val _logbooks = MutableStateFlow<List<Logbook>?>(null)

    /** Pre-built pins used when [USE_DUMMY_DATA] is on; bypasses the Firestore join below. */
    private val _dummyPins = MutableStateFlow<List<MapPin>?>(null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val pins: Flow<List<MapPin>> = combine(
        _mapItems,
        _logbooks,
        _dummyPins,
    ) { mapItems, logbooks, dummyPins ->
        if (dummyPins != null) return@combine dummyPins

        val logbooksById = logbooks?.associateBy { it.id } ?: emptyMap()

        mapItems
            ?.mapNotNull { mapItem ->
                val logbook = mapItem.logbookRef?.id?.let { logbooksById[it] }
                if (logbook == null) null else MapPin(mapItem = mapItem, logbook = logbook)
            }
            ?: emptyList()
    }

    val availableBaits = pins.map { pins ->
        pins
            .mapNotNull { it.logbook.umpan?.trim()?.takeIf { bait -> bait.isNotEmpty() } }
            .distinct()
            .sorted()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val filteredPins: StateFlow<List<MapPin>> = combine(
        pins,
        _searchQuery,
        _filterState,
    ) { pins, query, filterState ->
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
        if (USE_DUMMY_DATA) {
            generateDummyPins()
        } else {
            getMaps()
            getLogbooks()
        }
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
    }

    /**
     * Builds [MapPin]s straight from generated data. A pin needs both a [MapItem] and a [Logbook],
     * so the logbook is synthesised here too rather than joined against Firestore.
     */
    private fun generateDummyPins() {
        val random = Random(seed = 0)

        val species = listOf(
            "Barramundi", "Grouper", "Snapper", "Threadfin", "Queenfish",
            "Trevally", "Catfish", "Tilapia", "Mangrove Jack", "Milkfish",
        )
        val baits = listOf("Shrimp", "Squid", "Lure", "Worm", "Prawn", "Live Bait")

        val pins = (0..69).map { i ->
            val placeName = "Place $i"
            val latLong = LatLng(
                1.35 + random.nextDouble(),
                103.87 + random.nextDouble(),
            )
            val caughtAt = Timestamp(
                Date(System.currentTimeMillis() - random.nextLong(0, 90L * 24 * 60 * 60 * 1000))
            )

            MapPin(
                mapItem = MapItem(
                    id = i.toString(),
                    logbookRef = null,
                    placeName = placeName,
                    latLong = latLong,
                ),
                logbook = Logbook(
                    id = "dummy-$i",
                    email = null,
                    jenisIkan = species[i % species.size],
                    jumlahIkan = random.nextInt(1, 6),
                    waktuPenangkapan = caughtAt,
                    tempatPenangkapan = placeName,
                    fotoIkan = null,
                    beratIkan = random.nextDouble(0.2, 8.0),
                    panjangIkan = random.nextDouble(10.0, 90.0),
                    umpan = baits[i % baits.size],
                    dilepaskan = i % 3 == 0,
                    catatan = "Dummy catch #$i",
                ),
            )
        }

        _dummyPins.update { pins }
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
