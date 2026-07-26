package com.harissabil.fisch.feature.logbook.catch_detail

import android.graphics.Bitmap

data class CatchDetailState(
    val isInEditMode: Boolean = false,
    val showMoreOptionBottomSheet: Boolean = false,
    val imageBitmaps: Bitmap? = null,
    val id: String = "",
    val email: String = "",
    val fishUrl: String? = "",
    val fishType: String = "",
    val fishTypeError: String? = null,
    val isIdentifying: Boolean = false,
    val fishTypeSuggestions: List<String> = emptyList(),
    val fishQuantity: String = "",
    val fishQuantityError: String? = null,
    val fishWeight: String = "",
    val fishWeightError: String? = null,
    val fishLength: String = "",
    val fishLengthError: String? = null,
    val bait: String = "",
    val baitSuggestions: List<String> = emptyList(),
    val isReleased: Boolean = false,
    val captureDate: String = "",
    val captureTime: String = "",
    val captureLocation: String = "",
    val isCurrentLocation: Boolean = false,
    val notes: String = "",
    val isUploading: Boolean = false,
    val isUploaded: Boolean = false,
)
