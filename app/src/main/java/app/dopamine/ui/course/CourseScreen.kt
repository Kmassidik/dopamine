package app.dopamine.ui.course

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dopamine.R
import app.dopamine.ui.components.AppIcon
import app.dopamine.ui.components.bounceClick
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.OutlineWidth

/** Step 3: the units and lessons of one course. Tap a lesson to play it. */
@Composable
fun CourseScreen(vm: CourseViewModel, onBack: () -> Unit, onOpenLesson: (String) -> Unit) {
    val manifest by vm.manifest.collectAsStateWithLifecycle()
    val info by vm.info.collectAsStateWithLifecycle()
    val c = Dopamine.colors
    val course = manifest?.courses?.firstOrNull { it.id == vm.courseId }
    val cat = manifest?.categories?.firstOrNull { it.id == course?.categoryId }
    val tint = when ((cat?.order ?: 1) % 3) {
        1 -> c.greenTint
        2 -> c.yellowTint
        else -> c.pinkTint
    }

    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).clickable(onClickLabel = stringResource(R.string.back), onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) { AppIcon(R.drawable.ic_back, c.ink, description = stringResource(R.string.back)) }
                Text(course?.title.orEmpty(), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp))
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                course?.units?.sortedBy { it.order }?.forEach { unit ->
                    item(key = "unit-${unit.id}") {
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(unit.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(
                                pluralStringResource(R.plurals.n_lessons, unit.lessons.size, unit.lessons.size),
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
                                    .bounceClick(lesson.title) { onOpenLesson(lesson.id) }
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.size(48.dp).clip(CircleShape).background(tint), contentAlignment = Alignment.Center) {
                                    Text(lesson.order.toString(), style = MaterialTheme.typography.titleMedium)
                                }
                                Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                                    Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                                    info[lesson.id]?.let {
                                        Text(
                                            stringResource(R.string.lesson_summary, pluralStringResource(R.plurals.n_questions, it.questions, it.questions), it.xp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = c.inkSoft,
                                        )
                                    }
                                }
                                AppIcon(R.drawable.ic_chevron, c.ink, size = 24.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
