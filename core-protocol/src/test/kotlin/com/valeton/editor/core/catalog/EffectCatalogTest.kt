package com.valeton.editor.core.catalog

import com.valeton.editor.core.model.ModuleType
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EffectCatalogTest {

    @Test
    fun `catalog contains exactly 305 effects with parameter definitions`() {
        val effects = EffectCatalog.getAllEffects()
        assertEquals(305, effects.size, "EffectCatalog must contain all 305 GP-200 effects")
        
        // Test known amplifier: UK 800 (id: 117440565)
        val amp = EffectCatalog.getEffectById(117440565)
        assertNotNull(amp)
        assertEquals("UK 800", amp?.name)
        assertEquals(ModuleType.AMP, amp?.module)
        assertTrue(amp!!.params.isNotEmpty(), "AMP must have parameters")
        
        // Test known overdrive: Tube Clean (id: 0)
        val comp = EffectCatalog.getEffectById(0)
        assertNotNull(comp)
        assertEquals("COMP", comp?.name)
        assertEquals(ModuleType.PRE, comp?.module)
        assertEquals(2, comp?.params?.size)
    }

    @Test
    fun `getEffectsByModule returns correct filtered list`() {
        val drives = EffectCatalog.getEffectsByModule(ModuleType.DST)
        assertTrue(drives.isNotEmpty())
        assertTrue(drives.all { it.module == ModuleType.DST })
    }

    @Test
    fun `getModuleName returns Unknown for non-existent effect code`() {
        val unknown = EffectCatalog.getEffectById(999999)
        assertNull(unknown)
        val module = EffectCatalog.getModuleName(999999)
        assertEquals(ModuleType.UNKNOWN, module)
    }

    @Test
    fun `ModuleType fromSlotIndex returns exact canonical modules for 11 GP-200 slots`() {
        assertEquals(ModuleType.PRE, ModuleType.fromSlotIndex(0))
        assertEquals(ModuleType.WAH, ModuleType.fromSlotIndex(1))
        assertEquals(ModuleType.DST, ModuleType.fromSlotIndex(2))
        assertEquals(ModuleType.AMP, ModuleType.fromSlotIndex(3))
        assertEquals(ModuleType.NR, ModuleType.fromSlotIndex(4))
        assertEquals(ModuleType.CAB, ModuleType.fromSlotIndex(5))
        assertEquals(ModuleType.EQ, ModuleType.fromSlotIndex(6))
        assertEquals(ModuleType.MOD, ModuleType.fromSlotIndex(7))
        assertEquals(ModuleType.DLY, ModuleType.fromSlotIndex(8))
        assertEquals(ModuleType.RVB, ModuleType.fromSlotIndex(9))
        assertEquals(ModuleType.VOL, ModuleType.fromSlotIndex(10))
        assertEquals(ModuleType.UNKNOWN, ModuleType.fromSlotIndex(11))
    }

    @Test
    fun `effects have populated categories and basedOn gear names`() {
        val bassPre = EffectCatalog.getEffectById(134217846) // Bass Pre
        assertNotNull(bassPre)
        assertEquals("Bass Amp", bassPre?.category)
        assertTrue(bassPre?.basedOn?.contains("Alembic") == true)
        assertEquals("bass-pre.svg", bassPre?.svgFile)

        val uk45 = EffectCatalog.getEffectById(117440554) // UK 45
        assertNotNull(uk45)
        assertEquals("Drive Amp", uk45?.category)
        assertTrue(uk45?.basedOn?.contains("Marshall") == true)
        assertEquals("uk-45.svg", uk45?.svgFile)

        // Check all effects have category, basedOn and svgFile populated
        val all = EffectCatalog.getAllEffects()
        assertTrue(all.all { it.category.isNotEmpty() })
        assertTrue(all.all { it.basedOn.isNotEmpty() })
        assertTrue(all.all { it.category != "Other" || it.name == "Other" })
        assertTrue(all.all { it.svgFile.isNotEmpty() })
    }

    @Test
    fun `getCategoriesForModule returns sorted categories with Other at the end`() {
        val ampCategories = EffectCatalog.getCategoriesForModule(ModuleType.AMP)
        assertEquals(
            listOf("Acoustic Preamp", "Bass Amp", "Clean Amp", "Drive Amp", "Hi-Gain Amp", "NAM Player"),
            ampCategories
        )

        val preCategories = EffectCatalog.getCategoriesForModule(ModuleType.PRE)
        assertEquals(
            listOf("Acoustic", "Boost", "Compressor", "Filter", "Overdrive", "Pitch", "Special"),
            preCategories
        )
    }
}
