package com.yizuka17.dailylife.feature.me.ui.about

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.yizuka17.dailylife.R
import com.yizuka17.dailylife.core.common.launchExternalUrl
import com.yizuka17.dailylife.core.ui.navigation.safePopBackStack
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltApi
import com.moriafly.salt.ui.ext.safeMainPadding

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(UnstableSaltApi::class, ExperimentalFoundationApi::class)
@Composable
fun AboutAuthorScreen(navController: NavHostController) {
    val pagerState = rememberPagerState(pageCount = { 2 })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SaltTheme.colors.background)
            .safeMainPadding(),
    ) {
        TitleBar(
            onBack = { navController.safePopBackStack() },
            text = stringResource(id = R.string.me_about_author),
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            when (page) {
                0 -> CurrentAuthorPage()
                else -> OriginalAuthorPage()
            }
        }
    }
}

@Composable
private fun CurrentAuthorPage() {
    val context = LocalContext.current

    CurrentAuthorPageContent(
        profile = CURRENT_AUTHOR_PROFILE,
        onOpenGitHub = { context.launchExternalUrl(CURRENT_AUTHOR_PROFILE.htmlUrl) },
        onOpenTwitter = {
            context.launchExternalUrl(CURRENT_AUTHOR_X_URL)
        },
    )
}

@Composable
private fun CurrentAuthorPageContent(
    profile: GitHubProfile,
    onOpenGitHub: () -> Unit,
    onOpenTwitter: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = stringResource(id = R.string.me_about_author_page_current_hint),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        GitHubAuthorHero(profile = profile)
        GitHubProfileCard(profile = profile, onOpenGitHub = onOpenGitHub, onOpenTwitter = onOpenTwitter)
    }
}

@Composable
private fun GitHubAuthorHero(profile: GitHubProfile) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Avatar(imageResId = R.drawable.github_avatar_yizuka17)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "@${profile.login}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                )
            }
            Text(
                text = profile.safeBio.ifBlank { stringResource(id = R.string.me_about_author_current_bio_empty) },
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )

        }
    }
}

@Composable
private fun GitHubProfileCard(
    profile: GitHubProfile,
    onOpenGitHub: () -> Unit,
    onOpenTwitter: (() -> Unit)?,
) {
    SectionCard {
        Text(
            text = stringResource(id = R.string.me_about_author_current_profile_title),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary,
        )
        ContactRow(
            contact = ContactInfo(
                icon = Icons.Outlined.Link,
                label = stringResource(id = R.string.me_about_author_contact_github_label),
                value = profile.htmlUrl.removePrefix("https://"),
                onClick = onOpenGitHub,
            ),
        )
        profile.twitterUsername?.takeIf { it.isNotBlank() }?.let { username ->
            HorizontalDivider(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                thickness = 0.6.dp,
            )
            ContactRow(
                contact = ContactInfo(
                    icon = Icons.Outlined.AlternateEmail,
                    label = stringResource(id = R.string.me_about_author_current_twitter_label),
                    value = "x.com/$username",
                    onClick = onOpenTwitter,
                ),
            )
        }
    }
}

@Composable
private fun OriginalAuthorPage() {
    val context = LocalContext.current

    val recentItems = listOf(
        stringResource(id = R.string.me_about_author_recent_item_1),
        stringResource(id = R.string.me_about_author_recent_item_2),
        stringResource(id = R.string.me_about_author_recent_item_3),
    )

    val contacts = listOf(
        ContactInfo(
            icon = Icons.Outlined.Public,
            label = stringResource(id = R.string.me_about_author_contact_site_label),
            value = stringResource(id = R.string.me_about_author_contact_site_value),
            onClick = { context.launchExternalUrl(AUTHOR_SITE_URL) },
        ),
        ContactInfo(
            icon = Icons.Outlined.Link,
            label = stringResource(id = R.string.me_about_author_contact_github_label),
            value = stringResource(id = R.string.me_about_author_contact_github_value),
            onClick = { context.launchExternalUrl(AUTHOR_GITHUB_URL) },
        ),
        ContactInfo(
            icon = Icons.Outlined.Email,
            label = stringResource(id = R.string.me_about_author_contact_email_label),
            value = stringResource(id = R.string.me_about_author_contact_email_value),
            onClick = { context.launchExternalUrl(AUTHOR_EMAIL_URI) },
        ),
    )

    AuthorPageContent(
        pageHint = stringResource(id = R.string.me_about_author_page_original_hint),
        name = "Evening",
        role = stringResource(id = R.string.me_about_author_role),
        intro = stringResource(id = R.string.me_about_author_intro),
        listTitle = stringResource(id = R.string.me_about_author_recent_title),
        listItems = recentItems,
        contacts = contacts,
        footer = stringResource(id = R.string.me_about_author_footer),
    )
}

@Composable
private fun AuthorPageContent(
    pageHint: String,
    name: String,
    role: String,
    intro: String,
    listTitle: String,
    listItems: List<String>,
    contacts: List<ContactInfo>,
    footer: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = pageHint,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        AuthorHeader(name = name, role = role)

        SectionCard {
            Text(
                text = intro,
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
            )
        }

        SectionCard {
            Text(
                text = listTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listItems.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }

        SectionCard {
            Text(
                text = stringResource(id = R.string.me_about_author_contacts_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                contacts.forEachIndexed { index, contact ->
                    ContactRow(contact = contact)
                    if (index != contacts.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxWidth(),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                            thickness = 0.6.dp,
                        )
                    }
                }
            }
        }

        SectionCard {
            Text(
                text = footer,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun AuthorHeader(name: String, role: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Avatar()

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Avatar(imageResId: Int = R.drawable.ic_user) {
    Box(
        modifier = Modifier
            .size(96.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.matchParentSize(),
        ) {}
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = null,
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun SectionCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun ContactRow(contact: ContactInfo) {
    val shape = RoundedCornerShape(16.dp)
    val rowModifier = if (contact.onClick != null) {
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(onClick = contact.onClick)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    } else {
        Modifier.fillMaxWidth()
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        ) {
            Icon(
                imageVector = contact.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(10.dp),
            )
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = contact.label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = contact.value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (contact.onClick != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
    }
}

private data class ContactInfo(
    val icon: ImageVector,
    val label: String,
    val value: String,
    val onClick: (() -> Unit)? = null,
)

private const val AUTHOR_SITE_URL = "https://evening.dev"
private const val AUTHOR_GITHUB_URL = "https://github.com/Evening-01"
private const val AUTHOR_EMAIL_URI = "mailto:H3410233124@gmail.com"
private const val CURRENT_AUTHOR_GITHUB_URL = "https://github.com/Yizuka17"
private const val CURRENT_AUTHOR_X_URL = "https://x.com/JyushitiYizuka"

private val CURRENT_AUTHOR_PROFILE = GitHubProfile(
    login = "Yizuka17",
    name = "JYUSHITI",
    bio = "Maintain passion for life now and for the future 🩷\nA student learning in ECNU. Not an expert in coding. Mainly develop with AI.",
    htmlUrl = CURRENT_AUTHOR_GITHUB_URL,
    twitterUsername = "JyushitiYizuka",
    publicRepos = 4,
    followers = 13,
    following = 13,
)

private data class GitHubProfile(
    val login: String,
    val name: String? = null,
    val bio: String? = null,
    val htmlUrl: String,
    val twitterUsername: String? = null,
    val publicRepos: Int = 0,
    val followers: Int = 0,
    val following: Int = 0,
) {
    val displayName: String
        get() = name?.takeIf { it.isNotBlank() } ?: login

    val safeBio: String
        get() = bio.orEmpty()
}

