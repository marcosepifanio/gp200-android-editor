package com.valeton.editor.core.model

enum class ModuleType(
    val code: String,
    val displayName: String,
    val colorHex: String,
    val slotIndex: Int
) {
    PRE("PRE", "Pre-Effects", "#D4A24E", 0),
    WAH("WAH", "Wah", "#C876D4", 1),
    DST("DST", "Distortion / Overdrive", "#D45050", 2),
    AMP("AMP", "Amplifier", "#E08040", 3),
    NR("NR", "Noise Reduction", "#7A8A8A", 4),
    CAB("CAB", "Cabinet IR", "#50B878", 5),
    EQ("EQ", "Equalizer", "#50B8D4", 6),
    MOD("MOD", "Modulation", "#5090E0", 7),
    DLY("DLY", "Delay", "#7070D8", 8),
    RVB("RVB", "Reverb", "#9060C8", 9),
    VOL("VOL", "Volume", "#909090", 10),
    UNKNOWN("UNK", "Unknown", "#606060", -1);

    companion object {
        fun fromSlotIndex(slotIndex: Int): ModuleType {
            return entries.firstOrNull { it.slotIndex == slotIndex } ?: UNKNOWN
        }

        fun fromCode(code: String): ModuleType {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
