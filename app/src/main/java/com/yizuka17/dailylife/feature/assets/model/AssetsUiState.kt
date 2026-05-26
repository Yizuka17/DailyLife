package com.yizuka17.dailylife.feature.assets.model

import com.yizuka17.dailylife.core.data.local.entity.AssetAccountEntity
import com.yizuka17.dailylife.core.data.local.entity.AssetAccountType

data class AssetsUiState(
    val accounts: List<AssetAccountEntity> = emptyList(),
    val totalBalance: Double = 0.0,
    val isLoading: Boolean = true,
    val error: String? = null,
)

data class AssetAccountEditorState(
    val id: Int? = null,
    val name: String = "",
    val type: AssetAccountType = AssetAccountType.BANK_CARD,
    val balance: String = "",
    val isDefault: Boolean = false,
) {
    companion object {
        fun fromAccount(account: AssetAccountEntity): AssetAccountEditorState {
            return AssetAccountEditorState(
                id = account.id,
                name = account.name,
                type = account.type,
                balance = formatBalanceForInput(account.balance),
                isDefault = account.isDefault,
            )
        }

        private fun formatBalanceForInput(balance: Double): String {
            return if (balance % 1.0 == 0.0) {
                balance.toLong().toString()
            } else {
                "%.2f".format(balance).trimEnd('0').trimEnd('.')
            }
        }
    }
}
