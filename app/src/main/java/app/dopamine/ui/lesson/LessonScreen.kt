package app.dopamine.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dopamine.R
import app.dopamine.domain.model.Question
import app.dopamine.ui.components.AppIcon
import app.dopamine.ui.components.ChunkyButton
import app.dopamine.ui.components.IconImage
import app.dopamine.ui.components.ProgressBar
import app.dopamine.ui.components.StatChip
import app.dopamine.ui.components.bounceClick
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.DopamineColors
import app.dopamine.ui.theme.OutlineWidth

/** What the question body tells the screen: can the learner check yet, and is the answer right. */
private class Answer {
    var ready by mutableStateOf(false)
    var eval: () -> Boolean = { false }
}

@Composable
fun LessonScreen(vm: LessonViewModel, onClose: () -> Unit) {
    val s by vm.state.collectAsStateWithLifecycle()
    val c = Dopamine.colors
    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            when {
                s.loading -> Box(Modifier.fillMaxSize())
                s.questions.isEmpty() -> Empty(c, onClose)
                s.finished -> Done(c, s, onClose)
                else -> Playing(c, s, vm, onClose)
            }
        }
    }
}

@Composable
private fun Empty(c: DopamineColors, onClose: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        IconImage(R.drawable.mascot_sweat, size = 120.dp)
        Text(stringResource(R.string.lesson_empty), style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 16.dp))
        ChunkyButton(stringResource(R.string.back), onClose, c.green, c.greenLip, c.onAccent)
    }
}

@Composable
private fun Done(c: DopamineColors, s: LessonState, onClose: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        IconImage(if (s.correct * 2 >= s.questions.size) R.drawable.mascot_trophy else R.drawable.mascot_cheer, size = 160.dp)
        Text(stringResource(R.string.lesson_done), style = MaterialTheme.typography.displayLarge, modifier = Modifier.padding(top = 16.dp))
        Text(
            stringResource(R.string.lesson_score, s.correct, s.questions.size),
            style = MaterialTheme.typography.bodyLarge,
            color = c.inkSoft,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )
        StatChip(R.drawable.ic_xp, "+${s.xpEarned}", c.yellowTint, c.ink, stringResource(R.string.xp_desc, s.xpEarned))
        Box(Modifier.size(24.dp))
        ChunkyButton(stringResource(R.string.lesson_finish), onClose, c.green, c.greenLip, c.onAccent)
    }
}

@Composable
private fun ColumnScope.Playing(c: DopamineColors, s: LessonState, vm: LessonViewModel, onClose: () -> Unit) {
    val q = s.questions[s.index]
    val ans = remember(q.id) { Answer() }
    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).clickable(onClickLabel = stringResource(R.string.lesson_close), onClick = onClose),
            contentAlignment = Alignment.Center,
        ) { AppIcon(R.drawable.ic_back, c.ink, description = stringResource(R.string.lesson_close)) }
        ProgressBar(s.index.toFloat() / s.questions.size, c.greenTint, c.green, Modifier.weight(1f).padding(end = 16.dp))
    }
    Column(
        Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(plainMath(q.prompt.orEmpty()), style = MaterialTheme.typography.titleLarge)
        when (q.type) {
            "multiple_choice", "fill_blank" -> ChoiceBody(c, q, ans, s.checked)
            "type_answer" -> TypeBody(c, q, ans, s.checked)
            "match_pairs" -> MatchBody(c, q, ans, s.checked)
            "order_steps" -> OrderBody(c, q, ans, s.checked)
        }
    }
    Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (s.checked) {
            val good = s.lastCorrect
            Column(
                Modifier.fillMaxWidth().clip(CardShape).background(if (good) c.greenTint else c.pinkTint).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconImage(if (good) R.drawable.ic_correct else R.drawable.ic_wrong, size = 32.dp, bob = false)
                    Text(
                        stringResource(if (good) R.string.lesson_correct else R.string.lesson_wrong),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (good) c.greenText else c.pinkText,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                if (q.explanation.isNotBlank()) Text(plainMath(q.explanation), style = MaterialTheme.typography.bodyMedium)
            }
            ChunkyButton(
                stringResource(R.string.lesson_continue), vm::next,
                if (good) c.green else c.pink, if (good) c.greenLip else c.pinkLip, c.onAccent,
            )
        } else {
            ChunkyButton(
                stringResource(R.string.lesson_check), { vm.check(ans.eval()) },
                c.green, c.greenLip, c.onAccent, enabled = ans.ready,
            )
        }
    }
}

@Composable
private fun Option(c: DopamineColors, text: String, bg: Color, border: Color, onClick: (() -> Unit)?, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(bg)
            .border(OutlineWidth, border, CardShape)
            .bounceClick(onClick = { onClick?.invoke() })
            .heightIn(min = 56.dp)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) { Text(text, style = MaterialTheme.typography.titleMedium, color = c.ink) }
}

@Composable
private fun ChoiceBody(c: DopamineColors, q: Question, ans: Answer, checked: Boolean) {
    var sel by remember(q.id) { mutableStateOf<Int?>(null) }
    SideEffect { ans.ready = sel != null; ans.eval = { sel == q.answerIndex } }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        q.choices.orEmpty().forEachIndexed { i, choice ->
            val (bg, line) = when {
                checked && i == q.answerIndex -> c.greenTint to c.green
                checked && i == sel -> c.pinkTint to c.pink
                sel == i -> c.yellowTint to c.yellow
                else -> Color.White to c.line
            }
            Option(c, plainMath(choice), bg, line, if (checked) null else ({ sel = i }))
        }
    }
}

private fun normalize(s: String) = s.trim().lowercase().replace(" ", "").replace(",", ".")

private fun sameAnswer(given: String, accepted: String): Boolean {
    val a = normalize(given)
    val b = normalize(accepted)
    if (a == b) return true
    val x = a.toDoubleOrNull()
    val y = b.toDoubleOrNull()
    return x != null && y != null && kotlin.math.abs(x - y) < 1e-9
}

@Composable
private fun TypeBody(c: DopamineColors, q: Question, ans: Answer, checked: Boolean) {
    var text by remember(q.id) { mutableStateOf("") }
    SideEffect {
        ans.ready = text.isNotBlank()
        ans.eval = { q.accepted.orEmpty().any { sameAnswer(text, it) } }
    }
    OutlinedTextField(
        value = text,
        onValueChange = { if (!checked && it.length <= 40) text = it },
        singleLine = true,
        readOnly = checked,
        shape = CardShape,
        textStyle = MaterialTheme.typography.titleLarge,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        modifier = Modifier.fillMaxWidth(),
    )
    if (checked) {
        Text(
            stringResource(R.string.lesson_answer_was, plainMath(q.accepted.orEmpty().firstOrNull().orEmpty())),
            style = MaterialTheme.typography.bodyMedium,
            color = c.inkSoft,
        )
    }
}

@Composable
private fun MatchBody(c: DopamineColors, q: Question, ans: Answer, checked: Boolean) {
    val pairs = q.pairs.orEmpty().filter { it.size >= 2 }
    val rights = remember(q.id) { pairs.indices.shuffled() }
    val matched = remember(q.id) { mutableStateListOf<Int>() }
    var left by remember(q.id) { mutableStateOf<Int?>(null) }
    var wrong by remember(q.id) { mutableStateOf<Int?>(null) }
    var mistakes by remember(q.id) { mutableStateOf(0) }
    SideEffect { ans.ready = pairs.isNotEmpty() && matched.size == pairs.size; ans.eval = { mistakes == 0 } }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            pairs.indices.forEach { i ->
                val (bg, line) = when {
                    i in matched -> c.greenTint to c.green
                    left == i -> c.yellowTint to c.yellow
                    else -> Color.White to c.line
                }
                Option(c, plainMath(pairs[i][0]), bg, line, if (checked || i in matched) null else ({ left = i; wrong = null }))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rights.forEach { r ->
                val (bg, line) = when {
                    r in matched -> c.greenTint to c.green
                    wrong == r -> c.pinkTint to c.pink
                    else -> Color.White to c.line
                }
                Option(c, plainMath(pairs[r][1]), bg, line, if (checked || r in matched) null else ({
                    val l = left
                    if (l != null) {
                        if (l == r) matched.add(r) else { mistakes++; wrong = r }
                        left = null
                    }
                }))
            }
        }
    }
}

@Composable
private fun OrderBody(c: DopamineColors, q: Question, ans: Answer, checked: Boolean) {
    val steps = q.steps.orEmpty()
    val order = remember(q.id) { steps.indices.shuffled() }
    val chosen = remember(q.id) { mutableStateListOf<Int>() }
    SideEffect { ans.ready = steps.isNotEmpty() && chosen.size == steps.size; ans.eval = { chosen.toList() == steps.indices.toList() } }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.lesson_order_hint), style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
        chosen.forEachIndexed { n, idx ->
            val line = if (!checked) c.yellow else if (idx == n) c.green else c.pink
            val bg = if (!checked) c.yellowTint else if (idx == n) c.greenTint else c.pinkTint
            Option(c, "${n + 1}. ${plainMath(steps[idx])}", bg, line, if (checked) null else ({ chosen.remove(idx) }))
        }
        order.filter { it !in chosen }.forEach { idx ->
            Option(c, plainMath(steps[idx]), Color.White, c.line, if (checked) null else ({ chosen.add(idx) }))
        }
    }
}
