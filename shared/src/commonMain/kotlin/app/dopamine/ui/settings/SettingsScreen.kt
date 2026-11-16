package app.dopamine.ui.settings

import app.dopamine.platform.relativeTimeText
import app.dopamine.platform.nowMillis
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dopamine.shared.resources.*
import app.dopamine.ui.components.heading
import app.dopamine.domain.model.ProfileName
import app.dopamine.ui.components.AppIcon
import app.dopamine.ui.components.ChunkyButton
import app.dopamine.ui.components.IconImage
import app.dopamine.ui.components.InkButton
import app.dopamine.ui.components.Message
import app.dopamine.ui.components.NameField
import app.dopamine.ui.components.ProgressBar
import app.dopamine.ui.theme.CardShape
import app.dopamine.ui.theme.Dopamine

@Composable
fun SettingsScreen(vm: SettingsViewModel, onOpenFormulas: () -> Unit) {
    val c = Dopamine.colors
    val name by vm.name.collectAsStateWithLifecycle()
    val draft by vm.draft.collectAsStateWithLifecycle()
    val update by vm.update.collectAsStateWithLifecycle()
    val lastUpdated by vm.lastUpdated.collectAsStateWithLifecycle()

    Surface(Modifier.fillMaxSize(), color = c.bg, contentColor = c.ink) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(Res.string.settings_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp).heading())
            }
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item { ProfileCard(c, name, draft, vm) }
                item { ContentCard(c, update, lastUpdated, vm) }
                item { AboutCard(c, vm.versionName, onOpenFormulas) }
            }
        }
    }
}

@Composable
private fun Card(tint: Color, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(CardShape).background(tint).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) { content() }
}

@Composable
private fun ProfileCard(c: app.dopamine.ui.theme.DopamineColors, name: String, draft: String?, vm: SettingsViewModel) {
    Card(c.pinkTint) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(c.pink), contentAlignment = Alignment.Center) {
                Text(name.take(1).uppercase(), style = MaterialTheme.typography.titleLarge, color = c.onAccent)
            }
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Text(stringResource(Res.string.settings_name), style = MaterialTheme.typography.labelMedium, color = c.inkSoft)
                Text(name, style = MaterialTheme.typography.titleLarge)
            }
        }
        if (draft == null) {
            InkButton(stringResource(Res.string.settings_change_name), onClick = vm::startEdit, container = c.onAccent, content = Color.White)
        } else {
            NameField(draft, vm::setDraft, stringResource(Res.string.onboarding_hint), onDone = vm::saveName)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = vm::cancelEdit, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(Res.string.settings_cancel), color = c.pinkText)
                }
                Box(Modifier.weight(1f)) {
                    ChunkyButton(
                        text = stringResource(Res.string.settings_save),
                        onClick = vm::saveName,
                        container = c.green,
                        lip = c.greenLip,
                        content = c.onAccent,
                        enabled = ProfileName.clean(draft) != null,
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentCard(c: app.dopamine.ui.theme.DopamineColors, state: UpdateUi, lastUpdated: Long, vm: SettingsViewModel) {
    Card(c.greenTint) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                IconImage(Res.drawable.ic_update, size = 44.dp)
            }
            Column(Modifier.weight(1f).padding(start = 16.dp)) {
                Text(stringResource(Res.string.settings_content), style = MaterialTheme.typography.titleLarge)
                Text(
                    if (lastUpdated == 0L) stringResource(Res.string.update_bundled)
                    else stringResource(
                        Res.string.update_last,
                        relativeTimeText(lastUpdated, nowMillis()),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = c.inkSoft,
                )
            }
        }
        Text(stringResource(Res.string.settings_content_hint), style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
        when (state) {
            is UpdateUi.Running -> {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (state.total == 0) stringResource(Res.string.update_checking)
                        else stringResource(Res.string.update_running, state.done, state.total),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = vm::cancelUpdate, modifier = Modifier.heightIn(min = 48.dp)) {
                        Text(stringResource(Res.string.update_cancel), color = c.pinkText)
                    }
                }
                ProgressBar(if (state.total == 0) 0f else state.done.toFloat() / state.total, Color.White, c.green)
            }
            is UpdateUi.Done -> Message(Color.White, c.greenText, Res.drawable.ic_check, state.message, stringResource(Res.string.update_dismiss), vm::dismissMessage)
            is UpdateUi.Failed -> Message(c.pinkTint, c.pinkText, Res.drawable.ic_warning, state.message, stringResource(Res.string.update_dismiss), vm::dismissMessage)
            UpdateUi.Idle -> Unit
        }
        if (state !is UpdateUi.Running) {
            InkButton(
                text = stringResource(Res.string.update_idle),
                onClick = vm::startUpdate,
                container = c.onAccent,
                content = Color.White,
                leadingIcon = Res.drawable.ic_download,
            )
        }
    }
}

@Composable
private fun AboutCard(c: app.dopamine.ui.theme.DopamineColors, version: String, onOpenFormulas: () -> Unit) {
    val formulaLabel = stringResource(Res.string.settings_formula_open)
    Card(c.yellowTint) {
        Text(stringResource(Res.string.settings_about), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(Res.string.settings_version, version),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .pointerInput(Unit) { detectTapGestures(onLongPress = { onOpenFormulas() }) }
                .semantics { onLongClick(label = formulaLabel) { onOpenFormulas(); true } },
        )
        Text(stringResource(Res.string.settings_privacy), style = MaterialTheme.typography.bodyMedium, color = c.inkSoft)
    }
}
