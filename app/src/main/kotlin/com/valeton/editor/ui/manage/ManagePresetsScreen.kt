package com.valeton.editor.ui.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.valeton.editor.ui.components.SegmentedPill
import com.valeton.editor.ui.theme.*

data class PresetSlotItem(
    val slotNumber: Int,
    val name: String
)

@Composable
fun ManagePresetsScreen(
    currentSlot: Int,
    presetList: List<PresetSlotItem>,
    onSelectSlot: (Int) -> Unit,
    onBackClick: () -> Unit,
    onRenameSlot: (Int, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Patch(00-99)") }
    val tabs = listOf("Patch(00-99)", "Songlist(A-Z)")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantDark)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = TextPrimary
                )
            }

            // Title
            Text(
                text = "Manage",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // List Options Icon
            IconButton(
                onClick = {},
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceVariantDark)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                    contentDescription = "Lista",
                    tint = TextPrimary
                )
            }
        }

        // Segmented Tabs Pill
        SegmentedPill(
            items = tabs,
            selectedItem = selectedTab,
            onItemSelected = { selectedTab = it },
            itemLabel = { it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Preset List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val displayList = if (selectedTab == "Songlist(A-Z)") {
                presetList.sortedBy { it.name }
            } else {
                presetList
            }

            itemsIndexed(displayList) { _, item ->
                val isActive = item.slotNumber == currentSlot

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) SurfaceVariantDark else Color.Transparent)
                        .clickable { onSelectSlot(item.slotNumber) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Circular Number Badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isActive) ValetonBlue else Color(0xFF38404E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "%02d".format(item.slotNumber),
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Preset Name
                        Text(
                            text = item.name,
                            color = if (isActive) TextPrimary else TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                        )
                    }

                    // Options menu icon ...
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Mais opções",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
