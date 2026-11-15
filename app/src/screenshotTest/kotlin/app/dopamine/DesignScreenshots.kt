package app.dopamine

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import app.dopamine.ui.components.ChunkyButton
import app.dopamine.ui.components.FloatingNav
import app.dopamine.ui.components.IconImage
import app.dopamine.ui.components.InkButton
import app.dopamine.ui.components.ProgressBar
import app.dopamine.ui.components.StatChip
import app.dopamine.ui.components.Tab
import app.dopamine.ui.loading.LoadingScreen
import app.dopamine.ui.math.MathText
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.DopamineTheme

/*
 * The design guard. These screenshots are compared pixel by pixel with the reference images in
 * app/src/screenshotTestDefaultDebug/reference. If a change makes the app look different from the
 * design on purpose, run the update task (see TESTING.md) and commit the new references with a
 * note on why. If it was not on purpose, the check fails: that is the point.
 */

@Composable
private fun Page(content: @Composable () -> Unit) {
    DopamineTheme(darkTheme = false) {
        Box(Modifier.background(Color.White).padding(20.dp)) { content() }
    }
}

@Composable
private fun Swatch(name: String, color: Color, onColor: Color = Color.White) {
    Box(Modifier.size(width = 104.dp, height = 56.dp).clip(MaterialTheme.shapes.medium).background(color).padding(8.dp)) {
        Text(name, style = MaterialTheme.typography.labelSmall, color = onColor)
    }
}

@PreviewTest
@Preview(name = "palette", showBackground = true, widthDp = 400, heightDp = 420)
@Composable
fun PaletteShot() = Page {
    val c = Dopamine.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Swatch("green", c.green, c.onAccent); Swatch("tint", c.greenTint, c.ink); Swatch("lip", c.greenLip)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Swatch("yellow", c.yellow, c.onAccent); Swatch("tint", c.yellowTint, c.ink); Swatch("lip", c.yellowLip)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Swatch("pink", c.pink, c.onAccent); Swatch("tint", c.pinkTint, c.ink); Swatch("lip", c.pinkLip)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Swatch("ink", c.ink); Swatch("ink soft", c.inkSoft); Swatch("line", c.line, c.ink)
        }
    }
}

@PreviewTest
@Preview(name = "type-scale", showBackground = true, widthDp = 400, heightDp = 560)
@Composable
fun TypeScaleShot() = Page {
    val t = MaterialTheme.typography
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Hello, Kurnia", style = t.displayLarge)
        Text("Display medium 32", style = t.displayMedium)
        Text("Headline 28", style = t.headlineMedium)
        Text("Title large 24", style = t.titleLarge)
        Text("Title medium 18", style = t.titleMedium)
        Text("Title small 16", style = t.titleSmall)
        Text("Body large 16. A little every day.", style = t.bodyLarge)
        Text("Body medium 14. Calm and easy to read.", style = t.bodyMedium)
        Text("Button label 17", style = t.labelLarge)
        Text("Label medium 13", style = t.labelMedium)
        Text("Label small 12", style = t.labelSmall)
    }
}

@PreviewTest
@Preview(name = "buttons-and-chips", showBackground = true, widthDp = 400, heightDp = 520)
@Composable
fun ButtonsShot() = Page {
    val c = Dopamine.colors
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ChunkyButton("Check", {}, c.green, c.greenLip, c.onAccent)
        ChunkyButton("Got it", {}, c.yellow, c.yellowLip, c.onAccent)
        ChunkyButton("Continue", {}, c.pink, c.pinkLip, c.onAccent)
        ChunkyButton("Check (disabled)", {}, c.green, c.greenLip, c.onAccent, enabled = false)
        InkButton("Change name", {}, c.onAccent, Color.White)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatChip(app.dopamine.R.drawable.ic_streak, "3", c.pinkTint, c.ink, "Streak")
            StatChip(app.dopamine.R.drawable.ic_xp, "120", c.yellowTint, c.ink, "XP")
        }
        ProgressBar(0.6f, c.greenTint, c.green)
    }
}

@PreviewTest
@Preview(name = "floating-nav", showBackground = true, widthDp = 400, heightDp = 300)
@Composable
fun NavShot() = Page {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Tab.entries.forEach { FloatingNav(it, {}) }
    }
}

@PreviewTest
@Preview(name = "loading", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun LoadingShot() = DopamineTheme(darkTheme = false) { LoadingScreen() }

@PreviewTest
@Preview(name = "math", showBackground = true, widthDp = 400, heightDp = 520)
@Composable
fun MathShot() = Page {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        MathText("Solve \$x+3=7\$ now", style = MaterialTheme.typography.titleLarge)
        MathText("\$\\frac{a}{b}+\\frac{c}{b}=\\frac{a+c}{b}\$", style = MaterialTheme.typography.titleLarge)
        MathText("\$x=\\frac{-b\\pm\\sqrt{b^2-4ac}}{2a}\$", style = MaterialTheme.typography.titleLarge)
        MathText("\$a_n=a_1+(n-1)d\$ and \$10^{-3}\$", style = MaterialTheme.typography.titleLarge)
        MathText("Area is \$\\pi r^2\$, water is \$\\text{H}_2\\text{O}\$", style = MaterialTheme.typography.titleMedium)
    }
}

/** The hard case from TESTING.md: a small phone with large text. Nothing may be cut off. */
@PreviewTest
@Preview(name = "small-phone-large-font", showBackground = true, widthDp = 320, heightDp = 640, fontScale = 1.3f)
@Composable
fun SmallPhoneShot() = Page {
    val c = Dopamine.colors
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Hello, Kurnia", style = MaterialTheme.typography.displayLarge)
        Text("What will you learn today?", style = MaterialTheme.typography.bodyLarge, color = c.inkSoft)
        ChunkyButton("Start course", {}, c.green, c.greenLip, c.onAccent)
        MathText("Solve \$2x+3=7\$ for \$x\$", style = MaterialTheme.typography.titleLarge)
        FloatingNav(Tab.Home, {}, Modifier.fillMaxWidth())
    }
}
