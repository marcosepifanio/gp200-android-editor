package com.valeton.editor.core.catalog

import com.valeton.editor.core.model.DrumGenre
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DrumCatalogTest {

    @Test
    fun `catalog contains exactly 100 patterns with valid sequential ids 0 to 99`() {
        val patterns = DrumCatalog.getAllPatterns()
        assertEquals(100, patterns.size)

        for (i in 0 until 100) {
            val pattern = DrumCatalog.getPatternById(i)
            assertNotNull(pattern, "Pattern $i should exist")
            assertEquals(i, pattern?.id)
            assertTrue(pattern!!.name.isNotEmpty())
            assertTrue(pattern.timeSignature.isNotEmpty())
        }
    }

    @Test
    fun `patterns are categorized correctly across genres`() {
        assertEquals(30, DrumCatalog.getPatternsByGenre(DrumGenre.ROCK).size)
        assertEquals(7, DrumCatalog.getPatternsByGenre(DrumGenre.FUNK).size)
        assertEquals(10, DrumCatalog.getPatternsByGenre(DrumGenre.BLUES).size)
        assertEquals(8, DrumCatalog.getPatternsByGenre(DrumGenre.POP).size)
        assertEquals(7, DrumCatalog.getPatternsByGenre(DrumGenre.JAZZ).size)
        assertEquals(7, DrumCatalog.getPatternsByGenre(DrumGenre.ELECTRONIC).size)
        assertEquals(21, DrumCatalog.getPatternsByGenre(DrumGenre.WORLD).size)
        assertEquals(10, DrumCatalog.getPatternsByGenre(DrumGenre.METRO).size)
        assertEquals(100, DrumCatalog.getPatternsByGenre(DrumGenre.ALL).size)
    }

    @Test
    fun `search filters patterns by query and genre`() {
        val rockSearch = DrumCatalog.searchPatterns("Metal", DrumGenre.ALL)
        assertTrue(rockSearch.isNotEmpty())
        assertTrue(rockSearch.all { it.name.contains("Metal", ignoreCase = true) })

        val punkInRock = DrumCatalog.searchPatterns("Punk", DrumGenre.ROCK)
        assertEquals(6, punkInRock.size) // 4 Punk + 2 Post Punk

        val waltz = DrumCatalog.searchPatterns("Waltz", DrumGenre.ALL)
        assertEquals(1, waltz.size)
        assertEquals("3/4", waltz.first().timeSignature)

        val pubSearch = DrumCatalog.searchPatterns("Pub", DrumGenre.POP)
        assertEquals(1, pubSearch.size)
        assertEquals("Pub", pubSearch.first().name)
        assertEquals(54, pubSearch.first().id)

        val pop1 = DrumCatalog.getPatternById(47)
        assertEquals("Pop 1", pop1?.name)
        val pop2 = DrumCatalog.getPatternById(48)
        assertEquals("Pop 2", pop2?.name)
    }
}
