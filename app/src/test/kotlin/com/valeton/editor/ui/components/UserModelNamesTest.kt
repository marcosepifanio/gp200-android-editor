package com.valeton.editor.ui.components

import com.valeton.editor.core.catalog.EffectCatalog
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserModelNamesTest {

    private val userIr1 = EffectCatalog.getEffectById(168820736)!!

    @Test
    fun `user name replaces catalog name for IR`() {
        assertEquals("ir1", effectDisplayName(userIr1, mapOf(168820736 to "ir1")))
    }

    @Test
    fun `blank user name falls back to catalog name`() {
        assertEquals(userIr1.name, effectDisplayName(userIr1, mapOf(168820736 to "  ")))
    }

    @Test
    fun `effects without user name keep catalog name`() {
        val other = EffectCatalog.getEffectById(168820737)!!
        assertEquals(other.name, effectDisplayName(other, mapOf(168820736 to "ir1")))
    }
}
