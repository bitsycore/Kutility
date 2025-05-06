package sh.bitsy.app.kutility.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * Creates a Modifier that draws a repeatable two-color diagonal pattern behind the content.
 *
 * @param color1 The first color (background).
 * @param color2 The second color (stripes).
 * @param stripeWidth The visual width of each stripe.
 * @param angleDegrees The angle of the stripes in degrees. 45.0 means top-left to bottom-right. -45.0 (or 135.0) means top-right to bottom-left.
 */
fun Modifier.diagonalPattern(
    color1: Color,
    color2: Color,
    stripeWidth: Dp = 10.dp,
    angleDegrees: Double = 45.0 // Top-left to bottom-right
) = clip(RectangleShape).then(
    drawBehind {
        val stripeWidthPx = stripeWidth.toPx()
        drawRect(color = color1, size = size)
        drawDiagonalStripes(
            color = color2,
            stripeWidthPx = stripeWidthPx,
            angleDegrees = angleDegrees
        )
    }
)

private fun DrawScope.drawDiagonalStripes(
    color: Color,
    stripeWidthPx: Float,
    angleDegrees: Double
) {
    if (stripeWidthPx <= 0) return

    // For 45 degrees, the distance between line centers along x or y axis
    // for stripes of the SAME color is stripeWidthPx * 2 * sqrt(2)
    // The stroke width should be stripeWidthPx
    val step = stripeWidthPx * 2 * sqrt(2.0).toFloat()

    // Calculate line angle in radians
    // Note: Canvas 0 degrees is horizontal right. We adjust calculation based on desired visual angle.
    // For visual 45 deg (TL to BR), the line equation is y = -x + c => x + y = c
    // For visual -45 deg (TR to BL), the line equation is y = x + c => y - x = c
    val isTopLeftToBottomRight = (angleDegrees % 180.0) in 0.0..90.0 || (angleDegrees % 180.0) in -180.0..-90.0

    // Determine the range of constant 'c' needed to cover the canvas
    // The constant 'c' represents the value of (x+y) or (y-x) for a line
    val cMin: Float
    val cMax: Float
    if (isTopLeftToBottomRight) { // x + y = c
        cMin = 0f - step // Start slightly off-canvas top-left
        cMax = size.width + size.height + step // End slightly off-canvas bottom-right
    } else { // y - x = c
        cMin = -size.width - step // Start slightly off-canvas bottom-left
        cMax = size.height + step // End slightly off-canvas top-right
    }

    // Calculate number of lines needed
    val lineCount = ceil((cMax - cMin) / step).toInt()

    for (i in 0..lineCount) {
        val c = cMin + i * step

        // Calculate two points far outside the canvas bounds for the line
        // DrawLine will clip it correctly to the canvas draw area.
        val p1: Offset
        val p2: Offset
        val far = size.width + size.height // A distance guaranteed to be outside

        if (isTopLeftToBottomRight) { // x + y = c
            p1 = Offset(x = c - (-far), y = -far) // Point far top-left-ish
            p2 = Offset(x = c - (far), y = far)   // Point far bottom-right-ish
        } else { // y - x = c
            p1 = Offset(x = -far, y = c + (-far)) // Point far bottom-left-ish
            p2 = Offset(x = far, y = c + far)    // Point far top-right-ish
        }

        drawLine(
            color = color,
            start = p1,
            end = p2,
            strokeWidth = stripeWidthPx
        )
    }
}