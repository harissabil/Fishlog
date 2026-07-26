package com.harissabil.fisch.feature.profile.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harissabil.fisch.core.billing.domain.BillingManager
import com.harissabil.fisch.core.common.util.Resource
import com.harissabil.fisch.core.datastore.preference.domain.AiLanguage
import com.harissabil.fisch.core.datastore.preference.domain.Theme
import com.harissabil.fisch.core.datastore.preference.domain.usecase.AiLanguageUseCase
import com.harissabil.fisch.core.datastore.preference.domain.usecase.ThemeUseCase
import com.harissabil.fisch.core.firebase.auth.domain.usecase.GetSignedInUser
import com.harissabil.fisch.core.firebase.firestore.domain.usecase.GetLogbookCountThisMonth
import com.harissabil.fisch.core.firebase.firestore.domain.usecase.GetLogbooks
import com.harissabil.fisch.feature.profile.data.toUserData
import com.harissabil.fisch.feature.profile.domain.usecase.DeleteUserSignedIn
import com.harissabil.fisch.feature.profile.domain.usecase.ExportUserDataToZip
import com.harissabil.fisch.feature.profile.domain.usecase.SignOutUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val deleteUserSignedIn: DeleteUserSignedIn,
    private val getSignedInUser: GetSignedInUser,
    private val signOutUser: SignOutUser,
    private val themeUseCase: ThemeUseCase,
    private val aiLanguageUseCase: AiLanguageUseCase,
    private val getLogbooks: GetLogbooks,
    private val getLogbookCountThisMonth: GetLogbookCountThisMonth,
    private val billingManager: BillingManager,
    private val exportUserDataToZip: ExportUserDataToZip,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UIEvent>()
    val eventFlow: SharedFlow<UIEvent> = _eventFlow.asSharedFlow()

    init {
        getPreferences()
        getCatchesAndVisits()
        getPlanStatus()
    }

    fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.ShowMoreOption -> showMoreOption(event.isShow)
            is ProfileEvent.SetTheme -> setTheme(event.theme)
            is ProfileEvent.SetAiLanguage -> setAiLanguage(event.aiLanguage)
            ProfileEvent.GetSignedInUser -> getSignedInUser()
            ProfileEvent.SignOut -> onSignOut()
            is ProfileEvent.ShowPaywall -> _state.update { it.copy(showPaywallSheet = event.isShow) }
            is ProfileEvent.SubscribeToPlus -> billingManager.launchPurchaseFlow(event.activity)
            is ProfileEvent.ExportData -> exportData(event.context, event.uri)
        }
    }

    private fun exportData(context: Context, uri: Uri) {
        _state.update { it.copy(isExporting = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val result = try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    exportUserDataToZip(outputStream)
                } ?: Resource.Error("Unable to open the selected file")
            } catch (e: Exception) {
                Resource.Error(e.message ?: "Something went wrong!")
            }
            _state.update { it.copy(isExporting = false) }
            _eventFlow.emit(
                UIEvent.ShowSnackbar(
                    if (result is Resource.Success) "Data exported successfully"
                    else result.message ?: "Failed to export data"
                )
            )
        }
    }

    private fun getPlanStatus() {
        billingManager.isPlus.onEach { isPlus ->
            _state.update {
                it.copy(
                    isPlus = isPlus,
                    showPaywallSheet = if (isPlus) false else it.showPaywallSheet
                )
            }
        }.launchIn(viewModelScope)

        billingManager.planProductDetails.onEach { productDetails ->
            val price = productDetails
                ?.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases
                ?.pricingPhaseList
                ?.firstOrNull()
                ?.formattedPrice
            _state.update { it.copy(plusPriceLabel = price) }
        }.launchIn(viewModelScope)

        getLogbookCountThisMonth().onEach { result ->
            _state.update { it.copy(logbookCountThisMonth = result.data ?: 0) }
        }.launchIn(viewModelScope)
    }

    private fun getCatchesAndVisits() {
        viewModelScope.launch {
            getLogbooks.invoke().onEach { logbooks ->
                val catches = logbooks.data?.sumOf { it.jumlahIkan ?: 0 } ?: 0
                val visits = logbooks.data?.mapNotNull { it.tempatPenangkapan }?.distinct()?.count()
                _state.update { it.copy(catches = catches, visits = visits) }
            }.stateIn(viewModelScope)
        }
    }

    private fun getPreferences() = viewModelScope.launch {
        themeUseCase.getTheme().onEach { theme ->
            _state.update { it.copy(themeValue = theme.value) }
        }.stateIn(viewModelScope)

        aiLanguageUseCase.getAiLanguage().onEach { aiLanguage ->
            _state.update { it.copy(aiLanguageValue = aiLanguage.value) }
        }.stateIn(viewModelScope)
    }

    private fun showMoreOption(isShow: Boolean) {
        _state.value = _state.value.copy(showProfileMoreOptionBottomSheet = isShow)
    }

    private fun setTheme(theme: Theme) {
        viewModelScope.launch {
            themeUseCase.setTheme(theme)
        }
    }

    private fun setAiLanguage(aiLanguage: AiLanguage) {
        viewModelScope.launch {
            aiLanguageUseCase.setAiLanguage(aiLanguage)
        }
    }

    private fun getSignedInUser() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val user = getSignedInUser.invoke()) {
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _eventFlow.emit(UIEvent.ShowSnackbar(user.message ?: "An error occurred"))
                }

                is Resource.Loading -> {
                    _state.value =
                        _state.value.copy(isLoading = true)
                }

                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        userData = user.data?.toUserData()
                    )
                }
            }
        }
    }

    private fun onSignOut() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = signOutUser.invoke()) {
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false)
                    _eventFlow.emit(UIEvent.ShowSnackbar(result.message ?: "An error occurred"))
                }

                is Resource.Loading -> {
                    _state.value = _state.value.copy(isLoading = true)
                }

                is Resource.Success -> {
                    delay(1000)
                    _state.value = _state.value.copy(isLoading = false)
                    deleteUserSignedIn.invoke()
                    billingManager.clearLocalEntitlement()
                }
            }
        }
    }

    sealed class UIEvent {
        data class ShowSnackbar(val message: String) : UIEvent()
    }

    init {
        getSignedInUser()
    }
}