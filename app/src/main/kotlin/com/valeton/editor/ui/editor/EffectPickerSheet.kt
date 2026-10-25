package com.valeton.editor.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.EffectDefinition
import com.valeton.editor.core.model.ModuleType
import com.valeton.editor.ui.components.PedalArtwork
import com.valeton.editor.ui.theme.*

data class CategoryPill(
    val category: String,
    val count: Int
)

object EffectPickerLogic {
    const val ALL_CATEGORY = "All"

    /**
     * Filters [effects] based on [query] and [selectedCategory].
     * If [selectedCategory] is "All" (case-insensitive) or blank, all categories are eligible.
     * If [query] is non-blank, matches case-insensitively against name, basedOn, or category.
     */
    fun filterEffects(
        effects: List<EffectDefinition>,
        query: String = "",
        selectedCategory: String = ALL_CATEGORY
    ): List<EffectDefinition> {
        val trimmed = query.trim()
        val filterByCategory = selectedCategory.isNotBlank() && !selectedCategory.equals(ALL_CATEGORY, ignoreCase = true)

        return effects.filter { effect ->
            val matchesCategory = !filterByCategory || effect.category.equals(selectedCategory, ignoreCase = true)
            if (!matchesCategory) return@filter false

            if (trimmed.isEmpty()) {
                true
            } else {
                effect.name.contains(trimmed, ignoreCase = true) ||
                    effect.basedOn.contains(trimmed, ignoreCase = true) ||
                    effect.category.contains(trimmed, ignoreCase = true)
            }
        }
    }

    /**
     * Builds category pills with counts.
     * The first pill is "All" with total count of matching effects.
     * Subsequent pills represent each category in [categories] with the count of matching effects.
     */
    fun buildCategoryCounts(
        effects: List<EffectDefinition>,
        categories: List<String>,
        query: String = ""
    ): List<CategoryPill> {
        val trimmed = query.trim()
        val matchingEffects = if (trimmed.isEmpty()) {
            effects
        } else {
            effects.filter {
                it.name.contains(trimmed, ignoreCase = true) ||
                    it.basedOn.contains(trimmed, ignoreCase = true) ||
                    it.category.contains(trimmed, ignoreCase = true)
            }
        }

        val allPill = CategoryPill(ALL_CATEGORY, matchingEffects.size)
        val categoryPills = categories.map { category ->
            CategoryPill(
                category = category,
                count = matchingEffects.count { it.category.equals(category, ignoreCase = true) }
            )
        }
        return listOf(allPill) + categoryPills
    }

    /**
     * Groups effects by category, preserving [categoryOrder] if provided.
     */
    fun groupByCategory(
        effects: List<EffectDefinition>,
        categoryOrder: List<String> = emptyList()
    ): Map<String, List<EffectDefinition>> {
        val grouped = effects.groupBy { it.category }
        if (categoryOrder.isEmpty()) return grouped

        val ordered = LinkedHashMap<String, List<EffectDefinition>>()
        for (cat in categoryOrder) {
            grouped[cat]?.let { ordered[cat] = it }
        }
        for ((cat, list) in grouped) {
            if (!ordered.containsKey(cat)) {
                ordered[cat] = list
            }
        }
        return ordered
    }

    fun effectsInDisplayOrder(
        effects: List<EffectDefinition>,
        categories: List<String>
    ): List<EffectDefinition> = groupByCategory(effects, categories).values.flatten()

    fun adjacentEffectId(
        effects: List<EffectDefinition>,
        currentEffectId: Int,
        step: Int
    ): Int? {
        require(step == -1 || step == 1) { "step must be -1 or 1" }
        if (effects.isEmpty()) return null

        val currentIndex = effects.indexOfFirst { it.effectId == currentEffectId }
        if (currentIndex < 0) return null

        val nextIndex = Math.floorMod(currentIndex + step, effects.size)
        return effects[nextIndex].effectId
    }

    fun pickerItemIndex(
        effects: List<EffectDefinition>,
        categories: List<String>,
        effectId: Int
    ): Int? {
        var itemIndex = 0
        for (effectsInCategory in groupByCategory(effects, categories).values) {
            if (effectsInCategory.isEmpty()) continue
            itemIndex++ // Category header

            val effectOffset = effectsInCategory.indexOfFirst { it.effectId == effectId }
            if (effectOffset >= 0) return itemIndex + effectOffset
            itemIndex += effectsInCategory.size
        }
        return null
    }

    fun scrollDeltaToCenterItem(
        itemOffset: Int,
        itemSize: Int,
        viewportStart: Int,
        viewportEnd: Int
    ): Float {
        val itemCenter = itemOffset + itemSize / 2f
        val viewportCenter = (viewportStart + viewportEnd) / 2f
        return itemCenter - viewportCenter
    }
}

private val SheetBackground = StudioPalette.Surface
private val SearchBackground = StudioPalette.Case

@Composable
fun EffectPickerSheet(
    isOpen: Boolean,
    module: ModuleType,
    currentEffectId: Int,
    onSelectEffect: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val allModuleEffects = remember(module) {
        EffectCatalog.getEffectsByModule(module)
    }
    val moduleCategories = remember(module) {
        EffectCatalog.getCategoriesForModule(module)
    }

    val accentColor = remember(module) {
        try {
            Color(android.graphics.Color.parseColor(module.colorHex))
        } catch (_: Exception) {
            ValetonBlue
        }
    }

    var searchQuery by remember(isOpen, module) { mutableStateOf("") }
    var selectedCategory by remember(isOpen, module) { mutableStateOf(EffectPickerLogic.ALL_CATEGORY) }

    val categoryPills = remember(allModuleEffects, moduleCategories, searchQuery) {
        EffectPickerLogic.buildCategoryCounts(allModuleEffects, moduleCategories, searchQuery)
    }

    val filteredEffects = remember(allModuleEffects, searchQuery, selectedCategory) {
        EffectPickerLogic.filterEffects(allModuleEffects, searchQuery, selectedCategory)
    }

    val groupedEffects = remember(filteredEffects, moduleCategories) {
        EffectPickerLogic.groupByCategory(filteredEffects, moduleCategories)
    }

    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Auto-scroll to and center the selected effect when opening or changing the list filter.
    LaunchedEffect(isOpen, currentEffectId, filteredEffects, groupedEffects) {
        if (isOpen) {
            val targetIndex = EffectPickerLogic.pickerItemIndex(
                effects = filteredEffects,
                categories = moduleCategories,
                effectId = currentEffectId
            )
            if (targetIndex != null) {
                listState.animateScrollToItem(targetIndex)
                withFrameNanos { }

                val layoutInfo = listState.layoutInfo
                val selectedItem = layoutInfo.visibleItemsInfo.firstOrNull { it.key == currentEffectId }
                if (selectedItem != null) {
                    val scrollDelta = EffectPickerLogic.scrollDeltaToCenterItem(
                        itemOffset = selectedItem.offset,
                        itemSize = selectedItem.size,
                        viewportStart = layoutInfo.viewportStartOffset,
                        viewportEnd = layoutInfo.viewportEndOffset
                    )
                    if (scrollDelta != 0f) listState.animateScrollBy(scrollDelta)
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth(0.95f)
                    .widthIn(max = 600.dp)
                    .fillMaxHeight(0.92f)
                    .clip(RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectTapGestures { /* consume tap inside sheet without accessibility semantics */ }
                    },
                shape = RoundedCornerShape(16.dp),
                color = SheetBackground,
                border = BorderStroke(1.dp, BorderDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .background(SheetBackground)
                ) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TROCAR EFEITO",
                                color = TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = module.displayName,
                                color = accentColor,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Fechar",
                                tint = TextSecondary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        placeholder = {
                            Text(
                                text = "Buscar efeitos ou equipamentos...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar efeitos",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Limpar busca",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SearchBackground,
                            unfocusedContainerColor = SearchBackground,
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = BorderDark,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = accentColor
                        )
                    )

                    // Category Rail
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categoryPills, key = { it.category }) { pill ->
                            val isSelected = pill.category.equals(selectedCategory, ignoreCase = true)
                            val pillBg = if (isSelected) accentColor.copy(alpha = 0.22f) else SurfaceVariantDark
                            val pillBorder = if (isSelected) accentColor else BorderDark
                            val textColor = if (isSelected) accentColor else TextSecondary
                            val fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(pillBg)
                                    .border(1.dp, pillBorder, RoundedCornerShape(16.dp))
                                    .clickable {
                                        selectedCategory = pill.category
                                    }
                                    .sizeIn(minHeight = 48.dp)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${if (pill.category.equals(EffectPickerLogic.ALL_CATEGORY, ignoreCase = true)) "Todos" else pill.category} ${pill.count}",
                                    color = textColor,
                                    fontSize = 12.sp,
                                    fontWeight = fontWeight
                                )
                            }
                        }
                    }

                    // Content: Empty state or Grouped list
                    if (filteredEffects.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Nenhum efeito encontrado",
                                    color = TextSecondary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tente buscar outro nome ou equipamento",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            val centerPadding = maxHeight / 2
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = centerPadding,
                                    bottom = centerPadding
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                groupedEffects.forEach { (category, effectsInCategory) ->
                                    item(key = "header_$category") {
                                        Text(
                                            text = "${category.uppercase()} ${effectsInCategory.size}",
                                            color = TextMuted,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp, bottom = 4.dp)
                                        )
                                    }

                                    items(effectsInCategory, key = { it.effectId }) { effect ->
                                        val isCurrent = effect.effectId == currentEffectId
                                        val itemBorder = if (isCurrent) accentColor else BorderDark
                                        val itemBg = if (isCurrent) accentColor.copy(alpha = 0.12f) else SurfaceVariantDark

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(itemBg)
                                                .border(1.dp, itemBorder, RoundedCornerShape(8.dp))
                                                .clickable {
                                                    onSelectEffect(effect.effectId)
                                                    onDismiss()
                                                }
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            PedalArtwork(
                                                svgFile = effect.svgFile,
                                                name = effect.name,
                                                accentColor = accentColor,
                                                modifier = Modifier
                                                    .size(80.dp, 44.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                            )

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = effect.name,
                                                    color = TextPrimary,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (effect.basedOn.isNotBlank()) {
                                                    Text(
                                                        text = effect.basedOn,
                                                        color = TextSecondary,
                                                        fontSize = 12.sp,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(8.dp))

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(SurfaceElevated)
                                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                                            ) {
                                                Text(
                                                    text = effect.category,
                                                    color = TextMuted,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
