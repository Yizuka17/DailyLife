package com.yizuka17.dailylife.feature.discover.model

import com.yizuka17.dailylife.core.model.TypeProfile

data class DiscoverTypeProfileUiState(
    val monthTypeProfile: TypeProfile = TypeProfile(),
    val yearTypeProfile: TypeProfile = TypeProfile(),
    val isLoading: Boolean = true,
    val year: Int? = null,
    val month: Int? = null,
)
