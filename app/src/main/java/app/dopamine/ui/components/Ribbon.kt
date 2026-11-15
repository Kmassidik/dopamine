package app.dopamine.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import app.dopamine.R

/** A soft curved band in the top corner with text running along it. Decorative only. */
@Composable
fun RibbonCorner(text: String, band: Color, ink: Color, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val face = remember { ResourcesCompat.getFont(context, R.font.zen_maru_bold) }
    Canvas(modifier.size(width = 220.dp, height = 130.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(-30.dp.toPx(), 4.dp.toPx())
            cubicTo(w * 0.30f, h * 0.95f, w * 0.78f, h * 0.80f, w + 30.dp.toPx(), -14.dp.toPx())
        }
        drawPath(path, band, style = Stroke(width = 52.dp.toPx(), cap = StrokeCap.Round))
        drawIntoCanvas { canvas ->
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = ink.toArgb()
                textSize = 17.dp.toPx()
                typeface = face
            }
            canvas.nativeCanvas.drawTextOnPath(text, path.asAndroidPath(), 112.dp.toPx(), 6.dp.toPx(), paint)
        }
    }
}
