package com.yizuka17.dailylife.feature.me.ui.settings.general

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.BrightnessMedium
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.yizuka17.dailylife.R
import com.yizuka17.dailylife.core.data.preferences.AppLanguage
import com.yizuka17.dailylife.core.data.preferences.ThemeMode
import com.yizuka17.dailylife.core.ui.designsystem.component.ItemPopup
import com.yizuka17.dailylife.core.ui.navigation.safePopBackStack
import com.yizuka17.dailylife.feature.me.ui.component.AvatarCropDialog
import com.moriafly.salt.ui.Item
import com.moriafly.salt.ui.ItemSwitcher
import com.moriafly.salt.ui.ItemTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.ext.safeMainPadding
import com.moriafly.salt.ui.popup.PopupMenuItem
import com.moriafly.salt.ui.popup.rememberPopupState
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableSaltApi::class)
@Composable
fun GeneralSettingsScreen(
    navController: NavHostController,
    viewModel: GeneralSettingsViewModel = hiltViewModel(),
) {
    val dynamicColorEnabled by viewModel.dynamicColor.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val customFontEnabled by viewModel.customFontEnabled.collectAsState()
    val profileDisplayName by viewModel.profileDisplayName.collectAsState()
    val profileSignature by viewModel.profileSignature.collectAsState()
    val profileAvatarUri by viewModel.profileAvatarUri.collectAsState()
    val pendingLanguage by viewModel.pendingLanguage.collectAsState()

    val themeModePopupState = rememberPopupState()
    val scalePopupState = rememberPopupState()
    val languagePopupState = rememberPopupState()
    val context = LocalContext.current
    val defaultProfileDisplayName = stringResource(R.string.me_profile_display_name)
    val defaultProfileSignature = stringResource(R.string.me_profile_signature)
    val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val dynamicColorUnsupportedMessage = stringResource(R.string.dynamic_color_unsupported)

    var scalePopupHandled by remember { mutableStateOf(true) }
    var initialFontScale by remember { mutableStateOf(fontScale) }
    var showDisplayNameDialog by remember { mutableStateOf(false) }
    var showSignatureDialog by remember { mutableStateOf(false) }
    var pendingAvatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let {
            pendingAvatarBitmap = decodeSampledBitmapFromUri(
                context = context,
                sourceUri = it,
                maxSizePx = PROFILE_AVATAR_EDIT_MAX_SIZE_PX,
            )
        }
    }

    pendingAvatarBitmap?.let { bitmap ->
        AvatarCropDialog(
            bitmap = bitmap,
            onDismiss = {
                pendingAvatarBitmap = null
                bitmap.recycle()
            },
            onConfirm = { croppedBitmap ->
                saveProfileAvatarBitmapToPrivateStorage(context, croppedBitmap)?.let(viewModel::setProfileAvatarUri)
                croppedBitmap.recycle()
                bitmap.recycle()
                pendingAvatarBitmap = null
            },
        )
    }

    LaunchedEffect(scalePopupState.expend) {
        if (scalePopupState.expend) {
            scalePopupHandled = false
            initialFontScale = fontScale
        } else if (!scalePopupHandled) {
            viewModel.revertFontScale(initialFontScale)
            scalePopupHandled = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            .safeMainPadding(),
    ) {
        TitleBar(
            onBack = { navController.safePopBackStack() },
            text = stringResource(id = R.string.general_settings),
        )

        RoundedColumn(modifier = Modifier.fillMaxWidth()) {
            ItemTitle(text = stringResource(id = R.string.user_interface))

            Box {
                ItemSwitcher(
                    state = dynamicColorEnabled,
                    onChange = viewModel::setDynamicColor,
                    enabled = isDynamicColorSupported,
                    text = stringResource(R.string.dynamic_color_switcher_text),
                    sub = stringResource(R.string.dynamic_color_switcher_sub),
                    iconPainter = rememberVectorPainter(image = Icons.Outlined.Palette),
                    iconPaddingValues = PaddingValues(all = 1.8.dp),
                    iconColor = SaltTheme.colors.text,
                )

                if (!isDynamicColorSupported) {
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                Toast
                                    .makeText(
                                        context,
                                        dynamicColorUnsupportedMessage,
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            },
                    )
                }
            }

            ItemPopup(
                state = themeModePopupState,
                iconPainter = rememberVectorPainter(image = Icons.Outlined.BrightnessMedium),
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                iconColor = SaltTheme.colors.text,
                text = stringResource(R.string.theme_mode_switcher_text),
                selectedItem = stringResource(id = themeMode.resId),
                popupWidth = 160,
            ) {
                ThemeMode.entries.forEach { mode ->
                    PopupMenuItem(
                        onClick = {
                            viewModel.setThemeMode(mode)
                            themeModePopupState.dismiss()
                        },
                        text = stringResource(id = mode.resId),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        RoundedColumn(modifier = Modifier.fillMaxWidth()) {
            ItemTitle(text = stringResource(id = R.string.user_profile_settings))

            Item(
                text = stringResource(R.string.me_profile_edit_avatar_title),
                sub = profileAvatarUri.takeIf { it.isNotBlank() }?.let {
                    stringResource(R.string.me_profile_avatar_customized)
                } ?: stringResource(R.string.me_profile_avatar_default),
                iconPainter = rememberVectorPainter(image = Icons.Outlined.AccountCircle),
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                iconColor = SaltTheme.colors.text,
                onClick = { avatarPickerLauncher.launch("image/*") },
            )

            Item(
                text = stringResource(R.string.me_profile_edit_nickname_title),
                sub = profileDisplayName.ifBlank { defaultProfileDisplayName },
                iconPainter = rememberVectorPainter(image = Icons.Outlined.Badge),
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                iconColor = SaltTheme.colors.text,
                onClick = { showDisplayNameDialog = true },
            )

            Item(
                text = stringResource(R.string.me_profile_edit_signature_title),
                sub = profileSignature.ifBlank { defaultProfileSignature },
                iconPainter = rememberVectorPainter(image = Icons.Outlined.Draw),
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                iconColor = SaltTheme.colors.text,
                onClick = { showSignatureDialog = true },
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        RoundedColumn(modifier = Modifier.fillMaxWidth()) {
            ItemTitle(text = stringResource(id = R.string.text_language_settings))

            val scaleSummary = stringResource(
                R.string.text_scale_selected_percent,
                (fontScale * 100).roundToInt(),
            )

            ItemPopup(
                state = scalePopupState,
                iconPainter = rememberVectorPainter(image = Icons.Outlined.FormatSize),
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                iconColor = SaltTheme.colors.text,
                text = stringResource(R.string.text_scale_switcher_text),
                sub = stringResource(R.string.text_scale_switcher_sub),
                selectedItem = scaleSummary,
                popupWidth = 240,
            ) {
                ScalePopupContent(
                    fontScale = fontScale,
                    onFontScaleChange = viewModel::previewFontScale,
                    onReset = {
                        viewModel.resetScaleToDefault()
                    },
                    onConfirm = { confirmedFont ->
                        viewModel.confirmFontScale(confirmedFont)
                        scalePopupHandled = true
                        scalePopupState.dismiss()
                    },
                    onCancel = {
                        viewModel.revertFontScale(initialFontScale)
                        scalePopupHandled = true
                        scalePopupState.dismiss()
                    }
                )
            }

            ItemPopup(
                state = languagePopupState,
                iconPainter = rememberVectorPainter(image = Icons.Outlined.Language),
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                iconColor = SaltTheme.colors.text,
                text = stringResource(R.string.language_switcher_text),
                sub = stringResource(R.string.language_switcher_sub),
                selectedItem = stringResource(id = appLanguage.resId),
                popupWidth = 200,
            ) {
                AppLanguage.entries.forEach { language ->
                    PopupMenuItem(
                        onClick = {
                            viewModel.onLanguageOptionSelected(language)
                            languagePopupState.dismiss()
                        },
                        text = stringResource(id = language.resId),
                    )
                }
            }

            ItemSwitcher(
                state = customFontEnabled,
                onChange = viewModel::setCustomFontEnabled,
                text = stringResource(R.string.custom_font_switcher_text),
                sub = stringResource(R.string.custom_font_switcher_sub),
                iconPainter = rememberVectorPainter(image = Icons.Outlined.FontDownload),
                iconPaddingValues = PaddingValues(all = 1.8.dp),
                iconColor = SaltTheme.colors.text,
            )
        }
    }

    if (showDisplayNameDialog) {
        ProfileTextEditDialog(
            title = stringResource(R.string.me_profile_edit_nickname_title),
            label = stringResource(R.string.me_profile_nickname_label),
            initialValue = profileDisplayName.ifBlank { defaultProfileDisplayName },
            singleLine = true,
            onDismiss = { showDisplayNameDialog = false },
            onConfirm = {
                viewModel.setProfileDisplayName(it)
                showDisplayNameDialog = false
            },
        )
    }

    if (showSignatureDialog) {
        ProfileTextEditDialog(
            title = stringResource(R.string.me_profile_edit_signature_title),
            label = stringResource(R.string.me_profile_signature_label),
            initialValue = profileSignature.ifBlank { defaultProfileSignature },
            singleLine = false,
            onDismiss = { showSignatureDialog = false },
            onConfirm = {
                viewModel.setProfileSignature(it)
                showSignatureDialog = false
            },
        )
    }

    pendingLanguage?.let { target ->
        LanguageRestartDialog(
            targetLanguage = target,
            onConfirm = viewModel::confirmLanguageChange,
            onDismiss = viewModel::dismissLanguageChange,
        )
    }
}

@Composable
private fun ScalePopupContent(
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    onReset: () -> Unit,
    onConfirm: (Float) -> Unit,
    onCancel: () -> Unit,
) {
    var fontScaleState by remember(fontScale) { mutableStateOf(fontScale) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.text_scale_popup_title),
            style = SaltTheme.textStyles.main,
            color = SaltTheme.colors.text,
        )

        ScaleSlider(
            title = stringResource(R.string.font_scale_label),
            value = fontScaleState,
            onValueChange = {
                fontScaleState = it
                onFontScaleChange(it)
            }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = {
                fontScaleState = 1.0f
                onReset()
            }) {
                Text(text = stringResource(R.string.action_reset))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCancel) {
                    Text(text = stringResource(R.string.common_cancel))
                }
                Button(onClick = { onConfirm(fontScaleState) }) {
                    Text(text = stringResource(R.string.common_confirm))
                }
            }
        }
    }
}

@Composable
private fun ScaleSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = SaltTheme.textStyles.main,
            color = SaltTheme.colors.text,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.scale_value_percent, (value * 100).roundToInt()),
                style = SaltTheme.textStyles.sub,
                color = SaltTheme.colors.subText,
                modifier = Modifier.width(60.dp),
                textAlign = TextAlign.Center,
            )
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = 0.9f..1.2f,
                steps = 5,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ProfileTextEditDialog(
    title: String,
    label: String,
    initialValue: String,
    singleLine: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember(initialValue) { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(text = label) },
                singleLine = singleLine,
                minLines = if (singleLine) 1 else 2,
                maxLines = if (singleLine) 1 else 3,
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
                Text(text = stringResource(R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_cancel))
            }
        },
    )
}

private fun saveProfileAvatarBitmapToPrivateStorage(
    context: android.content.Context,
    bitmap: Bitmap,
): String? {
    return runCatching {
        val avatarsDir = File(context.filesDir, "profile_avatars").apply { mkdirs() }
        val targetFile = File(avatarsDir, "avatar-${UUID.randomUUID()}.jpg")

        FileOutputStream(targetFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, PROFILE_AVATAR_JPEG_QUALITY, output)
        }

        avatarsDir.listFiles()
            ?.filter { it.isFile && it != targetFile }
            ?.forEach { it.delete() }

        Uri.fromFile(targetFile).toString()
    }.getOrNull()
}

private fun decodeSampledBitmapFromUri(
    context: android.content.Context,
    sourceUri: Uri,
    maxSizePx: Int,
): Bitmap? {
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(sourceUri)?.use { input ->
        BitmapFactory.decodeStream(input, null, boundsOptions)
    }

    val sourceWidth = boundsOptions.outWidth
    val sourceHeight = boundsOptions.outHeight
    if (sourceWidth <= 0 || sourceHeight <= 0) return null

    val sampleSize = calculateBitmapSampleSize(
        sourceWidth = sourceWidth,
        sourceHeight = sourceHeight,
        maxSizePx = maxSizePx,
    )
    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }

    val sampledBitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
        BitmapFactory.decodeStream(input, null, decodeOptions)
    } ?: return null

    val longestSide = maxOf(sampledBitmap.width, sampledBitmap.height)
    if (longestSide <= maxSizePx) return sampledBitmap

    val scale = maxSizePx.toFloat() / longestSide.toFloat()
    val scaledWidth = (sampledBitmap.width * scale).roundToInt().coerceAtLeast(1)
    val scaledHeight = (sampledBitmap.height * scale).roundToInt().coerceAtLeast(1)
    val scaledBitmap = Bitmap.createScaledBitmap(sampledBitmap, scaledWidth, scaledHeight, true)
    if (scaledBitmap != sampledBitmap) {
        sampledBitmap.recycle()
    }
    return scaledBitmap
}

private fun calculateBitmapSampleSize(
    sourceWidth: Int,
    sourceHeight: Int,
    maxSizePx: Int,
): Int {
    var sampleSize = 1
    val halfWidth = sourceWidth / 2
    val halfHeight = sourceHeight / 2
    while (halfWidth / sampleSize >= maxSizePx && halfHeight / sampleSize >= maxSizePx) {
        sampleSize *= 2
    }
    return sampleSize
}

private const val PROFILE_AVATAR_EDIT_MAX_SIZE_PX = 1600
private const val PROFILE_AVATAR_JPEG_QUALITY = 90

@Composable
private fun LanguageRestartDialog(
    targetLanguage: AppLanguage,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.language_restart_title))
        },
        text = {
            val targetLabel = stringResource(id = targetLanguage.resId)
            Text(text = stringResource(R.string.language_restart_message, targetLabel))
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = stringResource(R.string.restart_now))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.common_cancel))
            }
        }
    )
}
