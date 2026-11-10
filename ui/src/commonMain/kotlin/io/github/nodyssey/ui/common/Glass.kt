package io.github.nodyssey.ui.common

import androidx.compose.foundation.background
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource

/**
 * The blur the floating bars sample, shared between what scrolls and what sits over it.
 *
 * `null` on a screen that draws no glass — the strip and the bar both fall back to their plain
 * background rather than blurring nothing.
 */
val LocalHazeState = compositionLocalOf<HazeState?> { null }

/**
 * How far the bars' blur reaches behind them.
 *
 * Enough to lose the text of the row passing under, not so much that the bar reads as a smear: the
 * point is that the reader can tell something is moving under there, not to read it.
 */
private val GlassBlurRadius = 22.dp

/**
 * How much of the page colour the glass lays over its blur.
 *
 * The other half of the frosted look, and the one worth tuning: too high and the bar is a slab that
 * happens to be blurred, too low and the icons stop reading against a light row. It started at 0.74
 * and read as opaque, went to 0.42, and sits at 0.25 — a quarter of the page colour, so the blur is
 * what the bar mostly is.
 */
const val GLASS_TINT_ALPHA = 0.25f

/**
 * The blur recipe, shared by every glass surface so the strip and the bar are the same glass.
 *
 * Remembered at the call site that owns it: a new style instance per frame would rebuild the effect
 * every frame.
 */
val GlassBlurStyle: HazeBlurStyle
    get() = HazeBlurStyle { blurRadius(GlassBlurRadius) }

/**
 * Marks the scrolling content as what the bars blur.
 *
 * A no-op when nothing above it is glass, so a screen can wrap unconditionally.
 */
fun Modifier.glassSource(state: HazeState?): Modifier = if (state == null) this else hazeSource(state)

/**
 * Turns a bar into frosted glass: it blurs what passes behind it and lays a translucent sheet of
 * [tint] over the blur.
 *
 * The tint is what keeps the bar's own icons readable — a blur alone leaves the row's contrast at
 * the mercy of whatever heading happens to be under it.
 */
fun Modifier.glassBar(
    state: HazeState?,
    shape: Shape,
    tint: Color,
    style: HazeBlurStyle = GlassBlurStyle,
): Modifier =
    if (state == null) {
        this
    } else {
        clip(shape)
            .hazeBlur(input = HazeInput.Sources(state), style = style)
            .background(tint)
    }
