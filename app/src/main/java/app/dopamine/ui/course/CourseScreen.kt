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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dopamine.R
import app.dopamine.domain.content.ContentContract
import app.dopamine.domain.progress.QUIZ_MAX_QUESTIONS
import app.dopamine.domain.progress.QUIZ_MIN_QUESTIONS
import app.dopamine.domain.progress.QUIZ_PASS_PERCENT
import app.dopamine.ui.components.AppIcon
import app.dopamine.ui.components.ChunkyButton
import app.dopamine.ui.components.IconImage
import app.dopamine.ui.components.bounceClick
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.DopamineColors
import app.dopamine.ui.theme.OutlineWidth

/** Step 3: the units and lessons of one course. Tap a lesson to play it. */
@Composable
fun CourseScreen(vm: CourseViewModel, onBack: () -> Unit, onOpenLesson: (String) -> Unit, onOpenNotes: (String) -> Unit, onOpenQuiz: (String) -> Unit) {
    val manifest by vm.manifest.collectAsStateWithLifecycle()
    val info by vm.info.collectAsStateWithLifecycle()
    val done by vm.done.collectAsStateWithLifecycle()
    val passedQuizzes by vm.passedQuizzes.collectAsStateWithLifecycle()
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
                course?.overview?.let { ov ->
                    item(key = "overview") {
                        val ordered = course.units.sortedBy { it.order }.flatMap { u -> u.lessons.sortedBy { it.order } }
                        val playable = ordered.filter { !ContentContract.isLocked(it.minApp) }
                        val next = playable.firstOrNull { it.id !in done }
                        val label = when {
                            ordered.isEmpty() || done.isEmpty() -> R.string.course_start
                            next != null -> R.string.course_continue
                            else -> R.string.course_review
                        }
                        Overview(c, tint, ov, label, onStart = { (next ?: playable.firstOrNull())?.let { onOpenLesson(it.id) } })
                    }
                }
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
                    if (unit.notes != null && !ContentContract.isLocked(unit.notes.minApp)) {
                        item(key = "notes-${unit.id}") {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape)
                                    .background(c.yellowTint)
                                    .bounceClick(stringResource(R.string.notes_open)) { onOpenNotes(unit.id) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconImage(R.drawable.ic_dictionary, size = 32.dp, bob = false)
                                Text(
                                    stringResource(R.string.notes_open),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                                )
                                AppIcon(R.drawable.ic_chevron, c.ink, size = 24.dp)
                            }
                        }
                    }
                    unit.lessons.sortedBy { it.order }.forEach { lesson ->
                        val locked = ContentContract.isLocked(lesson.minApp)
                        item(key = "lesson-${lesson.id}") {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(CardShape)
                                    .border(OutlineWidth, c.line, CardShape)
                                    .then(if (locked) Modifier else Modifier.bounceClick(lesson.title) { onOpenLesson(lesson.id) })
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(Modifier.size(48.dp).clip(CircleShape).background(tint), contentAlignment = Alignment.Center) {
                                    if (locked) {
                                        IconImage(R.drawable.ic_locked, size = 32.dp, description = stringResource(R.string.lesson_locked), bob = false)
                                    } else if (lesson.id in done) {
                                        IconImage(R.drawable.ic_complete, size = 32.dp, description = stringResource(R.string.lesson_completed), bob = false)
                                    } else {
                                        Text(lesson.order.toString(), style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                                Column(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                                    Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                                    if (locked) {
                                        Text(stringResource(R.string.lesson_locked), style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
                                    }
                                    info[lesson.id]?.takeIf { !locked }?.let {
                                        Text(
                                            stringResource(R.string.lesson_summary, pluralStringResource(R.plurals.n_questions, it.questions, it.questions), it.xp),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = c.inkSoft,
                                        )
                                    }
                                }
                                if (!locked) AppIcon(R.drawable.ic_chevron, c.ink, size = 24.dp)
                            }
                        }
                    }
                    val poolSize = unit.lessons.filter { !ContentContract.isLocked(it.minApp) }.sumOf { info[it.id]?.questions ?: 0 }
                    if (poolSize >= QUIZ_MIN_QUESTIONS) {
                        item(key = "quiz-${unit.id}") {
                            val passed = unit.id in passedQuizzes
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(CircleShape)
                                    .background(c.pinkTint)
                                    .bounceClick(stringResource(R.string.unit_quiz)) { onOpenQuiz(unit.id) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconImage(if (passed) R.drawable.ic_complete else R.drawable.ic_trophy, size = 32.dp, bob = false)
                                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                    Text(stringResource(R.string.unit_quiz), style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        stringResource(if (passed) R.string.unit_quiz_passed else R.string.unit_quiz_sub, minOf(poolSize, QUIZ_MAX_QUESTIONS), QUIZ_PASS_PERCENT),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = c.inkSoft,
                                    )
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

@Composable
private fun Overview(c: DopamineColors, tint: Color, ov: app.dopamine.domain.model.Overview, @androidx.annotation.StringRes startLabel: Int, onStart: () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(CardShape).background(tint).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (ov.summary.isNotBlank()) Text(ov.summary, style = MaterialTheme.typography.bodyLarge)
        if (ov.minutes > 0) {
            Text(
                stringResource(R.string.course_minutes, ov.minutes),
                style = MaterialTheme.typography.labelMedium,
                color = c.ink,
                modifier = Modifier.clip(CircleShape).background(Color.White).padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }
        if (ov.outcomes.isNotEmpty()) {
            Text(stringResource(R.string.course_learn), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
            ov.outcomes.forEach { Bullet(c, it, R.drawable.ic_correct) }
        }
        if (ov.requirements.isNotEmpty()) {
            Text(stringResource(R.string.course_before), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
            ov.requirements.forEach { Bullet(c, it, R.drawable.ic_bulb) }
        }
        ChunkyButton(stringResource(startLabel), onStart, c.green, c.greenLip, c.onAccent, Modifier.padding(top = 8.dp))
    }
    Text(stringResource(R.string.course_lessons), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
}

@Composable
private fun Bullet(c: DopamineColors, text: String, icon: Int) {
    Row(verticalAlignment = Alignment.Top) {
        IconImage(icon, size = 24.dp, bob = false)
        Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(start = 10.dp))
    }
}
