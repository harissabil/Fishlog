package com.harissabil.fisch.feature.logbook.add_catch.presentation

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import androidx.activity.result.IntentSenderRequest

sealed class AddCatchEvent {

    data object SaveIntroShown : AddCatchEvent()

    data class SetFishImage(val imageBitmaps: Bitmap?) : AddCatchEvent()

    data class SetFishType(val fishType: String) : AddCatchEvent()

    data object IdentifyFishType : AddCatchEvent()

    data class SetFishQuantity(val fishQuantity: String) : AddCatchEvent()

    data class SetFishWeight(val fishWeight: String) : AddCatchEvent()

    data class SetFishLength(val fishLength: String) : AddCatchEvent()

    data class SetBait(val bait: String) : AddCatchEvent()

    data class SetIsReleased(val isReleased: Boolean) : AddCatchEvent()

    data class SetCaptureDate(val captureDate: String) : AddCatchEvent()

    data class SetCaptureTime(val captureTime: String) : AddCatchEvent()

    data class SetCaptureLocation(val captureLocation: String) : AddCatchEvent()

    data class SetCurrentLocation(val isCurrentLocation: Boolean, val context: Context) :
        AddCatchEvent()

    data class EnableLocationRequest(
        val context: Context,
        val makeRequest: (intentSenderRequest: IntentSenderRequest) -> Unit,
    ) : AddCatchEvent()

    data class SetNotes(val notes: String) : AddCatchEvent()

    data class UploadCatchData(val context: Context) : AddCatchEvent()

    data object DismissPaywall : AddCatchEvent()

    data class SubscribeToPlus(val activity: Activity) : AddCatchEvent()
}