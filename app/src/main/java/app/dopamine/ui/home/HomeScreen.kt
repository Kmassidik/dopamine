package app.dopamine.ui.home

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import app.dopamine.domain.content.ManifestDiff
import app.dopamine.domain.model.CategoryDto
import app.dopamine.domain.model.Manifest
import app.dopamine.ui.components.AppIcon
import app.dopamine.ui.components.ChunkyButton
import app.dopamine.ui.components.ProgressBar
import app.dopamine.ui.components.ProgressRing
import app.dopamine.ui.components.StatChip
import app.dopamine.ui.components.iconFor
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.DopamineColors

@Composable
fun HomeScreen(vm: HomeViewModel, onOpenCategory: (String) -> Unit) {
    val manifest by vm.manifest.collectAsStateWithLifecycle()
    val update by vm.update.collectAsStateWithLifecycle()
    val lastUpdated by vm.lastUpdated.collectAsStateWithLifecycle()
    val c = Dopamine.colors

    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { TopRow(c) }
                item {
                    GoalBanner(c) { manifest?.categories?.minByOrNull { it.order }?.let { onOpenCategory(it.id) } }
                }
                item { ReviewRow(c) }
                item {
                    Text(
                        stringResource(R.string.section_courses),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                val m = manifest
                if (m == null) {
                    item { Text(stringResource(R.string.loading), color = c.inkSoft) }
                } else {
                    items(m.categories.sortedBy { it.order }, key = { it.id }) { cat ->
                        CategoryCard(c, m, cat, onOpenCategory)
                    }
                }
            }
            UpdateBar(c, update, lastUpdated, vm::startUpdate, vm::cancelUpdate, vm::dismissMessage)
        }
    }
}

@Composable
private fun TopRow(c: DopamineColors) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.home_title), style = MaterialTheme.typography.displayLarge, modifier = Modifier.weight(1f))
        StatChip(R.drawable.ic_streak, "0", c.pinkText, c.pinkTint, c.ink, stringResource(R.string.streak_desc, 0))
        Box(Modifier.width(8.dp))
        StatChip(R.drawable.ic_xp, "0", c.yellowText, c.yellowTint, c.ink, stringResource(R.string.xp_desc, 0))
    }
}

@Composable
private fun GoalBanner(c: DopamineColors, onPick: () -> Unit) {
    val goal = 20
    Column(
        Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(c.yellow)
            .drawBehind {
                drawCircle(c.pink, radius = 70.dp.toPx(), center = Offset(size.width - 8.dp.toPx(), -10.dp.toPx()))
                drawCircle(c.green, radius = 36.dp.toPx(), center = Offset(size.width - 4.dp.toPx(), size.height - 30.dp.toPx()))
                drawCircle(Color.White.copy(alpha = 0.35f), radius = 90.dp.toPx(), center = Offset(-20.dp.toPx(), size.height))
            }
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.goal_label), style = MaterialTheme.typography.labelMedium, color = c.onAccent)
                Text(stringResource(R.string.goal_progress, 0, goal), style = MaterialTheme.typography.titleLarge, color = c.onAccent)
                Text(stringResource(R.string.goal_hint), style = MaterialTheme.typography.bodyMedium, color = c.onAccent)
            }
            Box(Modifier.width(12.dp))
            Box(contentAlignment = Alignment.Center) {
                ProgressRing(0f, 72.dp, 10.dp, Color.White.copy(alpha = 0.7f), c.onAccent)
                AppIcon(R.drawable.ic_xp, c.onAccent, size = 28.dp)
            }
        }
        ChunkyButton(
            text = stringResource(R.string.goal_action),
            onClick = onPick,
            container = c.onAccent,
            lip = Color.Black.copy(alpha = 0.45f),
            content = c.yellow,
        )
    }
}

@Composable
private fun ReviewRow(c: DopamineColors) {
    Row(
        Modifier.fillMaxWidth().clip(CardShape).background(c.pinkTint).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(c.pink), contentAlignment = Alignment.Center) {
            AppIcon(R.drawable.ic_review, c.onAccent, size = 24.dp)
        }
        Column(Modifier.padding(start = 16.dp)) {
            Text(stringResource(R.string.review_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.review_empty), style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
        }
    }
}

@Composable
private fun CategoryCard(c: DopamineColors, m: Manifest, cat: CategoryDto, onOpen: (String) -> Unit) {
    val (tint, fill, lip, text) = when (cat.order % 3) {
        1 -> listOf(c.greenTint, c.green, c.greenLip, c.greenText)
        2 -> listOf(c.yellowTint, c.yellow, c.yellowLip, c.yellowText)
        else -> listOf(c.pinkTint, c.pink, c.pinkLip, c.pinkText)
    }
    val courses = m.courses.filter { it.categoryId == cat.id }
    val lessons = courses.flatMap { it.units }.sumOf { it.lessons.size }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(tint)
            .clickable(onClickLabel = cat.name) { onOpen(cat.id) }
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(fill), contentAlignment = Alignment.Center) {
                AppIcon(iconFor(cat.icon), c.onAccent, size = 28.dp)
            }
            Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                Text(cat.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.category_summary, pluralStringResource(R.plurals.n_courses, courses.size, courses.size), pluralStringResource(R.plurals.n_lessons, lessons, lessons)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.inkSoft,
                )
            }
            AppIcon(R.drawable.ic_chevron, text, size = 24.dp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ProgressBar(0f, c.bg.copy(alpha = 0.8f), fill)
            Text(
                stringResource(R.string.category_progress, 0, lessons),
                style = MaterialTheme.typography.labelSmall,
                color = text,
            )
        }
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
                ProgressBar(
                    if (state.total == 0) 0f else state.done.toFloat() / state.total,
                    c.greenTint, c.green,
                )
            }
            is UpdateUi.Done -> Message(c.greenTint, c.greenText, R.drawable.ic_check, state.message, onDismiss)
            is UpdateUi.Failed -> Message(c.pinkTint, c.pinkText, R.drawable.ic_warning, state.message, onDismiss)
            UpdateUi.Idle -> Unit
        }
        if (state !is UpdateUi.Running) {
            ChunkyButton(
                text = stringResource(R.string.update_idle),
                onClick = onUpdate,
                container = c.green,
                lip = c.greenLip,
                content = c.onAccent,
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
