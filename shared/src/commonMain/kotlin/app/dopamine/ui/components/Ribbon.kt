package app.dopamine.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dopamine.ui.theme.fredoka
import kotlin.math.atan2

/** A soft curved band in the top corner with text running along it. Decorative only. */
@Composable
fun RibbonCorner(text: String, band: Color, ink: Color, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    val style = TextStyle(fontFamily = fredoka(), fontWeight = FontWeight.Bold, color = ink, fontSize = 17.sp)
    Canvas(modifier.size(width = 220.dp, height = 130.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(-30.dp.toPx(), 4.dp.toPx())
            cubicTo(w * 0.30f, h * 0.95f, w * 0.78f, h * 0.80f, w + 30.dp.toPx(), -14.dp.toPx())
        }
        drawPath(path, band, style = Stroke(width = 52.dp.toPx(), cap = StrokeCap.Round))
        // Text on a path: each letter is placed and rotated along the curve (Compose has no drawTextOnPath).
        val measure = PathMeasure().apply { setPath(path, false) }
        var d = 62.dp.toPx()
        for (ch in text) {
            val layout = measurer.measure(ch.toString(), style)
            val advance = layout.size.width.toFloat()
            if (d + advance > measure.length) break
            val mid = d + advance / 2
            val pos = measure.getPosition(mid)
            val tan = measure.getTangent(mid)
            val angle = atan2(tan.y, tan.x) * 180f / kotlin.math.PI.toFloat()
            translate(pos.x, pos.y) {
                rotate(angle, Offset.Zero) {
                    drawText(layout, topLeft = Offset(-advance / 2, 6.dp.toPx() - layout.firstBaseline))
                }
            }
            d += advance
        }
    }
}
