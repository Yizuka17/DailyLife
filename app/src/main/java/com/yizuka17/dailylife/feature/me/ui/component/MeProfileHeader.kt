package com.yizuka17.dailylife.feature.me.ui.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.yizuka17.dailylife.R
import com.yizuka17.dailylife.feature.me.ui.MeProfileStatsUiState
import com.yizuka17.dailylife.feature.me.ui.MeProfileUiState

@Composable
fun MeProfileHeader(
    profile: MeProfileUiState,
    stats: MeProfileStatsUiState,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val defaultDisplayName = stringResource(R.string.me_profile_display_name)
    val displayName = profile.displayName.ifBlank { defaultDisplayName }
    val defaultSignature = stringResource(R.string.me_profile_signature)
    val signature = profile.signature.ifBlank { defaultSignature }
    val avatarSizePx = with(LocalDensity.current) { 64.dp.roundToPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(containerColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.12f)),
                ) {
                    val customAvatarUri = profile.avatarUri.takeIf { it.isNotBlank() }
                    val customAvatarBitmap = remember(customAvatarUri, avatarSizePx) {
                        customAvatarUri
                            ?.removePrefix("file://")
                            ?.let { decodeSampledBitmapFile(path = it, targetSizePx = avatarSizePx) }
                            ?.asImageBitmap()
                    }
                    if (customAvatarBitmap != null) {
                        Image(
                            bitmap = customAvatarBitmap,
                            contentDescription = stringResource(R.string.me_profile_avatar_content_description),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_user),
                            contentDescription = stringResource(R.string.me_profile_avatar_content_description),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = signature,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            MeProfileStatsRow(
                stats = stats,
                contentColor = contentColor,
            )
        }
    }
}

private fun decodeSampledBitmapFile(
    path: String,
    targetSizePx: Int,
) = runCatching {
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, boundsOptions)
    val sourceWidth = boundsOptions.outWidth
    val sourceHeight = boundsOptions.outHeight
    if (sourceWidth <= 0 || sourceHeight <= 0) return@runCatching null

    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateBitmapSampleSize(
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            targetSizePx = targetSizePx,
        )
    }
    BitmapFactory.decodeFile(path, decodeOptions)
}.getOrNull()
private fun calculateBitmapSampleSize(
    sourceWidth: Int,
    sourceHeight: Int,
    targetSizePx: Int,
): Int {
    var sampleSize = 1
    while (sourceWidth / sampleSize > targetSizePx * 2 || sourceHeight / sampleSize > targetSizePx * 2) {
        sampleSize *= 2
    }
    return sampleSize
}

@Composable
private fun MeProfileStatsRow(
    stats: MeProfileStatsUiState,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    val consecutiveDaysText = if (stats.isLoading) "--" else stats.consecutiveCheckInDays.toString()
    val totalDaysText = if (stats.isLoading) "--" else stats.totalActiveDays.toString()
    val totalTransactionsText = if (stats.isLoading) "--" else stats.totalTransactions.toString()

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MeProfileStatItem(
            value = consecutiveDaysText,
            label = stringResource(R.string.me_profile_stat_streak),
            contentColor = contentColor,
            modifier = Modifier.weight(1f),
        )
        MeProfileStatItem(
            value = totalDaysText,
            label = stringResource(R.string.me_profile_stat_total_days),
            contentColor = contentColor,
            modifier = Modifier.weight(1f),
        )
        MeProfileStatItem(
            value = totalTransactionsText,
            label = stringResource(R.string.me_profile_stat_total_transactions),
            contentColor = contentColor,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MeProfileStatItem(
    value: String,
    label: String,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = contentColor,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor.copy(alpha = 0.7f),
        )
    }
}
