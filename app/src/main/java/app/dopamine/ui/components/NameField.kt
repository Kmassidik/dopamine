package app.dopamine.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import app.dopamine.domain.model.ProfileName
import app.dopamine.ui.theme.ButtonShape
import app.dopamine.ui.theme.Dopamine

/** The single-line name field used by onboarding and settings. */
@Composable
fun NameField(value: String, onChange: (String) -> Unit, hint: String, onDone: () -> Unit, modifier: Modifier = Modifier) {
    val c = Dopamine.colors
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.length <= ProfileName.MAX) onChange(it) },
        singleLine = true,
        shape = ButtonShape,
        placeholder = { Text(hint) },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        textStyle = MaterialTheme.typography.titleMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = c.green,
            unfocusedBorderColor = c.line,
            cursorColor = c.greenText,
            focusedPlaceholderColor = c.inkSoft,
            unfocusedPlaceholderColor = c.inkSoft,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
