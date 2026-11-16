package app.dopamine.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dopamine.shared.resources.*
import app.dopamine.data.repo.HomeStats
import app.dopamine.domain.model.CategoryDto
import app.dopamine.domain.progress.goalFraction
import app.dopamine.domain.model.Manifest
import app.dopamine.ui.components.AppIcon
import app.dopamine.ui.components.IconImage
import app.dopamine.ui.components.ProgressBar
import app.dopamine.ui.components.bounceClick
import app.dopamine.ui.components.heading
import app.dopamine.ui.components.RibbonCorner
import app.dopamine.ui.components.StatChip
import app.dopamine.ui.components.iconFor
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.DopamineColors

@Composable
fun HomeScreen(name: String, vm: HomeViewModel, onOpenCategory: (String) -> Unit, onOpenSettings: () -> Unit) {
    val manifest by vm.manifest.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val c = Dopamine.colors
    val firstCategory = manifest?.categories?.minByOrNull { it.order }?.id

    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item { Header(c, name, stats, onOpenSettings) }
                item {
                    Bento(c, stats, onStart = { firstCategory?.let(onOpenCategory) })
                }
                item {
                    Text(
                        stringResource(Res.string.section_courses),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 4.dp).heading(),
                    )
                }
                val m = manifest
                if (m == null) {
                    item { Text(stringResource(Res.string.loading), color = c.inkSoft) }
                } else {
                    items(m.categories.sortedBy { it.order }, key = { it.id }) { cat -> CourseRow(c, m, cat, onOpenCategory) }
                }
            }
        }
    }
}

@Composable
private fun Header(c: DopamineColors, name: String, stats: HomeStats, onOpenSettings: () -> Unit) {
    val settingsLabel = stringResource(Res.string.settings_open)
    Box(Modifier.fillMaxWidth()) {
        RibbonCorner(stringResource(Res.string.ribbon_text), c.greenTint, c.ink, Modifier.align(Alignment.TopEnd).offset(y = (-36).dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.clip(CircleShape).heightIn(min = 48.dp).padding(end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(c.yellow), contentAlignment = Alignment.Center) {
                        IconImage(Res.drawable.mascot_happy, size = 36.dp, bob = false)
                    }
                    Text(
                        stringResource(Res.string.home_title),
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
                        .semantics { contentDescription = settingsLabel }
                        .bounceClick(role = Role.Button, onClick = onOpenSettings),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, color = c.onAccent, modifier = Modifier.clearAndSetSemantics {})
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(Res.string.home_hello, name), style = MaterialTheme.typography.displayLarge, modifier = Modifier.heading())
                Text(stringResource(Res.string.home_subtitle), style = MaterialTheme.typography.bodyLarge, color = c.inkSoft)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatChip(Res.drawable.ic_streak, stats.streak.toString(), c.pinkTint, c.ink, stringResource(Res.string.streak_desc, stats.streak))
                StatChip(Res.drawable.ic_xp, stats.totalXp.toString(), c.yellowTint, c.ink, stringResource(Res.string.xp_desc, stats.totalXp))
            }
        }
    }
}

@Composable
private fun Tile(bg: Color, modifier: Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val openLabel = stringResource(Res.string.action_open)
    Column(
        modifier
            .clip(CardShape)
            .background(bg)
            .then(if (onClick != null) Modifier.bounceClick(openLabel, onClick = onClick) else Modifier)
            .padding(20.dp),
        content = content,
    )
}

@Composable
private fun Bento(c: DopamineColors, stats: HomeStats, onStart: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Tile(
            c.green,
            Modifier.weight(1f).fillMaxHeight().heightIn(min = 220.dp).drawBehind {
                drawCircle(Color.White.copy(alpha = 0.22f), 70.dp.toPx(), Offset(size.width, size.height * 0.25f))
                drawCircle(Color.White.copy(alpha = 0.16f), 44.dp.toPx(), Offset(10.dp.toPx(), size.height * 0.55f))
            },
            onClick = onStart,
        ) {
            IconImage(Res.drawable.mascot_study, size = 112.dp)
            Box(Modifier.weight(1f))
            Text(stringResource(Res.string.tile_start_title), style = MaterialTheme.typography.titleLarge, color = c.onAccent)
            Text(stringResource(Res.string.tile_start_sub), style = MaterialTheme.typography.bodyMedium, color = c.onAccent)
        }
        Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Tile(c.yellow, Modifier.weight(1f).fillMaxWidth()) {
                IconImage(Res.drawable.ic_review, size = 48.dp)
                Text(stringResource(Res.string.tile_review_title), style = MaterialTheme.typography.titleMedium, color = c.onAccent)
                Text(stringResource(Res.string.tile_review_sub), style = MaterialTheme.typography.bodyMedium, color = c.onAccent)
            }
            Tile(c.onAccent, Modifier.weight(1f).fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(Res.string.tile_goal_title), style = MaterialTheme.typography.titleMedium, color = Color.White)
                        Text(stringResource(Res.string.goal_progress, stats.todayXp, stats.goalXp), style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    }
                    IconImage(Res.drawable.ic_goal, size = 40.dp)
                }
                Box(Modifier.height(10.dp))
                ProgressBar(goalFraction(stats.todayXp, stats.goalXp), Color.White.copy(alpha = 0.25f), c.yellow, height = 10.dp)
            }
        }
    }
}

@Composable
private fun CourseRow(c: DopamineColors, m: Manifest, cat: CategoryDto, onOpen: (String) -> Unit) {
    val tint = when (cat.order % 3) {
        1 -> c.greenTint
        2 -> c.yellowTint
        else -> c.pinkTint
    }
    val courses = m.courses.count { it.categoryId == cat.id }
    val lessons = m.courses.filter { it.categoryId == cat.id }.flatMap { it.units }.sumOf { it.lessons.size }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(tint)
            .bounceClick(stringResource(Res.string.action_open)) { onOpen(cat.id) }
            .padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(56.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
            IconImage(iconFor(cat.icon), size = 44.dp)
        }
        Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
            Text(cat.name, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(
                    Res.string.category_summary,
                    pluralStringResource(Res.plurals.n_courses, courses, courses),
                    pluralStringResource(Res.plurals.n_lessons, lessons, lessons),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = c.inkSoft,
            )
        }
        AppIcon(Res.drawable.ic_chevron, c.ink, size = 24.dp)
    }
}
