package app.dopamine.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.dopamine.R
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.PillShape

enum class Tab(val route: String, @DrawableRes val icon: Int, @StringRes val label: Int) {
    Home("home", R.drawable.ic_home, R.string.tab_home),
    Review("review", R.drawable.ic_review, R.string.tab_review),
    Settings("settings", R.drawable.ic_settings, R.string.tab_settings),
}

/** A dark pill that floats above the content. The selected tab shows its label on a yellow chip. */
@Composable
fun FloatingNav(selected: Tab?, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    val c = Dopamine.colors
    Row(
        modifier.clip(PillShape).background(c.onAccent).padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tab.entries.forEach { tab ->
            val on = tab == selected
            val tabLabel = stringResource(tab.label)
            Row(
                Modifier
                    .clip(PillShape)
                    .background(if (on) c.yellow else Color.Transparent)
                    .heightIn(min = 52.dp)
                    .semantics { if (!on) contentDescription = tabLabel }
                    .selectable(selected = on, role = Role.Tab, onClick = { onSelect(tab) })
                    .sizeIn(minWidth = 56.dp, minHeight = 52.dp)
                    .padding(horizontal = if (on) 18.dp else 14.dp, vertical = 6.dp)
                    .animateContentSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconImage(tab.icon, size = 32.dp, bob = false)
                if (on) {
                    Text(
                        stringResource(tab.label),
                        style = MaterialTheme.typography.labelLarge,
                        color = c.onAccent,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}
