package com.moses.inspectionapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moses.inspectionapp.ui.theme.AppColors

@Composable
fun PaginationRow(
    currentPage: Int,
    totalPages: Int,
    onPageSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (totalPages <= 1) return
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PaginationIconButton(
            enabled = currentPage > 0,
            icon = Icons.Rounded.ChevronLeft,
            onClick = { onPageSelected(currentPage - 1) },
        )
        Spacer(modifier = Modifier.size(8.dp))
        buildPageItems(currentPage, totalPages).forEach { item ->
            when (item) {
                is PageItem.Page -> {
                    PaginationPageButton(
                        label = (item.index + 1).toString(),
                        selected = item.index == currentPage,
                        onClick = { onPageSelected(item.index) },
                    )
                }
                PageItem.Ellipsis -> {
                    Text(
                        text = "...",
                        color = AppColors.TextSecondary,
                        modifier = Modifier.size(32.dp),
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        PaginationIconButton(
            enabled = currentPage < totalPages - 1,
            icon = Icons.Rounded.ChevronRight,
            onClick = { onPageSelected(currentPage + 1) },
        )
    }
}

@Composable
private fun PaginationPageButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background = if (selected) AppColors.NavyDark else AppColors.CardSurface
    val textColor = if (selected) AppColors.TextOnDark else AppColors.TextPrimary
    Surface(
        color = background,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.size(34.dp),
        onClick = onClick,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = textColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
    Spacer(modifier = Modifier.size(6.dp))
}

@Composable
private fun PaginationIconButton(
    enabled: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val background = if (enabled) AppColors.CardSurface else AppColors.SteelBlueTint
    val tint = if (enabled) AppColors.TextPrimary else AppColors.TextSecondary
    Surface(
        color = background,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.size(34.dp),
        onClick = { if (enabled) onClick() },
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
            )
        }
    }
}

private sealed class PageItem {
    data class Page(val index: Int) : PageItem()
    object Ellipsis : PageItem()
}

private fun buildPageItems(currentPage: Int, totalPages: Int): List<PageItem> {
    if (totalPages <= 6) {
        return (0 until totalPages).map { PageItem.Page(it) }
    }
    val items = mutableListOf<PageItem>()
    items.add(PageItem.Page(0))
    val start = (currentPage - 1).coerceAtLeast(1)
    val end = (currentPage + 1).coerceAtMost(totalPages - 2)
    if (start > 1) {
        items.add(PageItem.Ellipsis)
    }
    for (page in start..end) {
        items.add(PageItem.Page(page))
    }
    if (end < totalPages - 2) {
        items.add(PageItem.Ellipsis)
    }
    items.add(PageItem.Page(totalPages - 1))
    return items
}
