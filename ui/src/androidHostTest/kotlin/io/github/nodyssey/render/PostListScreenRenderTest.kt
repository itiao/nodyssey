package io.github.nodyssey.render

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureScreenRoboImage
import io.github.nodyssey.data.Board
import io.github.nodyssey.data.FeedPost
import io.github.nodyssey.model.PostSummary
import io.github.nodyssey.ui.navigation.NodysseyBottomBar
import io.github.nodyssey.ui.navigation.TopLevelDestination
import io.github.nodyssey.ui.postlist.HomeFeedStates
import io.github.nodyssey.ui.postlist.PostListScreen
import io.github.nodyssey.ui.postlist.PostListUiState
import io.github.plaza.designsys.theme.PlazaTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * 首页 feed in both themes — the app's front door, so the first picture on the promo page.
 *
 * Avatars are left `null` on purpose: the JVM has no network, so a real `/avatar/<uid>.png` would
 * never paint. `null` takes the same fallback the app uses for a picture-less account — the coloured
 * monogram — which renders deterministically and reads far better than an empty grey square.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w360dp-h800dp")
class PostListScreenRenderTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun onlyWhenRendering() = assumeRendering()

    @Composable
    private fun Feed() {
        val feed =
            remember {
                flowOf(
                    PagingData.from(
                        data = FEED,
                        sourceLoadStates =
                        LoadStates(
                            refresh = LoadState.NotLoading(false),
                            prepend = LoadState.NotLoading(true),
                            append = LoadState.NotLoading(true),
                        ),
                    ),
                )
            }
        PostListScreen(
            state = PostListUiState(boards = BOARDS),
            postsFor = { feed.collectAsLazyPagingItems() },
            feedStates = rememberSaveable(saver = HomeFeedStates.Saver) { HomeFeedStates() },
            onPostClick = {},
            onBoardClick = {},
            onSortChange = {},
            onSignInClick = {},
            onRecoverInBrowser = {},
        )
    }

    /**
     * [withBar] puts the app's bottom bar under the screen.
     *
     * The bar is not part of 首页 — it belongs to the suite one level up — so a picture of the screen
     * alone is a picture with its foot cut off. Every shot here that is about the feed keeps the bar
     * off (the screen is the subject); the one shot that is about the app's chrome as a whole turns
     * it on.
     */
    @Composable
    private fun Screen(darkTheme: Boolean, pureWhite: Boolean = false, withBar: Boolean = false) {
        PlazaTheme(darkTheme = darkTheme, pureWhite = pureWhite) {
            if (withBar) {
                // The app's own arrangement for a compact window: the screen above the bar, which
                // keeps its height instead of the screen running under it.
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) { Feed() }
                    NodysseyBottomBar(current = TopLevelDestination.HOME, onSelect = {})
                }
            } else {
                Feed()
            }
        }
    }

    @Test
    fun `the feed in light`() {
        composeRule.setContent { Screen(darkTheme = false) }

        composeRule.onRoot().captureRender("post-list-light")
    }

    @Test
    fun `the feed in dark`() {
        composeRule.setContent { Screen(darkTheme = true) }

        composeRule.onRoot().captureRender("post-list-dark")
    }

    /** 首页 as the app draws it: header, feed and the bottom bar in one frame. */
    @Test
    fun `the feed with the bottom bar in dark`() {
        composeRule.setContent { Screen(darkTheme = true, withBar = true) }

        composeRule.onRoot().captureRender("post-list-with-bar-dark")
    }

    @Test
    fun `the feed in pure white`() {
        composeRule.setContent { Screen(darkTheme = false, pureWhite = true) }

        composeRule.onRoot().captureRender("post-list-pure-white")
    }

    @Test
    fun `neutral dark feed`() {
        composeRule.setContent { Screen(darkTheme = true, pureWhite = true) }
        composeRule.onRoot().captureRender("post-list-neutral-dark")
    }

    /** The two menus of the header, open over the feed; each marks its current entry. */
    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun `the sort menu in light`() {
        composeRule.setContent { Screen(darkTheme = false) }
        // 排序 lives on 综合 held down now that the header row is gone.
        composeRule.onNodeWithContentDescription("综合").performTouchInput { longClick() }
        composeRule.waitForIdle()

        captureScreenRoboImage(filePath = "build/outputs/renders/post-list-sort-light.png")
    }

    /** The board strip opened over the feed: every board as a wrapped grid of pills. */
    @OptIn(ExperimentalRoborazziApi::class)
    @Test
    fun `the board strip expanded in light`() {
        composeRule.setContent { Screen(darkTheme = false) }
        composeRule.onNodeWithContentDescription("全部版块").performClick()
        composeRule.waitForIdle()

        captureScreenRoboImage(filePath = "build/outputs/renders/post-list-boards-light.png")
    }

    private companion object {
        val BOARDS =
            listOf(
                Board(null, "综合", null),
                Board("daily", "日常", null),
                Board("tech", "技术", null),
                Board("info", "情报", null),
                Board("trade", "交易", null),
                Board("review", "测评", null),
                Board("carpool", "拼车", null),
                Board("dev", "Dev", null),
                Board("life", "生活", null),
                Board("photo-share", "贴图", null),
            )

        private fun post(
            id: Long,
            title: String,
            author: String,
            category: String,
            comments: Int,
            views: Int,
            lastActive: String,
            lastCommenter: String? = null,
            newComments: Int = 0,
            pinned: Boolean = false,
            awarded: Boolean = false,
            read: Boolean = false,
        ) = FeedPost(
            summary =
            PostSummary(
                postId = id,
                title = title,
                authorName = author,
                authorUid = id,
                avatarUrl = null,
                categoryTitle = category,
                categorySlug = category,
                viewCount = views,
                commentCount = comments,
                lastActiveText = lastActive,
                lastActiveTitle = null,
                lastCommenterName = lastCommenter,
                isPinned = pinned,
                isLocked = false,
                isAwarded = awarded,
            ),
            isRead = read,
            newCommentCount = newComments,
            page = null,
        )

        val FEED =
            listOf(
                post(1, "【公告】NodeSeek 社区行为准则与版规更新", "管理员", "公告", 42, 8600, "5分钟前", pinned = true),
                post(8, "【公告】鸡腿与星辰的获取和使用说明", "管理员", "公告", 17, 5200, "2小时前", pinned = true),
                post(2, "自建 NAS 一年，聊聊我踩过的那些坑和真香时刻", "homelab_er", "技术", 128, 3400, "刚刚", "轻舟", newComments = 6, awarded = true),
                post(3, "有没有那种用了就回不去的小众 App，求推荐", "轻舟", "日常", 87, 2100, "3分钟前", "nas_newbie", newComments = 2),
                post(4, "黑五机场怎么选？把我对比的几家整理成了表格", "过路人", "情报", 65, 4800, "12分钟前", "Sakura"),
                post(5, "第一次跑长文本翻译，本地模型和 API 的取舍", "codemonkey", "技术", 33, 1500, "18分钟前", "阿杰", read = true),
                post(6, "深夜放毒：分享一个我常做的十分钟快手菜", "厨房杀手", "日常", 54, 1900, "26分钟前", "homelab_er", read = true),
                post(7, "关于最近 VPS 涨价，说说我的续费策略", "省钱达人", "情报", 19, 900, "34分钟前", "过路人", read = true),
            )
    }
}
