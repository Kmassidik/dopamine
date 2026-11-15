package app.dopamine.ui.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dopamine.R
import app.dopamine.ui.components.AppIcon
import app.dopamine.ui.components.iconFor
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.OutlineWidth

@Composable
fun CategoryScreen(vm: CategoryViewModel, onBack: () -> Unit) {
    val manifest by vm.manifest.collectAsStateWithLifecycle()
    val info by vm.info.collectAsStateWithLifecycle()
    val c = Dopamine.colors
    val cat = manifest?.categories?.firstOrNull { it.id == vm.categoryId }
    val courses = manifest?.courses?.filter { it.categoryId == vm.categoryId }?.sortedBy { it.order }.orEmpty()
    val accent = when ((cat?.order ?: 1) % 3) {
        1 -> listOf(c.greenTint, c.green)
        2 -> listOf(c.yellowTint, c.yellow)
        else -> listOf(c.pinkTint, c.pink)
    }

    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).clickable(onClickLabel = stringResource(R.string.back), onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) { AppIcon(R.drawable.ic_back, c.ink, description = stringResource(R.string.back)) }
                Text(cat?.name.orEmpty(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp))
            }
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                courses.forEach { course ->
                    item(key = "course-${course.id}") {
                        Row(
                            Modifier.fillMaxWidth().clip(CardShape).background(accent[0]).padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(Modifier.size(48.dp).clip(CircleShape).background(accent[1]), contentAlignment = Alignment.Center) {
                                AppIcon(iconFor(cat?.icon.orEmpty()), c.onAccent, size = 24.dp)
                            }
                            Column(Modifier.padding(start = 16.dp)) {
                                Text(course.title, style = MaterialTheme.typography.titleLarge)
                                if (course.description.isNotBlank()) {
                                    Text(course.description, style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
                                }
                            }
                        }
                    }
                    course.units.sortedBy { it.order }.forEach { unit ->
                        item(key = "unit-${unit.id}") {
                            Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(unit.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                Text(
                                    stringResource(R.string.unit_lessons, unit.lessons.size),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = c.inkSoft,
                                )
                            }
                        }
                        unit.lessons.sortedBy { it.order }.forEach { lesson ->
                            item(key = "lesson-${lesson.id}") {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(CardShape)
                                        .border(OutlineWidth, c.line, CardShape)
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(Modifier.size(48.dp).clip(CircleShape).background(accent[0]), contentAlignment = Alignment.Center) {
                                        Text(lesson.order.toString(), style = MaterialTheme.typography.titleMedium)
                                    }
                                    Column(Modifier.padding(start = 16.dp)) {
                                        Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                                        info[lesson.id]?.let {
                                            Text(
                                                stringResource(R.string.lesson_summary, it.questions, it.xp),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = c.inkSoft,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
