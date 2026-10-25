package com.valeton.editor.core.catalog

import com.valeton.editor.core.model.DrumGenre
import com.valeton.editor.core.model.DrumPattern

object DrumCatalog {

    private val patterns: List<DrumPattern> = listOf(
        // Rock (0..29) - 30 patterns
        DrumPattern(0, "Classic Rock 1", DrumGenre.ROCK, "4/4"),
        DrumPattern(1, "Classic Rock 2", DrumGenre.ROCK, "4/4"),
        DrumPattern(2, "Classic Rock 3", DrumGenre.ROCK, "4/4"),
        DrumPattern(3, "Classic Rock 4", DrumGenre.ROCK, "4/4"),
        DrumPattern(4, "Classic Rock 5", DrumGenre.ROCK, "4/4"),
        DrumPattern(5, "Classic Rock 6", DrumGenre.ROCK, "4/4"),
        DrumPattern(6, "Hard Rock 1", DrumGenre.ROCK, "4/4"),
        DrumPattern(7, "Hard Rock 2", DrumGenre.ROCK, "4/4"),
        DrumPattern(8, "Hard Rock 3", DrumGenre.ROCK, "3/4"),
        DrumPattern(9, "Post Rock 1", DrumGenre.ROCK, "5/4"),
        DrumPattern(10, "Post Rock 2", DrumGenre.ROCK, "4/4"),
        DrumPattern(11, "Post Rock 3", DrumGenre.ROCK, "4/4"),
        DrumPattern(12, "Garage Rock", DrumGenre.ROCK, "4/4"),
        DrumPattern(13, "Prog Rock", DrumGenre.ROCK, "4/4"),
        DrumPattern(14, "Surf Rock", DrumGenre.ROCK, "4/4"),
        DrumPattern(15, "Punk 1", DrumGenre.ROCK, "4/4"),
        DrumPattern(16, "Punk 2", DrumGenre.ROCK, "4/4"),
        DrumPattern(17, "Punk 3", DrumGenre.ROCK, "4/4"),
        DrumPattern(18, "Punk 4", DrumGenre.ROCK, "4/4"),
        DrumPattern(19, "Post Punk 1", DrumGenre.ROCK, "4/4"),
        DrumPattern(20, "Post Punk 2", DrumGenre.ROCK, "4/4"),
        DrumPattern(21, "Heavy Metal 1", DrumGenre.ROCK, "4/4"),
        DrumPattern(22, "Heavy Metal 2", DrumGenre.ROCK, "4/4"),
        DrumPattern(23, "Nu-Metal 1", DrumGenre.ROCK, "4/4"),
        DrumPattern(24, "Nu-Metal 2", DrumGenre.ROCK, "4/4"),
        DrumPattern(25, "Hardcore", DrumGenre.ROCK, "4/4"),
        DrumPattern(26, "EMO", DrumGenre.ROCK, "4/4"),
        DrumPattern(27, "Grunge", DrumGenre.ROCK, "4/4"),
        DrumPattern(28, "New Wave", DrumGenre.ROCK, "4/4"),
        DrumPattern(29, "Rock 5/4", DrumGenre.ROCK, "5/4"),

        // Funk (30..36) - 7 patterns
        DrumPattern(30, "Funk 1", DrumGenre.FUNK, "4/4"),
        DrumPattern(31, "Funk 2", DrumGenre.FUNK, "4/4"),
        DrumPattern(32, "Funk 3", DrumGenre.FUNK, "4/4"),
        DrumPattern(33, "Funk 4", DrumGenre.FUNK, "4/4"),
        DrumPattern(34, "Jazz Funk 1", DrumGenre.FUNK, "4/4"),
        DrumPattern(35, "Jazz Funk 2", DrumGenre.FUNK, "4/4"),
        DrumPattern(36, "Jazz Funk 3", DrumGenre.FUNK, "4/4"),

        // Blues (37..46) - 10 patterns
        DrumPattern(37, "Blues 1", DrumGenre.BLUES, "4/4"),
        DrumPattern(38, "Blues 2", DrumGenre.BLUES, "4/4"),
        DrumPattern(39, "Blues 3", DrumGenre.BLUES, "4/4"),
        DrumPattern(40, "Blues 4", DrumGenre.BLUES, "4/4"),
        DrumPattern(41, "Swing", DrumGenre.BLUES, "4/4"),
        DrumPattern(42, "Shuffle", DrumGenre.BLUES, "3/4"),
        DrumPattern(43, "Shuffle 3/4", DrumGenre.BLUES, "3/4"),
        DrumPattern(44, "Bluegrass", DrumGenre.BLUES, "4/4"),
        DrumPattern(45, "Country", DrumGenre.BLUES, "4/4"),
        DrumPattern(46, "Country Folk", DrumGenre.BLUES, "4/4"),

        // Pop (47..54) - 8 patterns
        DrumPattern(47, "Pop 1", DrumGenre.POP, "4/4"),
        DrumPattern(48, "Pop 2", DrumGenre.POP, "4/4"),
        DrumPattern(49, "Pop 3", DrumGenre.POP, "4/4"),
        DrumPattern(50, "Hip Hop 1", DrumGenre.POP, "4/4"),
        DrumPattern(51, "Hip Hop 2", DrumGenre.POP, "4/4"),
        DrumPattern(52, "Hip Hop 3", DrumGenre.POP, "4/4"),
        DrumPattern(53, "Hip Hop Rock", DrumGenre.POP, "4/4"),
        DrumPattern(54, "Pub", DrumGenre.POP, "4/4"),

        // Jazz (55..61) - 7 patterns
        DrumPattern(55, "Jazz 1", DrumGenre.JAZZ, "4/4"),
        DrumPattern(56, "Jazz 2", DrumGenre.JAZZ, "4/4"),
        DrumPattern(57, "Jazz 3", DrumGenre.JAZZ, "4/4"),
        DrumPattern(58, "Jazz 4", DrumGenre.JAZZ, "4/4"),
        DrumPattern(59, "Bossanova 1", DrumGenre.JAZZ, "4/4"),
        DrumPattern(60, "Bossanova 2", DrumGenre.JAZZ, "4/4"),
        DrumPattern(61, "Fusion", DrumGenre.JAZZ, "4/4"),

        // Electronic (62..68) - 7 patterns
        DrumPattern(62, "Electro 1", DrumGenre.ELECTRONIC, "4/4"),
        DrumPattern(63, "Electro 2", DrumGenre.ELECTRONIC, "4/4"),
        DrumPattern(64, "Techno", DrumGenre.ELECTRONIC, "4/4"),
        DrumPattern(65, "TripHop", DrumGenre.ELECTRONIC, "4/4"),
        DrumPattern(66, "Electronic Pop", DrumGenre.ELECTRONIC, "4/4"),
        DrumPattern(67, "Break Beat", DrumGenre.ELECTRONIC, "4/4"),
        DrumPattern(68, "Drum&Bass", DrumGenre.ELECTRONIC, "4/4"),

        // World / Latin (69..89) - 21 patterns
        DrumPattern(69, "Latin 1", DrumGenre.WORLD, "4/4"),
        DrumPattern(70, "Latin 2", DrumGenre.WORLD, "4/4"),
        DrumPattern(71, "Latin 3", DrumGenre.WORLD, "4/4"),
        DrumPattern(72, "Latin Pop 1", DrumGenre.WORLD, "4/4"),
        DrumPattern(73, "Latin Pop 2", DrumGenre.WORLD, "4/4"),
        DrumPattern(74, "Samba", DrumGenre.WORLD, "4/4"),
        DrumPattern(75, "Tango", DrumGenre.WORLD, "4/4"),
        DrumPattern(76, "Beguine", DrumGenre.WORLD, "4/4"),
        DrumPattern(77, "Ska", DrumGenre.WORLD, "4/4"),
        DrumPattern(78, "Polka", DrumGenre.WORLD, "2/4"),
        DrumPattern(79, "Waltz", DrumGenre.WORLD, "3/4"),
        DrumPattern(80, "Reggae 1", DrumGenre.WORLD, "4/4"),
        DrumPattern(81, "Reggae 2", DrumGenre.WORLD, "4/4"),
        DrumPattern(82, "Mazuke", DrumGenre.WORLD, "3/4"),
        DrumPattern(83, "Musette", DrumGenre.WORLD, "4/4"),
        DrumPattern(84, "March 1", DrumGenre.WORLD, "4/4"),
        DrumPattern(85, "March 2", DrumGenre.WORLD, "4/4"),
        DrumPattern(86, "March 3", DrumGenre.WORLD, "4/4"),
        DrumPattern(87, "New Age 1", DrumGenre.WORLD, "4/4"),
        DrumPattern(88, "New Age 2", DrumGenre.WORLD, "4/4"),
        DrumPattern(89, "World", DrumGenre.WORLD, "4/4"),

        // Metro (90..99) - 10 patterns
        DrumPattern(90, "Metro 1/4", DrumGenre.METRO, "1/4"),
        DrumPattern(91, "Metro 2/4", DrumGenre.METRO, "2/4"),
        DrumPattern(92, "Metro 3/4", DrumGenre.METRO, "3/4"),
        DrumPattern(93, "Metro 4/4", DrumGenre.METRO, "4/4"),
        DrumPattern(94, "Metro 5/4", DrumGenre.METRO, "5/4"),
        DrumPattern(95, "Metro 6/4", DrumGenre.METRO, "6/4"),
        DrumPattern(96, "Metro 7/4", DrumGenre.METRO, "7/4"),
        DrumPattern(97, "Metro 6/8", DrumGenre.METRO, "6/8"),
        DrumPattern(98, "Metro 7/8", DrumGenre.METRO, "7/8"),
        DrumPattern(99, "Metro 8/9", DrumGenre.METRO, "8/9")
    )

    fun getAllPatterns(): List<DrumPattern> = patterns

    fun getPatternsByGenre(genre: DrumGenre): List<DrumPattern> =
        if (genre == DrumGenre.ALL) patterns else patterns.filter { it.genre == genre }

    fun getPatternById(id: Int): DrumPattern? = patterns.getOrNull(id)

    fun searchPatterns(query: String, genre: DrumGenre): List<DrumPattern> {
        val base = getPatternsByGenre(genre)
        if (query.isBlank()) return base
        val clean = query.trim().lowercase()
        return base.filter { it.name.lowercase().contains(clean) || it.timeSignature.contains(clean) }
    }
}
