package com.valeton.editor.core.model

sealed interface EffectParam {
    val name: String
    val index: Int
    val defaultValue: Float
}

data class KnobParam(
    override val name: String,
    override val index: Int,
    override val defaultValue: Float,
    val min: Float,
    val max: Float,
    val step: Float
) : EffectParam

data class OptionItem(val name: String, val id: Int)

data class SwitchParam(
    override val name: String,
    override val index: Int,
    override val defaultValue: Float,
    val options: List<OptionItem>,
    val boundParamIndex: Int? = null
) : EffectParam

data class ComboBoxParam(
    override val name: String,
    override val index: Int,
    override val defaultValue: Float,
    val options: List<OptionItem>
) : EffectParam

data class EffectDefinition(
    val effectId: Int,
    val name: String,
    val module: ModuleType,
    val params: List<EffectParam> = emptyList(),
    val category: String = "Other",
    val basedOn: String = "",
    val svgFile: String = ""
)

data class EffectSlot(
    val slotIndex: Int,
    val effectId: Int,
    val enabled: Boolean,
    val params: List<Float> = List(15) { 0f }
)

data class GP200Preset(
    val version: String = "1.0",
    val patchName: String,
    val author: String = "",
    val effects: List<EffectSlot>,
    val routingOrder: List<Int> = (0..10).toList(),
    val fxLoopSend: Int = 4,
    val fxLoopReturn: Int = 4,
    val patchVolume: Int = 50,
    val patchPan: Int = 0,
    val patchTempo: Int = 120,
    val checksum: Int = 0,
    val rawSource: ByteArray? = null,
    val drumSync: Boolean = false,
    val controlMappings: ControlMappings? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GP200Preset) return false
        return patchName == other.patchName &&
                author == other.author &&
                effects == other.effects &&
                routingOrder == other.routingOrder &&
                fxLoopSend == other.fxLoopSend &&
                fxLoopReturn == other.fxLoopReturn &&
                patchVolume == other.patchVolume &&
                patchPan == other.patchPan &&
                patchTempo == other.patchTempo &&
                drumSync == other.drumSync &&
                controlMappings == other.controlMappings &&
                checksum == other.checksum &&
                ((rawSource == null && other.rawSource == null) || (rawSource != null && other.rawSource != null && rawSource.contentEquals(other.rawSource)))
    }

    companion object {
        /**
         * FX loop positions the app lets the user pick: the number of blocks before the marker.
         * The live dump accepts 1..11 and the .prst decoder 1..10; editing stays inside both until
         * the pedal confirms the real limits.
         */
        val FX_LOOP_POSITIONS = 1..10
    }

    override fun hashCode(): Int {
        var result = patchName.hashCode()
        result = 31 * result + author.hashCode()
        result = 31 * result + effects.hashCode()
        result = 31 * result + routingOrder.hashCode()
        result = 31 * result + patchVolume.hashCode()
        result = 31 * result + patchPan.hashCode()
        result = 31 * result + patchTempo.hashCode()
        result = 31 * result + drumSync.hashCode()
        result = 31 * result + (controlMappings?.hashCode() ?: 0)
        result = 31 * result + checksum
        return result
    }
}
