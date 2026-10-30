package io.github.nodyssey.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import io.github.nodyssey.NotificationsKey
import io.github.nodyssey.PostListKey
import io.github.nodyssey.ProfileKey
import io.github.nodyssey.ui.postlist.HomeAccount
import io.github.nodyssey.ui.resources.Res
import io.github.nodyssey.ui.resources.tab_home
import io.github.nodyssey.ui.resources.tab_notifications
import io.github.nodyssey.ui.resources.tab_profile
import io.github.plaza.designsys.component.UserAvatar
import io.github.plaza.designsys.theme.LocalPlazaLayers
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * The three places the app can be.
 *
 * 搜索 used to be a fourth. It is now reached from 首页's own app bar and lives on 首页's stack, which
 * is where a search started from the feed belongs: Back returns to the list it was started from
 * instead of to whatever the search tab was last left showing.
 *
 * Selected items switch to the filled icon rather than only changing colour — Material 3's own
 * "expressive" signal, and the one that still reads at a glance on a small dark screen.
 */
enum class TopLevelDestination(
    val key: NavKey,
    val label: StringResource,
    val selectedIcon: ImageVector,
    val icon: ImageVector,
) {
    HOME(PostListKey, Res.string.tab_home, Icons.Filled.Home, Icons.Outlined.Home),
    NOTIFICATIONS(
        NotificationsKey,
        Res.string.tab_notifications,
        Icons.Filled.Notifications,
        Icons.Outlined.Notifications,
    ),
    PROFILE(ProfileKey, Res.string.tab_profile, Icons.Filled.Person, Icons.Outlined.Person),
    ;

    companion object {
        fun forKey(key: NavKey?): TopLevelDestination? = entries.firstOrNull { it.key == key }
    }
}

private val NavigationIconSize = 20.dp

/**
 * 首页 / 通知 / 我的 as a bar of the app's own, for the windows the suite draws a bar in.
 *
 * `NavigationSuiteScaffold` still owns the rail and the drawer — a bar pinned to the bottom edge of a
 * tablet is wrong, and the suite is what knows the difference. What it does not own is this bar's
 * height: Material's `NavigationBar` is a fixed 65dp and reserves a line for a label that is no
 * longer drawn, which left 35dp of empty card under three 20dp glyphs. Drawing the bar here puts the
 * row back to [BottomBarHeight] and keeps the three items on one line.
 *
 * The items look the way Material drew them, because that is what this bar had: a pill behind the
 * selected glyph, the filled icon variant, and the unread badge on 通知. Only the room around them
 * changed.
 */
@Composable
fun NodysseyBottomBar(
    current: TopLevelDestination?,
    onSelect: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    // v1 does not read notifications yet, so this is always zero — the slot exists so that turning
    // them on later is a data change rather than a layout change.
    unreadCount: Int = 0,
    /** The signed-in face 我的 draws in place of its glyph, and null while signed out. */
    account: HomeAccount? = null,
) {
    Surface(color = LocalPlazaLayers.current.card, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // The gesture inset goes under the row rather than into it: the bar keeps its height
                // on every device, and the strip below belongs to the system.
                .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                .height(BottomBarHeight),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopLevelDestination.entries.forEach { destination ->
                BottomBarItem(
                    destination = destination,
                    selected = destination == current,
                    unreadCount = unreadCount,
                    account = account,
                    onClick = { onSelect(destination) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/** One tab: the pill, the glyph, and the badge, centred in its share of the row. */
@Composable
private fun BottomBarItem(
    destination: TopLevelDestination,
    selected: Boolean,
    unreadCount: Int,
    account: HomeAccount?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(destination.label)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(role = Role.Tab, onClickLabel = label, onClick = onClick)
            .semantics {
                contentDescription = label
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = IndicatorWidth, height = IndicatorHeight)
                .clip(CircleShape)
                .background(
                    if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // 我的 draws the account's own face, the way the home bar used to. It is the one tab
            // whose glyph can say *which* account, and the picture is what the reader recognises;
            // the person outline stays for a signed-out session, where there is no face to draw.
            if (destination == TopLevelDestination.PROFILE && account != null) {
                UserAvatar(
                    url = account.avatarUrl,
                    name = account.name,
                    size = NavigationIconSize,
                )
            } else {
                Icon(
                    imageVector = if (selected) destination.selectedIcon else destination.icon,
                    // The item above carries the name; describing the glyph again says it twice.
                    contentDescription = null,
                    tint =
                    if (selected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(NavigationIconSize),
                )
            }
        }
        if (destination == TopLevelDestination.NOTIFICATIONS && unreadCount > 0) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(x = BadgeOffsetX, y = BadgeOffsetY),
            ) { Text(unreadCount.coerceAtMost(99).toString()) }
        }
    }
}

/** 48dp: the row the three glyphs sit in, without the system's gesture inset under it. */
val BottomBarHeight = 48.dp

/** The pill around the selected glyph, a step under Material's 64×32 so it fits a 48dp row. */
private val IndicatorWidth = 56.dp

private val IndicatorHeight = 26.dp

/** Far enough along the row that the badge clears the pill's shoulder rather than sitting on it. */
private val BadgeOffsetX = 18.dp

private val BadgeOffsetY = 2.dp
