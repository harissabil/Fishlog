package com.harissabil.fisch.feature.map.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.harissabil.fisch.R
import com.harissabil.fisch.core.common.theme.spacing
import com.harissabil.fisch.feature.logbook.catches.component.FilterOptionsBottomSheet
import com.harissabil.fisch.feature.logbook.catches.component.FishSearchBar
import com.harissabil.fisch.feature.logbook.common.mapper.toToDetailState
import com.harissabil.fisch.feature.logbook.common.state.ToDetailState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

private const val MY_LOCATION_ZOOM = 15f
private const val BOUNDS_PADDING_PX = 150

@OptIn(MapsComposeExperimentalApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState,
    onNavigateToDetail: (ToDetailState) -> Unit,
) {
    val pins by viewModel.filteredPins.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilterState by viewModel.filterState.collectAsState()
    val availableBaits by viewModel.availableBaits.collectAsState()

    val scope = rememberCoroutineScope()

    val filterOptionsBottomSheetState = rememberModalBottomSheetState()
    var showFilterOptionsBottomSheet by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    val properties = MapProperties(
        mapStyleOptions = MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style),
        isMyLocationEnabled = true
    )
    val uiSettings = MapUiSettings(
        zoomControlsEnabled = false,
        myLocationButtonEnabled = false,
        mapToolbarEnabled = false
    )

    LaunchedEffect(key1 = Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is MapViewModel.UIEvent.ShowSnackbar -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(message = event.message)
                }

                is MapViewModel.UIEvent.CenterCamera -> {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(event.latLng, MY_LOCATION_ZOOM),
                        durationMs = 500
                    )
                }
            }
        }
    }

    LaunchedEffect(key1 = pins) {
        if (pins.isEmpty()) return@LaunchedEffect

        if (pins.size == 1) {
            Timber.d("Camera position: ${pins.first().mapItem.latLong}")
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(
                    pins.first().mapItem.latLong!!,
                    MY_LOCATION_ZOOM
                ),
                durationMs = 500
            )
            return@LaunchedEffect
        }

        val bounds = LatLngBounds.Builder().apply {
            pins.forEach { pin -> pin.mapItem.latLong?.let { include(it) } }
        }.build()

        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngBounds(bounds, BOUNDS_PADDING_PX),
            durationMs = 500
        )
    }

    if (showFilterOptionsBottomSheet) {
        FilterOptionsBottomSheet(
            onDismissRequest = { showFilterOptionsBottomSheet = false },
            sheetState = filterOptionsBottomSheetState,
            filterState = selectedFilterState,
            availableBaits = availableBaits,
            onApply = { filterState ->
                viewModel.onEvent(MapEvent.FilterMaps(filterState))
                scope.launch { filterOptionsBottomSheetState.hide() }.invokeOnCompletion {
                    if (!filterOptionsBottomSheetState.isVisible) {
                        showFilterOptionsBottomSheet = false
                    }
                }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = properties,
            uiSettings = uiSettings
        ) {
            Clustering(
                items = pins,
                onClusterItemClick = { pin ->
                    onNavigateToDetail(pin.logbook.toToDetailState(isInEditMode = false))
                    true
                },
            )
        }

        FishSearchBar(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.small
                ),
            query = searchQuery,
            onQueryChange = { viewModel.onEvent(MapEvent.UpdateSearchQuery(it)) },
            onFilter = { showFilterOptionsBottomSheet = true },
            isFilterActive = selectedFilterState.isActive,
        )

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(MaterialTheme.spacing.medium),
            onClick = { viewModel.onEvent(MapEvent.RecenterToMyLocation) },
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Center on my location"
            )
        }
    }
}
