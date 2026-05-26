package com.yizuka17.dailylife.feature.chart.model

import com.yizuka17.dailylife.core.domain.chart.model.ChartCategoryRank
import com.yizuka17.dailylife.core.domain.chart.model.ChartEntry
import com.yizuka17.dailylife.core.domain.chart.model.ChartPeriod
import com.yizuka17.dailylife.core.domain.chart.model.ChartRangeOption
import com.yizuka17.dailylife.core.domain.chart.model.ChartType
import com.yizuka17.dailylife.core.domain.chart.model.MoodChartEntry
import com.yizuka17.dailylife.core.ui.model.ChartContentStatus

data class ChartUiState(
    val selectedType: ChartType = ChartType.Expense,
    val selectedPeriod: ChartPeriod = ChartPeriod.Week,
    val rangeTabs: List<ChartRangeOption> = emptyList(),
    val selectedRangeOption: ChartRangeOption? = null,
    val entries: List<ChartEntry> = emptyList(),
    val categoryRanks: List<ChartCategoryRank> = emptyList(),
    val categoryNamesById: Map<String, String> = emptyMap(),
    val totalAmount: Double = 0.0,
    val averageAmount: Double = 0.0,
    val moodEntries: List<MoodChartEntry> = emptyList(),
    val contentStatus: ChartContentStatus = ChartContentStatus.Loading,
)
