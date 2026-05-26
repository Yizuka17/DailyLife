package com.yizuka17.dailylife.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import com.yizuka17.dailylife.R
import com.yizuka17.dailylife.core.ui.navigation.TopLevelDestination
import com.yizuka17.dailylife.feature.assets.navigation.AssetsRoute
import com.yizuka17.dailylife.feature.chart.navigation.ChartRoute
import com.yizuka17.dailylife.feature.details.navigation.DetailsRoute
import com.yizuka17.dailylife.feature.discover.navigation.DiscoverRoute
import com.yizuka17.dailylife.feature.me.navigation.MeRoute

val topLevelDestinations = listOf(
    TopLevelDestination(DetailsRoute.DETAILS, R.string.details, Icons.AutoMirrored.Filled.List),
    TopLevelDestination(AssetsRoute.ASSETS, R.string.assets, Icons.Default.AccountBalanceWallet),
    TopLevelDestination(ChartRoute.CHART, R.string.chart, Icons.Default.BarChart),
    TopLevelDestination(DiscoverRoute.DISCOVER, R.string.discover, Icons.Default.Explore),
    TopLevelDestination(MeRoute.ME, R.string.me, Icons.Default.Person),
)
