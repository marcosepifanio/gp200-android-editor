package com.valeton.editor.ui.drums

import com.valeton.editor.core.catalog.DrumCatalog
import com.valeton.editor.core.model.DrumGenre
import com.valeton.editor.core.model.DrumPattern
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StudioDrumScreenLogicTest {

    private val samplePatterns = listOf(
        DrumPattern(0, "Classic Rock 1", DrumGenre.ROCK, "4/4"),
        DrumPattern(8, "Hard Rock 3", DrumGenre.ROCK, "3/4"),
        DrumPattern(21, "Heavy Metal 1", DrumGenre.ROCK, "4/4"),
        DrumPattern(30, "Funk 1", DrumGenre.FUNK, "4/4"),
        DrumPattern(42, "Shuffle 3/4", DrumGenre.BLUES, "3/4"),
        DrumPattern(46, "Pop 1", DrumGenre.POP, "4/4"),
        DrumPattern(81, "Waltz", DrumGenre.WORLD, "3/4"),
        DrumPattern(93, "Metro 3/4", DrumGenre.METRO, "3/4")
    )

    @Test
    fun `filterPatterns with ALL genre and empty query returns all patterns`() {
        val result = filterPatterns(samplePatterns, DrumGenre.ALL, "")
        assertEquals(samplePatterns.size, result.size)
        assertEquals(samplePatterns, result)
    }

    @Test
    fun `filterPatterns with specific genre returns only patterns of that genre`() {
        val rockPatterns = filterPatterns(samplePatterns, DrumGenre.ROCK, "")
        assertEquals(3, rockPatterns.size)
        assertTrue(rockPatterns.all { it.genre == DrumGenre.ROCK })

        val funkPatterns = filterPatterns(samplePatterns, DrumGenre.FUNK, "")
        assertEquals(1, funkPatterns.size)
        assertEquals("Funk 1", funkPatterns.first().name)
    }

    @Test
    fun `filterPatterns with search query returns matching patterns by name or time signature`() {
        // Query matching time signature
        val threeFourPatterns = filterPatterns(samplePatterns, DrumGenre.ALL, "3/4")
        assertEquals(4, threeFourPatterns.size)
        assertTrue(threeFourPatterns.all { it.timeSignature.contains("3/4") || it.name.contains("3/4") })

        // Query matching name (case-insensitive)
        val metalPatterns = filterPatterns(samplePatterns, DrumGenre.ALL, "metal")
        assertEquals(1, metalPatterns.size)
        assertEquals("Heavy Metal 1", metalPatterns.first().name)
    }

    @Test
    fun `filterPatterns combining genre and query filters by both criteria`() {
        // Search "3/4" inside ROCK genre only
        val rockThreeFour = filterPatterns(samplePatterns, DrumGenre.ROCK, "3/4")
        assertEquals(1, rockThreeFour.size)
        assertEquals("Hard Rock 3", rockThreeFour.first().name)

        // Search "Metal" inside FUNK genre (should be empty)
        val funkMetal = filterPatterns(samplePatterns, DrumGenre.FUNK, "Metal")
        assertTrue(funkMetal.isEmpty())
    }

    @Test
    fun `filterPatterns on full DrumCatalog matches expected counts`() {
        val allPatterns = DrumCatalog.getAllPatterns()
        assertEquals(100, filterPatterns(allPatterns, DrumGenre.ALL, "").size)
        assertEquals(30, filterPatterns(allPatterns, DrumGenre.ROCK, "").size)
        assertEquals(7, filterPatterns(allPatterns, DrumGenre.FUNK, "").size)
        assertEquals(10, filterPatterns(allPatterns, DrumGenre.BLUES, "").size)
        assertEquals(8, filterPatterns(allPatterns, DrumGenre.POP, "").size)
        assertEquals(7, filterPatterns(allPatterns, DrumGenre.JAZZ, "").size)
        assertEquals(7, filterPatterns(allPatterns, DrumGenre.ELECTRONIC, "").size)
        assertEquals(21, filterPatterns(allPatterns, DrumGenre.WORLD, "").size)
        assertEquals(10, filterPatterns(allPatterns, DrumGenre.METRO, "").size)
    }

    @Test
    fun `calculateGenreCounts returns accurate counts for ALL and each genre with empty query`() {
        val counts = calculateGenreCounts(samplePatterns, "")
        assertEquals(samplePatterns.size, counts[DrumGenre.ALL])
        assertEquals(3, counts[DrumGenre.ROCK])
        assertEquals(1, counts[DrumGenre.FUNK])
        assertEquals(1, counts[DrumGenre.BLUES])
        assertEquals(1, counts[DrumGenre.POP])
        assertEquals(0, counts[DrumGenre.JAZZ])
        assertEquals(0, counts[DrumGenre.ELECTRONIC])
        assertEquals(1, counts[DrumGenre.WORLD])
        assertEquals(1, counts[DrumGenre.METRO])
    }

    @Test
    fun `calculateGenreCounts reflects query filtering across genres`() {
        val counts = calculateGenreCounts(samplePatterns, "3/4")
        // In samplePatterns:
        // Hard Rock 3 (ROCK), Shuffle 3/4 (BLUES), Waltz (WORLD), Metro 3/4 (METRO) -> total 4
        assertEquals(4, counts[DrumGenre.ALL])
        assertEquals(1, counts[DrumGenre.ROCK])
        assertEquals(0, counts[DrumGenre.FUNK])
        assertEquals(1, counts[DrumGenre.BLUES])
        assertEquals(0, counts[DrumGenre.POP])
        assertEquals(0, counts[DrumGenre.JAZZ])
        assertEquals(0, counts[DrumGenre.ELECTRONIC])
        assertEquals(1, counts[DrumGenre.WORLD])
        assertEquals(1, counts[DrumGenre.METRO])
    }

    @Test
    fun `filterPatterns and calculateGenreCounts handle leading and trailing whitespace in query`() {
        val rockWithSpaces = filterPatterns(samplePatterns, DrumGenre.ALL, " Rock ")
        assertEquals(2, rockWithSpaces.size)
        assertTrue(rockWithSpaces.all { it.name.contains("Rock", ignoreCase = true) })

        val rockTrailing = filterPatterns(samplePatterns, DrumGenre.ALL, "Rock ")
        assertEquals(2, rockTrailing.size)

        val threeFourWithSpaces = filterPatterns(samplePatterns, DrumGenre.ALL, " 3/4 ")
        assertEquals(4, threeFourWithSpaces.size)
        assertTrue(threeFourWithSpaces.all { it.timeSignature == "3/4" })

        val whitespaceOnly = filterPatterns(samplePatterns, DrumGenre.ALL, "   ")
        assertEquals(samplePatterns.size, whitespaceOnly.size)

        val counts = calculateGenreCounts(samplePatterns, " 3/4 ")
        assertEquals(4, counts[DrumGenre.ALL])
        assertEquals(1, counts[DrumGenre.ROCK])
        assertEquals(0, counts[DrumGenre.FUNK])
        assertEquals(1, counts[DrumGenre.BLUES])
        assertEquals(1, counts[DrumGenre.WORLD])
        assertEquals(1, counts[DrumGenre.METRO])

        val countsWhitespaceOnly = calculateGenreCounts(samplePatterns, "   ")
        assertEquals(samplePatterns.size, countsWhitespaceOnly[DrumGenre.ALL])
    }
}
