package com.yizuka17.dailylife.feature.me.ui.component

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.yizuka17.dailylife.R
import kotlin.math.max

@Composable
fun AvatarCropDialog(
    bitmap: Bitmap,
    onDismiss: () -> Unit,
    onConfirm: (Bitmap) -> Unit,
    modifier: Modifier = Modifier,
) {
    var scale by remember(bitmap) { mutableFloatStateOf(1f) }
    var offset by remember(bitmap) { mutableStateOf(Offset.Zero) }
    var previewSize by remember(bitmap) { mutableStateOf(IntSize.Zero) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Text(
                    text = stringResource(R.string.me_profile_avatar_crop_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.me_profile_avatar_crop_tip),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .onSizeChanged { previewSize = it }
                        .pointerInput(bitmap) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                offset += pan
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = stringResource(R.string.me_profile_avatar_content_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                            },
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = stringResource(R.string.common_cancel))
                    }
                    TextButton(
                        onClick = {
                            val cropped = cropAvatarBitmap(
                                source = bitmap,
                                previewSize = previewSize,
                                scale = scale,
                                offset = offset,
                                outputSizePx = AVATAR_OUTPUT_SIZE_PX,
                            )
                            onConfirm(cropped)
                        },
                    ) {
                        Text(text = stringResource(R.string.common_confirm))
                    }
                }
            }
        }
    }
}

private fun cropAvatarBitmap(
    source: Bitmap,
    previewSize: IntSize,
    scale: Float,
    offset: Offset,
    outputSizePx: Int,
): Bitmap {
    val previewWidth = previewSize.width.takeIf { it > 0 } ?: outputSizePx
    val previewHeight = previewSize.height.takeIf { it > 0 } ?: outputSizePx
    val previewSide = minOf(previewWidth, previewHeight).toFloat()
    val output = Bitmap.createBitmap(outputSizePx, outputSizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)

    val baseScale = max(
        previewSide / source.width.toFloat(),
        previewSide / source.height.toFloat(),
    )
    val outputRatio = outputSizePx / previewSide
    val finalScale = baseScale * scale * outputRatio
    val centerDx = (outputSizePx - source.width * finalScale) / 2f
    val centerDy = (outputSizePx - source.height * finalScale) / 2f

    val matrix = Matrix().apply {
        postScale(finalScale, finalScale)
        postTranslate(
            centerDx + offset.x * outputRatio,
            centerDy + offset.y * outputRatio,
        )
    }
    canvas.drawColor(Color.Transparent.toArgb())
    canvas.drawBitmap(source, matrix, null)
    return output
}

private const val AVATAR_OUTPUT_SIZE_PX = 512
