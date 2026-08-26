package com.rork.mindsetframestracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rork.mindsetframestracker.data.HabitIconCatalog
import com.rork.mindsetframestracker.data.HabitSuggestion

@Composable
fun HabitPickerGrid(
    suggestions: List<HabitSuggestion>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
) {
    val icons = HabitIconCatalog.icons.filter { icon ->
        suggestions.any { it.name == icon.label }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(icons, key = { it.id }) { icon ->
            val isSelected = icon.label in selected
            val reason = suggestions.find { it.name == icon.label }?.reason

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(icon.colorHex).copy(alpha = if (isSelected) 1f else 0.5f))
                    .clickable { onToggle(icon.label) }
                    .padding(16.dp),
            ) {
                Text(icon.emoji, fontSize = 28.sp)
                Spacer(Modifier.height(8.dp))
                Text(icon.label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                if (reason != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        reason,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                }
            }
        }
    }
}
