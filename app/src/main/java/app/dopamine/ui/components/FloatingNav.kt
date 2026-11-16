package app.dopamine.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.dopamine.R
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.PillShape

enum class Tab(val route: String, @DrawableRes val icon: Int, @StringRes val label: Int) {
    Home("home", R.drawable.ic_home, R.string.tab_home),
    Review("review", R.drawable.ic_review, R.string.tab_review),
    Settings("settings", R.drawable.ic_settings, R.string.tab_settings),
}

/**
 * A dark pill that floats above the content. Three equal tabs, each with its icon and its label;
 * the selected one sits on a yellow chip. Each tab is a full third of the bar to tap.
 */
@Composable
fun FloatingNav(selected: Tab?, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    val c = Dopamine.colors
    Row(
        modifier.widthIn(max = 420.dp).fillMaxWidth().clip(PillShape).background(c.onAccent).padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tab.entries.forEach { tab ->
            val on = tab == selected
            Column(
                Modifier
                    .weight(1f)
                    .clip(PillShape)
                    .background(if (on) c.yellow else Color.Transparent)
                    .selectable(selected = on, role = Role.Tab, onClick = { onSelect(tab) })
                    .heightIn(min = 56.dp)
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                IconImage(tab.icon, size = 28.dp, bob = false)
                Text(
                    stringResource(tab.label),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (on) c.onAccent else Color.White.copy(alpha = 0.88f),
                )
            }
        }
    }
}
