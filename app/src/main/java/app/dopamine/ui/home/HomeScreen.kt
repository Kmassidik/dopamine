package app.dopamine.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.dopamine.R
import app.dopamine.ui.theme.Dopamine
import app.dopamine.ui.theme.InkCardShape
import app.dopamine.ui.theme.InkOutlineWidth

@Composable
fun HomeScreen() {
    val space = Dopamine.spacing
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = space.m, vertical = space.l),
                verticalArrangement = Arrangement.spacedBy(space.l),
            ) {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Surface(
                    shape = InkCardShape,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(InkOutlineWidth, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().padding(end = space.l),
                ) {
                    Column(
                        modifier = Modifier.padding(space.m),
                        verticalArrangement = Arrangement.spacedBy(space.s),
                    ) {
                        Text(
                            text = stringResource(R.string.home_empty_title),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(R.string.home_empty_body),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(space.m)) {
                Button(
                    onClick = {},
                    enabled = false,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(),
                    modifier = Modifier.fillMaxWidth().heightIn(min = space.minTouch),
                ) {
                    Text(stringResource(R.string.action_update), style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
