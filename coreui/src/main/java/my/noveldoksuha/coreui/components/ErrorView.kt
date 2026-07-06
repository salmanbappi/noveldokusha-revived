package my.noveldoksuha.coreui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import my.noveldoksuha.coreui.R

import my.noveldoksuha.coreui.theme.SpaceMonoFontFamily

@Composable
fun ErrorView(
    error: String,
    onReload: (() -> Unit)? = null,
    onCopyError: ((String) -> Unit)? = null
) {
    @Composable
    fun Modifier.click(onClick: () -> Unit) = clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(color = MaterialTheme.colorScheme.error),
        onClick = onClick
    )

    Column(
        Modifier
            .padding(4.dp)
            .border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(8.dp))
    ) {
        if (onReload != null || onCopyError != null) {
            Row(Modifier.height(IntrinsicSize.Min)) {
                if (onReload != null)
                    Text(
                        text = stringResource(R.string.reload).uppercase(),
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = SpaceMonoFontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 0.08.em,
                        modifier = Modifier
                            .click(onReload)
                            .weight(1f)
                            .padding(12.dp),
                        textAlign = TextAlign.Center,
                    )
                if (onReload != null && onCopyError != null)
                    DividerVertical(
                        color = MaterialTheme.colorScheme.error,
                        thickness = 1.dp,
                    )
                if (onCopyError != null)
                    Text(
                        text = stringResource(R.string.copy_error).uppercase(),
                        color = MaterialTheme.colorScheme.error,
                        fontFamily = SpaceMonoFontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 0.08.em,
                        modifier = Modifier
                            .click(onClick = { onCopyError(error) })
                            .weight(1f)
                            .padding(12.dp),
                        textAlign = TextAlign.Center,
                    )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.error, thickness = 1.dp)
        }
        SelectionContainer {
            Text(
                text = "[ERROR] $error",
                fontFamily = SpaceMonoFontFamily,
                fontSize = 11.sp,
                letterSpacing = 0.04.em,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
