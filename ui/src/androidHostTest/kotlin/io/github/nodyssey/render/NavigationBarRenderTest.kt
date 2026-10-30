package io.github.nodyssey.render

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import io.github.nodyssey.ui.navigation.NodysseyBottomBar
import io.github.nodyssey.ui.navigation.TopLevelDestination
import io.github.nodyssey.ui.postlist.HomeAccount
import io.github.plaza.designsys.theme.PlazaTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * The bottom bar on its own, which no screen render test reaches: every screen draws its own
 * `Scaffold`, and the bar sits a level above them. The page behind it is empty on purpose — the bar
 * is the subject.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h800dp")
class NavigationBarRenderTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun onlyWhenRendering() = assumeRendering()

    @Composable
    private fun Screen(
        darkTheme: Boolean,
        current: TopLevelDestination,
        unreadCount: Int = 0,
        account: HomeAccount? = null,
    ) {
        PlazaTheme(darkTheme = darkTheme) {
            Box(Modifier.fillMaxSize()) {
                NodysseyBottomBar(
                    current = current,
                    onSelect = {},
                    unreadCount = unreadCount,
                    account = account,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }

    @Test
    fun `the bar in dark`() {
        composeRule.setContent { Screen(darkTheme = true, current = TopLevelDestination.HOME) }

        composeRule.onRoot().captureRender("navigation-bar-dark")
    }

    @Test
    fun `the bar in light`() {
        composeRule.setContent { Screen(darkTheme = false, current = TopLevelDestination.HOME) }

        composeRule.onRoot().captureRender("navigation-bar-light")
    }

    /** The unread count on 通知, which is the one item that can grow a badge. */
    @Test
    fun `the bar with unread notifications in dark`() {
        composeRule.setContent {
            Screen(darkTheme = true, current = TopLevelDestination.NOTIFICATIONS, unreadCount = 12)
        }

        composeRule.onRoot().captureRender("navigation-bar-badge-dark")
    }

    /** 我的 while signed in: the account's own face in place of the person outline. */
    @Test
    fun `the bar with a signed-in account in dark`() {
        composeRule.setContent {
            Screen(
                darkTheme = true,
                current = TopLevelDestination.PROFILE,
                // A null URL takes the monogram the app falls back to, which is what the JVM can draw.
                account = HomeAccount(name = "tiao", avatarUrl = null),
            )
        }

        composeRule.onRoot().captureRender("navigation-bar-account-dark")
    }
}
