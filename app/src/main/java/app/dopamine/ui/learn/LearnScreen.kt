package app.dopamine.ui.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dopamine.R
import app.dopamine.domain.model.LearnPage
import app.dopamine.ui.components.AppIcon
import app.dopamine.ui.components.ChunkyButton
import app.dopamine.ui.components.IconImage
import app.dopamine.ui.components.ProgressBar
import app.dopamine.ui.math.MathText
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.DopamineColors
import app.dopamine.ui.theme.LocalReducedMotion

/** Reading pages before the practice. Tap the big button to move through; examples reveal one step at a time. */
@Composable
fun LearnScreen(vm: LearnViewModel, onBack: () -> Unit, onPractice: () -> Unit) {
    val s by vm.state.collectAsStateWithLifecycle()
    val c = Dopamine.colors
    val reduced = LocalReducedMotion.current

    // A lesson with no reading goes straight to its practice.
    LaunchedEffect(s.loading, s.pages.isEmpty()) { if (!s.loading && s.pages.isEmpty()) onPractice() }

    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            val page = s.page ?: return@Column
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).clickable(onClickLabel = stringResource(R.string.back), onClick = onBack),
                    contentAlignment = Alignment.Center,
                ) { AppIcon(R.drawable.ic_back, c.ink, description = stringResource(R.string.back)) }
                ProgressBar((s.index + 1f) / s.pages.size, c.yellowTint, c.yellow, Modifier.weight(1f).padding(horizontal = 8.dp))
                Text(
                    stringResource(R.string.learn_skip),
                    style = MaterialTheme.typography.labelMedium,
                    color = c.inkSoft,
                    modifier = Modifier.heightIn(min = 48.dp).clip(CircleShape).clickable(onClick = onPractice).padding(horizontal = 12.dp, vertical = 14.dp),
                )
            }
            Text(
                stringResource(R.string.learn_page, s.index + 1, s.pages.size),
                style = MaterialTheme.typography.labelMedium,
                color = c.inkSoft,
                modifier = Modifier.padding(start = 20.dp, top = 8.dp),
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = s.index,
                    transitionSpec = {
                        if (reduced) fadeIn() togetherWith fadeOut()
                        else (slideInHorizontally { it / 4 } + fadeIn()) togetherWith (slideOutHorizontally { -it / 4 } + fadeOut())
                    },
                    label = "page",
                ) { i ->
                    val p = s.pages.getOrNull(i) ?: page
                    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
                        when (p.kind) {
                            "example" -> ExamplePage(c, p, if (i == s.index) s.shown else p.steps.orEmpty().size)
                            "tip" -> TipPage(c, p)
                            else -> TextPage(c, p)
                        }
                    }
                }
            }
            val label = when {
                !s.atLastStep -> R.string.learn_next_step
                s.isLast -> R.string.learn_start
                else -> R.string.learn_next
            }
            ChunkyButton(
                stringResource(label),
                { if (vm.advance()) onPractice() },
                if (s.isLast && s.atLastStep) c.green else c.yellow,
                if (s.isLast && s.atLastStep) c.greenLip else c.yellowLip,
                c.onAccent,
                Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
            )
        }
    }
}

private val BodyStyle @Composable get() = MaterialTheme.typography.bodyLarge.merge(TextStyle(fontSize = 19.sp, lineHeight = 30.sp))

@Composable
private fun TextPage(c: DopamineColors, p: LearnPage) {
    IconImage(R.drawable.ic_reading, size = 64.dp)
    Text(
        p.title.orEmpty(),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
    )
    MathText(p.body.orEmpty(), style = BodyStyle)
}

@Composable
private fun TipPage(c: DopamineColors, p: LearnPage) {
    Column(Modifier.fillMaxWidth().clip(CardShape).background(c.greenTint).padding(24.dp)) {
        IconImage(R.drawable.ic_bulb, size = 56.dp)
        Text(
            stringResource(R.string.learn_tip),
            style = MaterialTheme.typography.titleLarge,
            color = c.greenText,
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
        )
        MathText(p.body.orEmpty(), style = BodyStyle)
    }
}

@Composable
private fun ExamplePage(c: DopamineColors, p: LearnPage, shown: Int) {
    Text(
        stringResource(R.string.learn_example),
        style = MaterialTheme.typography.labelLarge,
        color = c.yellowText,
        modifier = Modifier.clip(CircleShape).background(c.yellowTint).padding(horizontal = 14.dp, vertical = 6.dp),
    )
    MathText(p.title.orEmpty(), style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 12.dp, bottom = 8.dp))
    if (!p.body.isNullOrBlank()) MathText(p.body, style = BodyStyle, color = c.inkSoft)
    Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        p.steps.orEmpty().forEachIndexed { n, step ->
            AnimatedVisibility(visible = n < shown, enter = fadeIn() + slideInHorizontally { it / 6 }) {
                Row(
                    Modifier.fillMaxWidth().clip(CardShape).background(c.yellowTint).padding(16.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(c.yellow), contentAlignment = Alignment.Center) {
                        Text("${n + 1}", style = MaterialTheme.typography.labelLarge, color = c.onAccent)
                    }
                    MathText(step, style = BodyStyle, modifier = Modifier.weight(1f).padding(start = 12.dp))
                }
            }
        }
    }
}
