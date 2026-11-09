package com.cobalto.core.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun LazyColumnFastScroller(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    thickness: Dp = 6.dp,
    thumbHeight: Dp = 44.dp,
    thumbColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f),
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var containerHeightPx by remember { mutableStateOf(1f) }

    val totalItems = listState.layoutInfo.totalItemsCount
    val visibleItems = listState.layoutInfo.visibleItemsInfo.size

    val showScrollbar = totalItems > visibleItems && totalItems > 0

    val firstVisibleIndex = listState.firstVisibleItemIndex
    val firstVisibleOffset = listState.firstVisibleItemScrollOffset
    val itemHeightPx = if (visibleItems > 0) {
        listState.layoutInfo.visibleItemsInfo.sumOf { it.size }.toFloat() / visibleItems
    } else 1f

    val totalHeightPx = (totalItems * itemHeightPx).coerceAtLeast(1f)
    val currentScrollPx = (firstVisibleIndex * itemHeightPx + firstVisibleOffset)
    val scrollRatio = (currentScrollPx / (totalHeightPx - containerHeightPx).coerceAtLeast(1f)).coerceIn(0f, 1f)

    val alpha by animateFloatAsState(
        targetValue = if (showScrollbar) (if (isDragging || listState.isScrollInProgress) 1f else 0.7f) else 0f,
        animationSpec = tween(250)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                containerHeightPx = coords.size.height.toFloat().coerceAtLeast(1f)
            }
    ) {
        content()

        if (showScrollbar && alpha > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(thickness * 3f)
                    .padding(vertical = 4.dp, horizontal = 2.dp)
            ) {
                // Background Track
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxHeight()
                        .width(thickness)
                        .clip(CircleShape)
                        .background(trackColor)
                )

                // Interactive Thumb
                val maxOffsetDp = (containerHeightPx / 2.5f - thumbHeight.value).coerceAtLeast(0f)
                val thumbOffsetDp = scrollRatio * maxOffsetDp

                Box(
                    modifier = Modifier
                        .offset(y = thumbOffsetDp.dp)
                        .width(if (isDragging) thickness * 1.6f else thickness)
                        .height(thumbHeight)
                        .align(Alignment.TopCenter)
                        .shadow(if (isDragging) 4.dp else 1.dp, CircleShape)
                        .clip(CircleShape)
                        .background(if (isDragging) thumbColor else thumbColor.copy(alpha = alpha))
                        .pointerInput(totalItems) {
                            detectDragGestures(
                                onDragStart = { isDragging = true },
                                onDragEnd = { isDragging = false },
                                onDragCancel = { isDragging = false },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    val dragRatio = dragAmount.y / (containerHeightPx.coerceAtLeast(1f))
                                    val targetIndex = ((scrollRatio + dragRatio * 3f) * totalItems).toInt().coerceIn(0, totalItems - 1)
                                    coroutineScope.launch {
                                        listState.scrollToItem(targetIndex)
                                    }
                                }
                            )
                        }
                )
            }
        }
    }
}
