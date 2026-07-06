package my.noveldoksuha.coreui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import my.noveldoksuha.coreui.theme.InternalTheme

import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun MyOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeHolderText: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        placeholder = {
            Text(
                text = placeHolderText,
                modifier = Modifier.alpha(0.7f),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        modifier = modifier
            .heightIn(min = 42.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onSurface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        interactionSource = remember { MutableInteractionSource() }
    )
}

@Preview
@Composable
private fun PreviewView() {
    InternalTheme {
        MyOutlinedTextField(
            value = "",
            onValueChange = {},
            placeHolderText = "placeholder"
        )
    }
}