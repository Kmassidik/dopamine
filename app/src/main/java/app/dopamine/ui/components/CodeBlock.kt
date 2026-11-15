package app.dopamine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.dopamine.ui.code.Tok
import app.dopamine.ui.code.highlight
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.JetBrainsMono

/** Monospace code on a dark card. Scrolls sideways, never wraps, so indentation stays honest. */
@Composable
fun CodeBlock(code: String, language: String?, modifier: Modifier = Modifier) {
    val c = Dopamine.colors
    val text = remember(code, language, c) {
        val spans = highlight(code, language)
        buildAnnotatedString {
            var at = 0
            for (s in spans) {
                if (s.start > at) append(code.substring(at, s.start))
                val color = when (s.tok) {
                    Tok.KEYWORD -> c.pink
                    Tok.STRING -> c.green
                    Tok.NUMBER -> c.yellow
                    Tok.COMMENT -> Color(0xFFA7B5AC)
                    Tok.PLAIN -> Color.White
                }
                withStyle(SpanStyle(color = color)) { append(code.substring(s.start, s.end)) }
                at = s.end
            }
            if (at < code.length) append(code.substring(at))
        }
    }
    Column(modifier.fillMaxWidth().clip(CardShape).background(c.onAccent).padding(16.dp)) {
        if (!language.isNullOrBlank()) {
            Text(language, style = MaterialTheme.typography.labelSmall, color = c.yellow, modifier = Modifier.padding(bottom = 8.dp))
        }
        Text(
            text,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMono, fontSize = 14.sp, lineHeight = 22.sp),
            softWrap = false,
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        )
    }
}
