package app.dopamine.ui.notes

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dopamine.shared.resources.*
import app.dopamine.ui.components.BackButton
import app.dopamine.domain.model.NoteBlock
import app.dopamine.ui.components.AppIcon
import app.dopamine.ui.components.CodeBlock
import app.dopamine.ui.components.IconImage
import app.dopamine.ui.components.heading
import app.dopamine.ui.math.MathText
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.DopamineColors

/** The unit cheat sheet. Open any time from the course screen. */
@Composable
fun NotesScreen(vm: NotesViewModel, onBack: () -> Unit) {
    val s by vm.state.collectAsStateWithLifecycle()
    val c = Dopamine.colors
    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                BackButton(stringResource(Res.string.back), onBack)
                Text(
                    s.notes?.title ?: stringResource(Res.string.notes_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp).heading(),
                )
            }
            val notes = s.notes
            if (!s.loading && notes == null) {
                Text(
                    stringResource(Res.string.notes_missing),
                    style = MaterialTheme.typography.bodyLarge,
                    color = c.inkSoft,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                )
            }
            if (notes != null) {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 112.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(notes.blocks, key = { it.id }) { b ->
                        when (b.kind) {
                            "formula" -> Formula(c, b)
                            "list" -> ListBlock(c, b)
                            "tip" -> Tip(c, b)
                            "code" -> CodeNote(c, b)
                            "unsupported" -> app.dopamine.ui.learn.Unsupported(c)
                            else -> Prose(c, b)
                        }
                    }
                }
            }
        }
    }
}

private val BodyStyle @Composable get() = MaterialTheme.typography.bodyLarge.merge(TextStyle(fontSize = 18.sp, lineHeight = 28.sp))

@Composable
private fun Prose(c: DopamineColors, b: NoteBlock) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        b.title?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
        MathText(b.body.orEmpty(), style = BodyStyle)
    }
}

@Composable
private fun Formula(c: DopamineColors, b: NoteBlock) {
    Column(Modifier.fillMaxWidth().clip(CardShape).background(c.yellowTint).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        b.title?.let { Text(it, style = MaterialTheme.typography.labelLarge, color = c.yellowText) }
        MathText(b.body.orEmpty(), style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun ListBlock(c: DopamineColors, b: NoteBlock) {
    Column(Modifier.fillMaxWidth().clip(CardShape).background(c.greenTint).padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        b.title?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
        b.items.orEmpty().forEach { item ->
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.padding(top = 10.dp).size(8.dp).clip(CircleShape).background(c.green))
                MathText(item, style = BodyStyle, modifier = Modifier.weight(1f).padding(start = 12.dp))
            }
        }
    }
}

@Composable
private fun Tip(c: DopamineColors, b: NoteBlock) {
    Row(Modifier.fillMaxWidth().clip(CardShape).background(c.pinkTint).padding(20.dp), verticalAlignment = Alignment.Top) {
        IconImage(Res.drawable.ic_bulb, size = 40.dp, bob = false)
        MathText(b.body.orEmpty(), style = BodyStyle, modifier = Modifier.weight(1f).padding(start = 12.dp))
    }
}

@Composable
private fun CodeNote(c: DopamineColors, b: NoteBlock) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        b.title?.let { Text(it, style = MaterialTheme.typography.titleLarge) }
        CodeBlock(b.code.orEmpty(), b.language)
    }
}
