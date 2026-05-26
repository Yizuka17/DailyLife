package com.yizuka17.dailylife.feature.currency.model

import java.math.BigDecimal

data class CurrencyConversionResult(
    val baseCurrency: CurrencyOption,
    val targetCurrency: CurrencyOption,
    val baseAmount: BigDecimal,
    val targetAmount: BigDecimal,
    val rate: BigDecimal,
    val inverseRate: BigDecimal,
)
