package app.dopamine.ui.math

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import app.dopamine.shared.resources.*
import app.dopamine.ui.components.BackButton
import app.dopamine.ui.components.AppIcon
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine

private val FORMULAS = listOf(
    "Solve \$x+3=7\$ now",
    "\$2x+3=7\$",
    "\$\\frac{a}{b}+\\frac{c}{b}=\\frac{a+c}{b}\$",
    "\$\\frac{1}{2}+\\frac{1}{3}=\\frac{5}{6}\$",
    "\$x^2+y^2=z^2\$",
    "\$x^{10}\$ and \$10^{-3}\$",
    "\$\\sqrt{x}\$",
    "\$\\sqrt{b^2-4ac}\$",
    "\$x=\\frac{-b\\pm\\sqrt{b^2-4ac}}{2a}\$",
    "\$\\sqrt[3]{27}=3\$",
    "\$a_n=a_1+(n-1)d\$",
    "\$|x|\$ is the distance from zero",
    "Area is \$\\pi r^2\$",
    "\$E=mc^2\$",
    "\$\\frac{x^2+1}{x-1}\$",
    "\$\\frac{\\frac{a}{b}}{c}\$",
    "\$\\left(\\frac{a}{b}\\right)^2\$",
    "\$a\\cdot b\$ and \$3\\times 4\$",
    "\$x\\le 5\$ and \$y\\ge -2\$",
    "\$\\alpha+\\beta=\\gamma\$",
    "\$\\sin\\theta\$ and \$\\cos\\theta\$",
    "\$H_2O\$ and \$CO_2\$",
    "\$3.14\\times 2\$",
    "Speed is \$60\\ \\text{km/h}\$",
    "Broken: \$\\frac{1}\$ and \$\\foo{bar}\$ and \$\\sqrt{\$",
    "An unclosed dollar \$x+1",
)

/** Dev screen: every formula we care about, so layout problems are easy to spot. Long-press the version in Settings. */
@Composable
fun FormulaTestScreen(onBack: () -> Unit) {
    val c = Dopamine.colors
    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                BackButton(stringResource(Res.string.back), onBack)
                Text("Formula test", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp))
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(FORMULAS) { f ->
                    Column(Modifier.fillMaxWidth().clip(CardShape).background(c.greenTint).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        MathText(f, style = MaterialTheme.typography.titleLarge)
                        Text(f, style = MaterialTheme.typography.labelSmall, color = c.inkSoft)
                    }
                }
            }
        }
    }
}
