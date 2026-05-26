package com.yizuka17.dailylife.feature.discover.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yizuka17.dailylife.core.data.analytics.TransactionAnalyticsRepository
import com.yizuka17.dailylife.core.data.analytics.TransactionAnalyticsRepository.DiscoverAnalyticsCache
import com.yizuka17.dailylife.feature.discover.model.DiscoverTypeProfileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DiscoverViewModel @Inject constructor(
    analyticsRepository: TransactionAnalyticsRepository
) : ViewModel() {

    private val discoverCacheFlow = analyticsRepository.discoverCache()

    val typeProfileState: StateFlow<DiscoverTypeProfileUiState> = discoverCacheFlow
        .map { cache -> cache.toTypeProfileUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = discoverCacheFlow.value.toTypeProfileUiState(isLoading = true)
        )

    private fun DiscoverAnalyticsCache.toTypeProfileUiState(
        isLoading: Boolean = false
    ): DiscoverTypeProfileUiState {
        return DiscoverTypeProfileUiState(
            monthTypeProfile = monthTypeProfile.profile,
            yearTypeProfile = yearTypeProfile.profile,
            isLoading = isLoading,
            year = monthTypeProfile.year,
            month = monthTypeProfile.month
        )
    }
}
