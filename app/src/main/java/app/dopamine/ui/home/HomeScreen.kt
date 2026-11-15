package app.dopamine.ui.home

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dopamine.R
import app.dopamine.domain.model.CategoryDto
import app.dopamine.domain.model.Manifest
import app.dopamine.ui.components.AppIcon
import app.dopamine.ui.components.InkButton
import app.dopamine.ui.components.ProgressBar
import app.dopamine.ui.components.ProgressRing
import app.dopamine.ui.components.RibbonCorner
import app.dopamine.ui.components.StatChip
import app.dopamine.ui.components.iconFor
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.DopamineColors

@Composable
fun HomeScreen(name: String, vm: HomeViewModel, onOpenCategory: (String) -> Unit) {
    val manifest by vm.manifest.collectAsStateWithLifecycle()
    val update by vm.update.collectAsStateWithLifecycle()
    val lastUpdated by vm.lastUpdated.collectAsStateWithLifecycle()
    val c = Dopamine.colors
    val firstCategory = manifest?.categories?.minByOrNull { it.order }?.id

    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { Header(c, name) }
                item {
                    Bento(c, onStart = { firstCategory?.let(onOpenCategory) })
                }
                item {
                    Text(
                        stringResource(R.string.section_courses),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                val m = manifest
                if (m == null) {
                    item { Text(stringResource(R.string.loading), color = c.inkSoft) }
                } else {
                    items(m.categories.sortedBy { it.order }, key = { it.id }) { cat -> CourseRow(c, m, cat, onOpenCategory) }
                }
            }
            UpdateBar(c, update, lastUpdated, vm::startUpdate, vm::cancelUpdate, vm::dismissMessage)
        }
    }
}

@Composable
private fun Header(c: DopamineColors, name: String) {
    Box(Modifier.fillMaxWidth()) {
        RibbonCorner(stringResource(R.string.ribbon_text), c.greenTint, c.ink, Modifier.align(Alignment.TopEnd))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.clip(CircleShape).background(c.bg).heightIn(min = 48.dp).padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(c.ink), contentAlignment = Alignment.Center) {
                        AppIcon(R.drawable.ic_xp, c.yellow, size = 22.dp)
                    }
                    Text(
                        stringResource(R.string.home_title),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
                Box(Modifier.weight(1f))
                Box(
                    Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(c.pink)
                        .semantics { contentDescription = "" },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, color = c.onAccent)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.home_hello, name), style = MaterialTheme.typography.displayLarge)
                Text(stringResource(R.string.home_subtitle), style = MaterialTheme.typography.bodyLarge, color = c.inkSoft)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip(R.drawable.ic_streak, "0", c.pinkText, c.pinkTint, c.ink, stringResource(R.string.streak_desc, 0))
                StatChip(R.drawable.ic_xp, "0", c.yellowText, c.yellowTint, c.ink, stringResource(R.string.xp_desc, 0))
            }
        }
    }
}

@Composable
private fun Tile(bg: Color, modifier: Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .clip(CardShape)
            .background(bg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content,
    )
}

@Composable
private fun Badge(c: DopamineColors, icon: Int, fill: Color = Color.White) {
    Box(Modifier.size(44.dp).clip(CircleShape).background(fill), contentAlignment = Alignment.Center) {
        AppIcon(icon, c.onAccent, size = 22.dp)
    }
}

@Composable
private fun Bento(c: DopamineColors, onStart: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Tile(
            c.green,
            Modifier.weight(1f).fillMaxHeight().heightIn(min = 220.dp).drawBehind {
                drawCircle(Color.White.copy(alpha = 0.22f), 70.dp.toPx(), Offset(size.width, size.height * 0.25f))
                drawCircle(Color.White.copy(alpha = 0.16f), 44.dp.toPx(), Offset(10.dp.toPx(), size.height * 0.55f))
            },
            onClick = onStart,
        ) {
            Badge(c, R.drawable.ic_book)
            Box(Modifier.weight(1f))
            Text(stringResource(R.string.tile_start_title), style = MaterialTheme.typography.titleLarge, color = c.onAccent)
            Text(stringResource(R.string.tile_start_sub), style = MaterialTheme.typography.bodyMedium, color = c.onAccent)
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Tile(c.yellow, Modifier.weight(1f).fillMaxWidth()) {
                Badge(c, R.drawable.ic_review)
                Box(Modifier.height(10.dp))
                Text(stringResource(R.string.tile_review_title), style = MaterialTheme.typography.titleMedium, color = c.onAccent)
                Text(stringResource(R.string.tile_review_sub), style = MaterialTheme.typography.bodyMedium, color = c.onAccent)
            }
            Tile(c.onAccent, Modifier.weight(1f).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.tile_goal_title), style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(stringResource(R.string.goal_progress, 0, 20), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                }
                Box(Modifier.height(10.dp))
                ProgressBar(0f, Color.White.copy(alpha = 0.25f), c.yellow, height = 10.dp)
            }
        }
    }
}

@Composable
private fun CourseRow(c: DopamineColors, m: Manifest, cat: CategoryDto, onOpen: (String) -> Unit) {
    val (tint, fill) = when (cat.order % 3) {
        1 -> c.greenTint to c.green
        2 -> c.yellowTint to c.yellow
        else -> c.pinkTint to c.pink
    }
    val courses = m.courses.count { it.categoryId == cat.id }
    val lessons = m.courses.filter { it.categoryId == cat.id }.flatMap { it.units }.sumOf { it.lessons.size }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(tint)
            .clickable(onClickLabel = cat.name) { onOpen(cat.id) }
            .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(fill), contentAlignment = Alignment.Center) {
            AppIcon(iconFor(cat.icon), c.onAccent, size = 28.dp)
        }
        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(cat.name, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(
                    R.string.category_summary,
                    pluralStringResource(R.plurals.n_courses, courses, courses),
                    pluralStringResource(R.plurals.n_lessons, lessons, lessons),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = c.inkSoft,
            )
        }
        AppIcon(R.drawable.ic_chevron, c.ink, size = 24.dp)
    }
}

@Composable
private fun UpdateBar(
    c: DopamineColors,
    state: UpdateUi,
    lastUpdated: Long,
    onUpdate: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().background(c.bg).padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when (state) {
            is UpdateUi.Running -> {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (state.total == 0) stringResource(R.string.update_checking)
                        else stringResource(R.string.update_running, state.done, state.total),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = onCancel, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(R.string.update_cancel), color = c.pinkText)
                    }
                }
                ProgressBar(if (state.total == 0) 0f else state.done.toFloat() / state.total, c.greenTint, c.green)
            }
            is UpdateUi.Done -> Message(c.greenTint, c.greenText, R.drawable.ic_check, state.message, onDismiss)
            is UpdateUi.Failed -> Message(c.pinkTint, c.pinkText, R.drawable.ic_warning, state.message, onDismiss)
            UpdateUi.Idle -> Unit
        }
        if (state !is UpdateUi.Running) {
            InkButton(
                text = stringResource(R.string.update_idle),
                onClick = onUpdate,
                container = c.onAccent,
                content = Color.White,
                leadingIcon = R.drawable.ic_download,
            )
            Text(
                if (lastUpdated == 0L) stringResource(R.string.update_bundled)
                else stringResource(
                    R.string.update_last,
                    DateUtils.getRelativeTimeSpanString(lastUpdated, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = c.inkSoft,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun Message(bg: Color, fg: Color, icon: Int, text: String, onDismiss: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(CardShape).background(bg).padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(icon, fg, size = 24.dp)
        Text(text, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 8.dp))
        TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.update_dismiss), color = fg)
        }
    }
}
