package my.noveldoksuha.coreui.components

import my.noveldoksuha.coreui.modifiers.bounceOnPressed

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import my.noveldoksuha.coreui.theme.ColorAccent
import my.noveldoksuha.coreui.theme.InternalTheme
import my.noveldoksuha.coreui.theme.Themes
import my.noveldoksuha.coreui.theme.ifCase
import my.noveldoksuha.coreui.theme.selectableMinHeight

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import my.noveldoksuha.coreui.theme.SpaceMonoFontFamily

@Composable
fun MyButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    animate: Boolean = true,
    textAlign: TextAlign = TextAlign.Center,
    outerPadding: Dp = 4.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    minHeight: Dp = 48.dp,
    shape: Shape = RoundedCornerShape(999.dp),
    borderWidth: Dp = 0.dp,
    borderColor: Color = Color.Transparent,
    backgroundColor: Color = MaterialTheme.colorScheme.onSurface,
    textStyle: TextStyle = TextStyle(
        fontFamily = SpaceMonoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        letterSpacing = 0.06.em
    ),
    selected: Boolean = false,
    selectedBackgroundColor: Color = Color.White,
    textAllCaps: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    indication: Indication = LocalIndication.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable BoxScope.() -> Unit = {
        val defaultTextColor = if (backgroundColor == MaterialTheme.colorScheme.onSurface) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        val color = when {
            selected -> Color.Black
            textStyle.color != Color.Unspecified -> textStyle.color
            else -> defaultTextColor
        }
        Text(
            text = if (textAllCaps) text.uppercase() else text,
            style = textStyle,
            color = color,
            textAlign = textAlign,
            modifier = Modifier
                .padding(contentPadding)
                .wrapContentHeight()
                .align(Alignment.Center),
        )
    }
) {
    InternalButton(
        modifier = modifier,
        enabled = enabled,
        animate = animate,
        outerPadding = outerPadding,
        minHeight = minHeight,
        minWidth = Dp.Unspecified,
        shape = shape,
        borderWidth = borderWidth,
        borderColor = borderColor,
        backgroundColor = backgroundColor,
        selectedBackgroundColor = selectedBackgroundColor,
        selected = selected,
        onClick = onClick,
        onLongClick = onLongClick,
        indication = indication,
        interactionSource = interactionSource,
        content = content,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InternalButton(
    modifier: Modifier,
    enabled: Boolean,
    animate: Boolean,
    outerPadding: Dp,
    minHeight: Dp,
    minWidth: Dp,
    shape: Shape,
    borderWidth: Dp,
    borderColor: Color,
    backgroundColor: Color,
    selectedBackgroundColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    indication: Indication,
    interactionSource: MutableInteractionSource,
    content: @Composable BoxScope.() -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) selectedBackgroundColor else backgroundColor, label = ""
    )
    Surface(
        modifier = modifier
            .bounceOnPressed(interactionSource)
            .ifCase(animate) { animateContentSize() }
            .padding(outerPadding)
            .heightIn(min = minHeight)
            .widthIn(min = minWidth)
            .border(borderWidth, borderColor, shape)
            .clip(shape)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = indication,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = background
    ) {
        Box(propagateMinConstraints = true) {
            content(this)
        }
    }
}


@Preview
@Composable
fun Preview() {
    Column {
        for (theme in Themes.entries) InternalTheme(theme) {
            MyButton(
                text = "Theme ${theme.name}",
                modifier = Modifier.fillMaxWidth(),
                onClick = {},
            )
        }
    }
}
