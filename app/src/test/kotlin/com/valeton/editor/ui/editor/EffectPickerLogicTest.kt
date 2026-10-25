package com.valeton.editor.ui.editor

import com.valeton.editor.core.catalog.EffectCatalog
import com.valeton.editor.core.model.EffectDefinition
import com.valeton.editor.core.model.ModuleType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EffectPickerLogicTest {

    private val ampEffects = EffectCatalog.getEffectsByModule(ModuleType.AMP)
    private val ampCategories = EffectCatalog.getCategoriesForModule(ModuleType.AMP)
    private val navigationEffects = listOf(
        EffectDefinition(11, "Alpha", ModuleType.DST, category = "A"),
        EffectDefinition(21, "Beta", ModuleType.DST, category = "B"),
        EffectDefinition(12, "Zulu", ModuleType.DST, category = "A")
    )
    private val navigationCategories = listOf("A", "B")

    @Test
    fun `filtering by name matches effects case-insensitively`() {
        val foxyResults = EffectPickerLogic.filterEffects(ampEffects, query = "FOXY")
        assertTrue(foxyResults.isNotEmpty(), "Should find effects matching FOXY")
        assertTrue(foxyResults.any { it.name == "Foxy Bass" })
        assertTrue(foxyResults.all {
            it.name.contains("foxy", ignoreCase = true) ||
                it.basedOn.contains("foxy", ignoreCase = true) ||
                it.category.contains("foxy", ignoreCase = true)
        })

        val foxyBassOnly = EffectPickerLogic.filterEffects(ampEffects, query = "Foxy Bass")
        assertEquals(1, foxyBassOnly.size)
        assertEquals("Foxy Bass", foxyBassOnly.first().name)
    }

    @Test
    fun `filtering by gear or basedOn returns simulated equipment`() {
        // Search by gear brand "Alembic" -> should find "Bass Pre"
        val alembicResults = EffectPickerLogic.filterEffects(ampEffects, query = "Alembic")
        assertTrue(alembicResults.isNotEmpty(), "Should find gear based on Alembic")
        assertTrue(alembicResults.any { it.name == "Bass Pre" })
        assertTrue(alembicResults.all { it.basedOn.contains("Alembic", ignoreCase = true) })

        // Search by gear brand "VOX" -> should find Foxy Bass (VOX AC-100) and UK 30 / UK models
        val voxResults = EffectPickerLogic.filterEffects(ampEffects, query = "VOX")
        assertTrue(voxResults.isNotEmpty(), "Should find gear based on VOX")
        assertTrue(voxResults.any { it.name == "Foxy Bass" })
        assertTrue(voxResults.any { it.basedOn.contains("VOX", ignoreCase = true) })
    }

    @Test
    fun `category filtering returns only effects in the chosen category`() {
        val bassAmps = EffectPickerLogic.filterEffects(
            effects = ampEffects,
            query = "",
            selectedCategory = "Bass Amp"
        )
        assertEquals(5, bassAmps.size, "GP-200 AMP module has exactly 5 Bass Amp models")
        assertTrue(bassAmps.all { it.category == "Bass Amp" })
        val names = bassAmps.map { it.name }
        assertTrue(names.contains("Classic Bass"))
        assertTrue(names.contains("Foxy Bass"))
        assertTrue(names.contains("Mess Bass"))
        assertTrue(names.contains("Mini Bass"))
        assertTrue(names.contains("Bass Pre"))

        // Combining category with query
        val filtered = EffectPickerLogic.filterEffects(
            effects = ampEffects,
            query = "Alembic",
            selectedCategory = "Bass Amp"
        )
        assertEquals(1, filtered.size)
        assertEquals("Bass Pre", filtered.first().name)

        // Combining with non-matching query returns empty list
        val empty = EffectPickerLogic.filterEffects(
            effects = ampEffects,
            query = "Marshall",
            selectedCategory = "Bass Amp"
        )
        assertTrue(empty.isEmpty())
    }

    @Test
    fun `category count calculation accurately counts total and per-category items`() {
        val pills = EffectPickerLogic.buildCategoryCounts(
            effects = ampEffects,
            categories = ampCategories,
            query = ""
        )

        // First pill is "All" with total count
        assertEquals("All", pills.first().category)
        assertEquals(ampEffects.size, pills.first().count)

        // Bass Amp pill has count of 5
        val bassAmpPill = pills.firstOrNull { it.category == "Bass Amp" }
        assertNotNull(bassAmpPill)
        assertEquals(5, bassAmpPill?.count)

        // Total of individual category counts equals total
        val individualSum = pills.drop(1).sumOf { it.count }
        assertEquals(ampEffects.size, individualSum)

        // With query active, pills show filtered counts
        val filteredPills = EffectPickerLogic.buildCategoryCounts(
            effects = ampEffects,
            categories = ampCategories,
            query = "Alembic"
        )
        assertEquals(1, filteredPills.first().count) // All = 1
        val filteredBassAmp = filteredPills.firstOrNull { it.category == "Bass Amp" }
        assertEquals(1, filteredBassAmp?.count)
        val cleanAmpPill = filteredPills.firstOrNull { it.category == "Clean Amp" }
        assertEquals(0, cleanAmpPill?.count)
    }

    @Test
    fun `groupByCategory groups effects according to provided categories`() {
        val grouped = EffectPickerLogic.groupByCategory(ampEffects, ampCategories)
        assertTrue(grouped.containsKey("Bass Amp"))
        assertEquals(5, grouped["Bass Amp"]?.size)
        // Order of keys should match ampCategories
        assertEquals(ampCategories.filter { cat -> ampEffects.any { it.category == cat } }, grouped.keys.toList())
    }

    @Test
    fun `display order respects category order and sorted names`() {
        val ordered = EffectPickerLogic.effectsInDisplayOrder(navigationEffects, navigationCategories)

        assertEquals(listOf(11, 12, 21), ordered.map { it.effectId })
    }

    @Test
    fun `adjacent effect wraps and crosses category boundaries`() {
        val ordered = EffectPickerLogic.effectsInDisplayOrder(navigationEffects, navigationCategories)

        assertEquals(21, EffectPickerLogic.adjacentEffectId(ordered, currentEffectId = 12, step = 1))
        assertEquals(11, EffectPickerLogic.adjacentEffectId(ordered, currentEffectId = 21, step = 1))
        assertEquals(21, EffectPickerLogic.adjacentEffectId(ordered, currentEffectId = 11, step = -1))
        assertEquals(12, EffectPickerLogic.adjacentEffectId(ordered, currentEffectId = 21, step = -1))
    }

    @Test
    fun `adjacent effect returns null when list is empty or current ID is absent`() {
        assertNull(EffectPickerLogic.adjacentEffectId(emptyList(), currentEffectId = 1, step = 1))
        assertNull(EffectPickerLogic.adjacentEffectId(navigationEffects, currentEffectId = 999, step = 1))
    }

    @Test
    fun `single-effect list cycles to itself`() {
        val onlyEffect = navigationEffects.take(1)

        assertEquals(11, EffectPickerLogic.adjacentEffectId(onlyEffect, currentEffectId = 11, step = 1))
        assertEquals(11, EffectPickerLogic.adjacentEffectId(onlyEffect, currentEffectId = 11, step = -1))
    }

    @Test
    fun `picker item index accounts for category headers`() {
        assertEquals(1, EffectPickerLogic.pickerItemIndex(navigationEffects, navigationCategories, effectId = 11))
        assertEquals(2, EffectPickerLogic.pickerItemIndex(navigationEffects, navigationCategories, effectId = 12))
        assertEquals(4, EffectPickerLogic.pickerItemIndex(navigationEffects, navigationCategories, effectId = 21))
        assertNull(EffectPickerLogic.pickerItemIndex(navigationEffects, navigationCategories, effectId = 999))
    }

    @Test
    fun `scroll delta moves item center toward viewport center`() {
        assertEquals(0f, EffectPickerLogic.scrollDeltaToCenterItem(80, 40, 0, 200))
        assertEquals(-80f, EffectPickerLogic.scrollDeltaToCenterItem(10, 20, 0, 200))
        assertEquals(70f, EffectPickerLogic.scrollDeltaToCenterItem(160, 20, 0, 200))
    }
}
