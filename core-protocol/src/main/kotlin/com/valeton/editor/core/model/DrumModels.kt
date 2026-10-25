package com.valeton.editor.core.model

enum class DrumGenre(val displayName: String) {
    ALL("TODOS"),
    ROCK("ROCK"),
    FUNK("FUNK"),
    BLUES("BLUES"),
    POP("POP"),
    JAZZ("JAZZ"),
    ELECTRONIC("ELETRÔNICO"),
    WORLD("WORLD / LATIN"),
    METRO("METRÔNOMO")
}

data class DrumPattern(
    val id: Int,
    val name: String,
    val genre: DrumGenre,
    val timeSignature: String = "4/4"
)
