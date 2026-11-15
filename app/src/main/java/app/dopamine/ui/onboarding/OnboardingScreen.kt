package app.dopamine.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dopamine.R
import app.dopamine.domain.model.ProfileName
import app.dopamine.ui.components.IconImage
import app.dopamine.ui.components.ChunkyButton
import app.dopamine.ui.components.NameField
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine

@Composable
fun OnboardingScreen(vm: OnboardingViewModel) {
    val c = Dopamine.colors
    val text by vm.text.collectAsStateWithLifecycle()
    val valid = ProfileName.clean(text) != null

    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(CardShape)
                    .background(c.yellow)
                    .drawBehind {
                        drawCircle(c.pink, 80.dp.toPx(), Offset(size.width - 10.dp.toPx(), -10.dp.toPx()))
                        drawCircle(c.green, 52.dp.toPx(), Offset(20.dp.toPx(), size.height - 6.dp.toPx()))
                        drawCircle(Color.White.copy(alpha = 0.4f), 60.dp.toPx(), Offset(size.width * 0.55f, size.height + 16.dp.toPx()))
                    },
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                IconImage(R.drawable.mascot_wave, size = 150.dp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.displayLarge)
                Text(stringResource(R.string.onboarding_body), style = MaterialTheme.typography.bodyLarge, color = c.inkSoft)
            }
            NameField(
                value = text,
                onChange = vm::set,
                hint = stringResource(R.string.onboarding_hint),
                onDone = vm::save,
            )
            ChunkyButton(
                text = stringResource(R.string.onboarding_action),
                onClick = vm::save,
                container = c.green,
                lip = c.greenLip,
                content = c.onAccent,
                enabled = valid,
            )
        }
    }
}
