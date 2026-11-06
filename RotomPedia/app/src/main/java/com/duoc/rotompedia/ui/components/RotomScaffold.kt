package com.duoc.rotompedia.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.duoc.rotompedia.R
import com.duoc.rotompedia.ui.theme.*
import kotlin.math.roundToInt

/** One local bitmap per screen root. Draw proportionally; never crop the face to fill the viewport. */
@Composable
internal fun RotomBackground(sideFace: Boolean, faceWidthFraction: Float, modifier: Modifier = Modifier) {
    val bitmap = ImageBitmap.imageResource(R.drawable.rotomfondo)
    val colors = LocalRotomPalette.current
    Canvas(modifier.clipToBounds().background(colors.frame)) {
        val artWidth = if (sideFace) size.width * faceWidthFraction else size.width
        val artHeight = artWidth * bitmap.height / bitmap.width
        clipRect {
            drawImage(bitmap, dstOffset = IntOffset.Zero,
                dstSize = IntSize(artWidth.roundToInt().coerceAtLeast(1), artHeight.roundToInt().coerceAtLeast(1)))
            drawRect(Color.Black.copy(alpha = 0.28f))
            val faceBottom = artWidth * RotomDimensions.FaceAspect
            drawRect(colors.backgroundScrim, topLeft = androidx.compose.ui.geometry.Offset(0f, faceBottom),
                size = androidx.compose.ui.geometry.Size(size.width, (size.height - faceBottom).coerceAtLeast(0f)))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotomTopBar(title: String, onNavigateBack: (() -> Unit)?, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) }, navigationIcon = {
        if (onNavigateBack != null) IconButton(onClick = onNavigateBack, modifier = Modifier.size(RotomDimensions.TouchTarget)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
        }
    }, actions = actions, colors = TopAppBarDefaults.topAppBarColors(
        containerColor = LocalRotomPalette.current.frame, titleContentColor = Color.White,
        navigationIconContentColor = Color.White, actionIconContentColor = Color.White))
}

@Composable
fun RotomScaffold(title: String, onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}, content: @Composable BoxScope.() -> Unit) {
    RotomPageScaffold(topBar = { RotomTopBar(title, onNavigateBack, actions) }) {
        Box(Modifier.fillMaxSize(), content = content)
    }
}

/** Shared device frame for existing screens: content receives local insets, not a second status bar. */
@Composable
fun RotomPageScaffold(
    topBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    RotompediaTheme {
    Scaffold(topBar = topBar, floatingActionButton = floatingActionButton,
        containerColor = LocalRotomPalette.current.frame) { insets ->
            BoxWithConstraints(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets).imePadding()) {
                val availableWidth = maxWidth
                val side = maxWidth > maxHeight
                val sideFraction = if (maxWidth > 600.dp) 0.22f else 0.25f
                RotomBackground(side, sideFraction, Modifier.fillMaxSize())
                if (side) Row(Modifier.fillMaxSize()) {
                    Box(Modifier.width(availableWidth * sideFraction).fillMaxHeight().testTag("rotom_face"))
                    RotomReadingPanel(Modifier.weight(1f).fillMaxHeight()) { content(PaddingValues(0.dp)) }
                } else Column(Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxWidth().height(availableWidth * RotomDimensions.FaceAspect).testTag("rotom_face"))
                    RotomReadingPanel(Modifier.fillMaxWidth().weight(1f)) { content(PaddingValues(0.dp)) }
                }
            }
        }
    }
}

@Composable
private fun RotomReadingPanel(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier.padding(horizontal = RotomDimensions.ScreenGutter, vertical = 8.dp)
        .background(LocalRotomPalette.current.reading.copy(alpha = 0.96f), RoundedCornerShape(RotomDimensions.PanelCorner))
        .padding(horizontal = 4.dp).testTag("rotom_content"), content = content)
}

@Composable
fun RotomCard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface)
    val shape = RoundedCornerShape(RotomDimensions.CardCorner)
    val border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
    if (onClick == null) Card(modifier, shape = shape, colors = colors, border = border, content = content)
    else Card(onClick, modifier, shape = shape, colors = colors, border = border, content = content)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotomFilterSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface,
        content = content)
}
