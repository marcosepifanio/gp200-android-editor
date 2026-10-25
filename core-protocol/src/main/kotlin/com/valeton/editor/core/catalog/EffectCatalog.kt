package com.valeton.editor.core.catalog

import com.valeton.editor.core.model.*

object EffectCatalog {

    private val effectsMap: Map<Int, EffectDefinition> = mapOf(
        0 to EffectDefinition(0, "COMP", ModuleType.PRE, listOf(
            KnobParam("Sustain", 0, 20f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f)
        ), category = "Compressor", basedOn = "Ross™ Compressor", svgFile = "comp.svg"),
        1 to EffectDefinition(1, "COMP4", ModuleType.PRE, listOf(
            KnobParam("Sustain", 0, 20f, 0f, 100f, 1f),
            KnobParam("Attack", 1, 60f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Clipping", 3, 10f, 0f, 100f, 1f)
        ), category = "Compressor", basedOn = "Keeley® Compressor Plus (4-knob)", svgFile = "comp4.svg"),
        3 to EffectDefinition(3, "S-Comp", ModuleType.PRE, listOf(
            KnobParam("Threshold", 0, 20f, 0f, 100f, 1f),
            KnobParam("Ratio", 1, 30f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Attack", 3, 50f, 0f, 100f, 1f),
            KnobParam("Release", 4, 50f, 0f, 100f, 1f),
            KnobParam("Tone", 5, 50f, 0f, 100f, 1f),
            KnobParam("Blend", 6, 50f, 0f, 100f, 1f)
        ), category = "Compressor", basedOn = "Valeton original: fully adjustable studio-style compressor", svgFile = "s-comp.svg"),
        10 to EffectDefinition(10, "AC Boost", ModuleType.PRE, listOf(
            KnobParam("Gain", 0, 29f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 3, 50f, 0f, 100f, 1f)
        ), category = "Boost", basedOn = "Xotic® AC Booster", svgFile = "ac-boost.svg"),
        11 to EffectDefinition(11, "B-Boost", ModuleType.PRE, listOf(
            KnobParam("Gain", 0, 20f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 3, 50f, 0f, 100f, 1f)
        ), category = "Boost", basedOn = "Xotic® BB Preamp", svgFile = "b-boost.svg"),
        12 to EffectDefinition(12, "P-Boost", ModuleType.PRE, listOf(
            KnobParam("Gain", 0, 20f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 3, 50f, 0f, 100f, 1f)
        ), category = "Boost", basedOn = "Xotic® RC Booster", svgFile = "p-boost.svg"),
        14 to EffectDefinition(14, "14 Boost", ModuleType.PRE, listOf(
            KnobParam("Gain", 0, 20f, 0f, 100f, 1f)
        ), category = "Boost", basedOn = "Fortin® Grind boost", svgFile = "14-boost.svg"),
        20 to EffectDefinition(20, "Micro Boost", ModuleType.PRE, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f)
        ), category = "Boost", basedOn = "MXR® M133 Micro Amp", svgFile = "micro-boost.svg"),
        25 to EffectDefinition(25, "FAT BB", ModuleType.PRE, listOf(
            KnobParam("Bass", 0, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 20f, 0f, 100f, 1f),
            SwitchParam("Low Cut", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Boost", basedOn = "Fulltone® Fat Boost", svgFile = "fat-bb.svg"),
        26 to EffectDefinition(26, "Boost", ModuleType.PRE, listOf(
            KnobParam("Gain", 0, 20f, 0f, 100f, 1f),
            SwitchParam("+3dB", 1, 1f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1))),
            SwitchParam("Bright", 2, 1f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Boost", basedOn = "Xotic® EP Booster", svgFile = "boost.svg"),
        27 to EffectDefinition(27, "Gate 1", ModuleType.NR, listOf(
            KnobParam("Threshold", 0, 20f, 0f, 100f, 1f)
        ), category = "Gate", basedOn = "ISP® Decimator™", svgFile = "gate-1.svg"),
        29 to EffectDefinition(29, "Gate 2", ModuleType.NR, listOf(
            KnobParam("Threshold", 0, 20f, 0f, 100f, 1f),
            KnobParam("Attack", 1, 25f, 0f, 100f, 1f),
            KnobParam("Release", 2, 60f, 0f, 100f, 1f)
        ), category = "Gate", basedOn = "Valeton original: gate with attack/release control", svgFile = "gate-2.svg"),
        33 to EffectDefinition(33, "Gate 3", ModuleType.NR, listOf(
            KnobParam("Threshold", 0, 20f, 0f, 100f, 1f),
            KnobParam("Ratio", 1, 20f, 0f, 100f, 1f),
            KnobParam("Attack", 2, 20f, 1f, 500f, 1f),
            KnobParam("Release", 3, 200f, 10f, 10000f, 1f),
            KnobParam("Hold", 4, 500f, 0f, 1000f, 1f),
            KnobParam("Hysteresis", 5, -3f, -10f, 0f, 1f)
        ), category = "Gate", basedOn = "Valeton original: inverse expander dynamics gate", svgFile = "gate-3.svg"),
        16777216 to EffectDefinition(16777216, "AC Refiner", ModuleType.PRE, listOf(
            KnobParam("Shape", 0, 50f, 0f, 100f, 1f)
        ), category = "Acoustic", basedOn = "Valeton original: natural acoustic enhancer", svgFile = "ac-refiner.svg"),
        16777217 to EffectDefinition(16777217, "AC Sim", ModuleType.PRE, listOf(
            KnobParam("Body", 0, 50f, 0f, 100f, 1f),
            KnobParam("Top", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            ComboBoxParam("Mode", 3, 2f, listOf(OptionItem("Standard", 0), OptionItem("Jumbo", 1), OptionItem("Enhanced", 2), OptionItem("Piezo", 3)))
        ), category = "Acoustic", basedOn = "Boss® AC-2 Acoustic Simulator", svgFile = "ac-sim.svg"),
        16777231 to EffectDefinition(16777231, "T-Wah", ModuleType.PRE, listOf(
            KnobParam("Sense", 0, 50f, 0f, 100f, 1f),
            KnobParam("Range", 1, 50f, 0f, 100f, 1f),
            KnobParam("Q", 2, 50f, 0f, 100f, 1f),
            KnobParam("Mix", 3, 50f, 0f, 100f, 1f),
            ComboBoxParam("Mode", 4, 0f, listOf(OptionItem("Guitar", 0), OptionItem("Bass", 1)))
        ), category = "Filter", basedOn = "Valeton original: touch (envelope) wah", svgFile = "t-wah.svg"),
        16777237 to EffectDefinition(16777237, "A-WAH", ModuleType.PRE, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Low", 3, 25f, 0f, 100f, 1f),
            KnobParam("Q", 4, 70f, 0f, 100f, 1f),
            KnobParam("High", 5, 60f, 0f, 100f, 1f),
            SwitchParam("Sync", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Filter", basedOn = "Valeton original: variable auto wah", svgFile = "a-wah.svg"),
        16777241 to EffectDefinition(16777241, "Step Filter", ModuleType.PRE, listOf(
            KnobParam("Step 1", 0, 10f, 0f, 100f, 1f),
            KnobParam("Step 2", 1, 30f, 0f, 100f, 1f),
            KnobParam("Step 3", 2, 55f, 0f, 100f, 1f),
            KnobParam("Step 4", 3, 85f, 0f, 100f, 1f),
            KnobParam("Rate", 4, 10f, 0f, 100f, 1f),
            SwitchParam("Sync", 5, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 4)
        ), category = "Filter", basedOn = "Valeton original: 4-step synth filter sequencer", svgFile = "step-filter.svg"),
        16777249 to EffectDefinition(16777249, "OCTA", ModuleType.PRE, listOf(
            KnobParam("Low Oct", 0, 50f, 0f, 100f, 1f),
            KnobParam("High Oct", 1, 50f, 0f, 100f, 1f),
            KnobParam("Dry", 2, 50f, 0f, 100f, 1f)
        ), category = "Pitch", basedOn = "Valeton original: polyphonic octaver", svgFile = "octa.svg"),
        16777251 to EffectDefinition(16777251, "Pitch", ModuleType.PRE, listOf(
            KnobParam("Hi Pitch", 0, 12f, 0f, 24f, 1f),
            KnobParam("Low Pitch", 1, -12f, -24f, 0f, 1f),
            KnobParam("Dry", 2, 50f, 0f, 100f, 1f),
            KnobParam("Hi Vol", 4, 50f, 0f, 100f, 1f),
            KnobParam("Low Vol", 5, 50f, 0f, 100f, 1f)
        ), category = "Pitch", basedOn = "Valeton original: polyphonic pitch shifter (dry CTRL)", svgFile = "pitch.svg"),
        16777252 to EffectDefinition(16777252, "P-Bend", ModuleType.PRE, listOf(
            KnobParam("Hi Pitch", 0, 12f, 0f, 12f, 1f),
            KnobParam("Low Pitch", 1, 0f, -12f, 0f, 1f),
            KnobParam("Wet", 2, 50f, 0f, 100f, 1f),
            KnobParam("Dry", 3, 50f, 0f, 100f, 1f),
            KnobParam("Range", 4, 50f, 0f, 100f, 1f)
        ), category = "Pitch", basedOn = "Valeton original: polyphonic bend with wet/dry CTRL", svgFile = "p-bend.svg"),
        16777257 to EffectDefinition(16777257, "Detune", ModuleType.MOD, listOf(
            KnobParam("Detune", 0, -25f, -50f, 50f, 1f),
            KnobParam("Wet", 1, 50f, 0f, 100f, 1f),
            KnobParam("Dry", 2, 50f, 0f, 100f, 1f)
        ), category = "Pitch", basedOn = "Valeton original: micro pitch-shift doubler", svgFile = "detune.svg"),
        16777262 to EffectDefinition(16777262, "Bit Smash", ModuleType.MOD, listOf(
            KnobParam("Mix", 0, 50f, 0f, 100f, 1f),
            KnobParam("Krush", 1, 20f, 0f, 100f, 1f),
            KnobParam("Bit", 2, 20f, 0f, 100f, 1f),
            KnobParam("Hi Cut", 3, 50f, 0f, 100f, 1f),
            KnobParam("Lo Cut", 4, 50f, 0f, 100f, 1f)
        ), category = "Special", basedOn = "Valeton original: bitcrusher / sample reducer", svgFile = "bit-smash.svg"),
        16777263 to EffectDefinition(16777263, "Ring Mod", ModuleType.PRE, listOf(
            KnobParam("Mix", 0, 50f, 0f, 100f, 1f),
            KnobParam("Freq", 1, 50f, 0f, 100f, 1f),
            KnobParam("Fine", 2, 0f, -50f, 50f, 1f),
            KnobParam("Tone", 3, 50f, 0f, 100f, 1f)
        ), category = "Special", basedOn = "Valeton original: ring modulator (bells & chimes)", svgFile = "ring-mod.svg"),
        16777267 to EffectDefinition(16777267, "Saturate", ModuleType.PRE, listOf(
            KnobParam("Saturation", 0, 50f, 0f, 100f, 1f),
            KnobParam("Mix", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("High Cut", 3, 50f, 0f, 100f, 1f)
        ), category = "Special", basedOn = "Valeton original: vintage tape saturation", svgFile = "saturate.svg"),
        16777269 to EffectDefinition(16777269, "Guitar EQ 1", ModuleType.EQ, listOf(
            KnobParam("125Hz", 0, 0f, -50f, 50f, 1f),
            KnobParam("400Hz", 1, 0f, -50f, 50f, 1f),
            KnobParam("800Hz", 2, 0f, -50f, 50f, 1f),
            KnobParam("1.6kHz", 3, 0f, -50f, 50f, 1f),
            KnobParam("4kHz", 4, 0f, -50f, 50f, 1f),
            KnobParam("Volume", 5, 50f, 0f, 100f, 1f)
        ), category = "EQ", basedOn = "5-band guitar EQ (125 Hz – 4 kHz)", svgFile = "guitar-eq-1.svg"),
        16777270 to EffectDefinition(16777270, "Guitar EQ 2", ModuleType.EQ, listOf(
            KnobParam("100Hz", 0, 0f, -50f, 50f, 1f),
            KnobParam("500Hz", 1, 0f, -50f, 50f, 1f),
            KnobParam("1kHz", 2, 0f, -50f, 50f, 1f),
            KnobParam("3kHz", 3, 0f, -50f, 50f, 1f),
            KnobParam("6kHz", 4, 0f, -50f, 50f, 1f),
            KnobParam("Volume", 5, 50f, 0f, 100f, 1f)
        ), category = "EQ", basedOn = "5-band guitar EQ (100 Hz – 6 kHz)", svgFile = "guitar-eq-2.svg"),
        16777273 to EffectDefinition(16777273, "Bass EQ 1", ModuleType.EQ, listOf(
            KnobParam("33Hz", 0, 0f, -50f, 50f, 1f),
            KnobParam("150Hz", 1, 0f, -50f, 50f, 1f),
            KnobParam("600Hz", 2, 0f, -50f, 50f, 1f),
            KnobParam("2kHz", 3, 0f, -50f, 50f, 1f),
            KnobParam("8kHz", 4, 0f, -50f, 50f, 1f),
            KnobParam("Volume", 5, 50f, 0f, 100f, 1f)
        ), category = "EQ", basedOn = "5-band bass EQ", svgFile = "bass-eq-1.svg"),
        16777274 to EffectDefinition(16777274, "Bass EQ 2", ModuleType.EQ, listOf(
            KnobParam("50Hz", 0, 0f, -50f, 50f, 1f),
            KnobParam("120Hz", 1, 0f, -50f, 50f, 1f),
            KnobParam("400Hz", 2, 0f, -50f, 50f, 1f),
            KnobParam("800Hz", 3, 0f, -50f, 50f, 1f),
            KnobParam("4.5kHz", 4, 0f, -50f, 50f, 1f),
            KnobParam("Volume", 5, 50f, 0f, 100f, 1f)
        ), category = "EQ", basedOn = "5-band bass EQ", svgFile = "bass-eq-2.svg"),
        16777276 to EffectDefinition(16777276, "Mess EQ", ModuleType.EQ, listOf(
            KnobParam("80Hz", 0, 0f, -50f, 50f, 1f),
            KnobParam("240Hz", 1, 0f, -50f, 50f, 1f),
            KnobParam("750Hz", 2, 0f, -50f, 50f, 1f),
            KnobParam("2.2kHz", 3, 0f, -50f, 50f, 1f),
            KnobParam("6.6kHz", 4, 0f, -50f, 50f, 1f)
        ), category = "EQ", basedOn = "Mesa/Boogie® amp-style 5-band graphic EQ", svgFile = "mess-eq.svg"),
        16777283 to EffectDefinition(16777283, "Hyper EQ", ModuleType.EQ, listOf(
            KnobParam("31Hz", 0, 0f, -12f, 12f, 1f),
            KnobParam("63Hz", 1, 0f, -12f, 12f, 1f),
            KnobParam("125Hz", 2, 0f, -12f, 12f, 1f),
            KnobParam("250Hz", 3, 0f, -12f, 12f, 1f),
            KnobParam("500Hz", 4, 0f, -12f, 12f, 1f),
            KnobParam("1kHz", 5, 0f, -12f, 12f, 1f),
            KnobParam("2kHz", 6, 0f, -12f, 12f, 1f),
            KnobParam("4kHz", 7, 0f, -12f, 12f, 1f),
            KnobParam("8kHz", 8, 0f, -12f, 12f, 1f),
            KnobParam("16kHz", 9, 0f, -12f, 12f, 1f),
            KnobParam("Volume", 10, 50f, 0f, 100f, 1f)
        ), category = "EQ", basedOn = "10-band graphic EQ for any instrument", svgFile = "hyper-eq.svg"),
        16777289 to EffectDefinition(16777289, "Hammy", ModuleType.PRE, listOf(
            ComboBoxParam("Range", 0, 2f, listOf(OptionItem("-2 Oct", 0), OptionItem("-1 Oct", 1), OptionItem("+1 Oct", 2), OptionItem("+2 Oct", 3), OptionItem("+/-1Oct", 4), OptionItem("+/-2Oct", 5))),
            ComboBoxParam("Harmony", 1, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1))),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Position", 3, 0f, 0f, 100f, 1f)
        ), category = "Pitch", basedOn = "DigiTech® Whammy™ pitch treadle", svgFile = "hammy.svg"),
        16777293 to EffectDefinition(16777293, "Harmonizer 1", ModuleType.PRE, listOf(
            KnobParam("Mix", 0, 50f, 0f, 100f, 1f),
            ComboBoxParam("Key", 1, 0f, listOf(OptionItem("C", 0), OptionItem("C#", 1), OptionItem("D", 2), OptionItem("D#", 3), OptionItem("E", 4), OptionItem("F", 5), OptionItem("F#", 6), OptionItem("G", 7), OptionItem("G#", 8), OptionItem("A", 9), OptionItem("A#", 10), OptionItem("B", 11))),
            ComboBoxParam("Mode", 2, 0f, listOf(OptionItem("Major", 0), OptionItem("Minor", 1), OptionItem("H. Minor", 2), OptionItem("Dorian", 3), OptionItem("Phrygian", 4), OptionItem("Lydian", 5), OptionItem("Mixolydian", 6), OptionItem("Locrian", 7))),
            ComboBoxParam("Interval", 3, 8f, listOf(OptionItem("-Oct", 0), OptionItem("-7th", 1), OptionItem("-6th", 2), OptionItem("-5th", 3), OptionItem("-4th", 4), OptionItem("-3rd", 5), OptionItem("-2nd", 6), OptionItem("+2nd", 7), OptionItem("+3rd", 8), OptionItem("+4th", 9), OptionItem("+5th", 10), OptionItem("+6th", 11), OptionItem("+7th", 12), OptionItem("+Oct", 13))),
            ComboBoxParam("Smooth Mode", 5, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Pitch", basedOn = "Valeton original: single-voice auto harmonizer", svgFile = "harmonizer-1.svg"),
        16777294 to EffectDefinition(16777294, "Harmonizer 2", ModuleType.PRE, listOf(
            KnobParam("Mix", 0, 50f, 0f, 100f, 1f),
            ComboBoxParam("Key", 1, 0f, listOf(OptionItem("C", 0), OptionItem("C#", 1), OptionItem("D", 2), OptionItem("D#", 3), OptionItem("E", 4), OptionItem("F", 5), OptionItem("F#", 6), OptionItem("G", 7), OptionItem("G#", 8), OptionItem("A", 9), OptionItem("A#", 10), OptionItem("B", 11))),
            ComboBoxParam("Mode", 2, 0f, listOf(OptionItem("Major", 0), OptionItem("Minor", 1), OptionItem("H. Minor", 2), OptionItem("Dorian", 3), OptionItem("Phrygian", 4), OptionItem("Lydian", 5), OptionItem("Mixolydian", 6), OptionItem("Locrian", 7))),
            ComboBoxParam("Interval 1", 3, 8f, listOf(OptionItem("-Oct", 0), OptionItem("-7th", 1), OptionItem("-6th", 2), OptionItem("-5th", 3), OptionItem("-4th", 4), OptionItem("-3rd", 5), OptionItem("-2nd", 6), OptionItem("+2nd", 7), OptionItem("+3rd", 8), OptionItem("+4th", 9), OptionItem("+5th", 10), OptionItem("+6th", 11), OptionItem("+7th", 12), OptionItem("+Oct", 13))),
            ComboBoxParam("Interval 2", 4, 10f, listOf(OptionItem("-Oct", 0), OptionItem("-7th", 1), OptionItem("-6th", 2), OptionItem("-5th", 3), OptionItem("-4th", 4), OptionItem("-3rd", 5), OptionItem("-2nd", 6), OptionItem("+2nd", 7), OptionItem("+3rd", 8), OptionItem("+4th", 9), OptionItem("+5th", 10), OptionItem("+6th", 11), OptionItem("+7th", 12), OptionItem("+Oct", 13))),
            ComboBoxParam("Smooth Mode", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Pitch", basedOn = "Valeton original: dual-voice auto harmonizer", svgFile = "harmonizer-2.svg"),
        50331648 to EffectDefinition(50331648, "Green OD", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 70f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Overdrive", basedOn = "Ibanez® TS-808 Tube Screamer", svgFile = "green-od.svg"),
        50331649 to EffectDefinition(50331649, "OD 9", ModuleType.PRE, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 70f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Overdrive", basedOn = "Ibanez® TS9 Tube Screamer", svgFile = "od-9.svg"),
        50331650 to EffectDefinition(50331650, "Yellow OD", ModuleType.PRE, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f)
        ), category = "Overdrive", basedOn = "Boss® OD-1 OverDrive (2-knob)", svgFile = "yellow-od.svg"),
        50331652 to EffectDefinition(50331652, "Swarm", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Overdrive", basedOn = "Providence® SOV-2 Stampede OD", svgFile = "swarm.svg"),
        50331654 to EffectDefinition(50331654, "Super OD", ModuleType.PRE, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Overdrive", basedOn = "Boss® SD-1 Super OverDrive", svgFile = "super-od.svg"),
        50331656 to EffectDefinition(50331656, "Scream OD", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 60f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Fat", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1))),
            SwitchParam("Air", 4, 1f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Overdrive", basedOn = "Valeton's take on the Tube Screamer® circuit, extra switches", svgFile = "scream-od.svg"),
        50331657 to EffectDefinition(50331657, "Blues OD", ModuleType.PRE, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 60f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Overdrive", basedOn = "Boss® BD-2 Blues Driver", svgFile = "blues-od.svg"),
        50331658 to EffectDefinition(50331658, "Force", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 60f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            ComboBoxParam("Mode", 3, 1f, listOf(OptionItem("LP", 0), OptionItem("HP", 1)))
        ), category = "Overdrive", basedOn = "Fulltone® OCD", svgFile = "force.svg"),
        50331659 to EffectDefinition(50331659, "Tube", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 3, 50f, 0f, 100f, 1f)
        ), category = "Overdrive", basedOn = "BK Butler® Tube Driver", svgFile = "tube--dst.svg"),
        50331662 to EffectDefinition(50331662, "Blues Master", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 60f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Overdrive", basedOn = "Marshall® BluesBreaker", svgFile = "blues-master.svg"),
        50331663 to EffectDefinition(50331663, "Master OD", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Overdrive", basedOn = "Marshall® JCM800-in-a-box (clean to driven)", svgFile = "master-od.svg"),
        50331664 to EffectDefinition(50331664, "TaiChi OD", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 30f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Voice", 3, 60f, 0f, 100f, 1f)
        ), category = "Overdrive", basedOn = "Hermida® Zendrive", svgFile = "taichi-od.svg"),
        50331668 to EffectDefinition(50331668, "Penesas", ModuleType.PRE, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Overdrive", basedOn = "Klon® Centaur", svgFile = "penesas.svg"),
        50331676 to EffectDefinition(50331676, "Precise OD", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Attack", 3, 2f, 0f, 5f, 1f),
            KnobParam("Gate", 4, 10f, 0f, 100f, 1f)
        ), category = "Overdrive", basedOn = "Horizon Devices® Precision Drive", svgFile = "precise-od.svg"),
        50331678 to EffectDefinition(50331678, "Timmy OD", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 3, 50f, 0f, 100f, 1f),
            ComboBoxParam("Mode", 4, 1f, listOf(OptionItem("I", 0), OptionItem("II", 1), OptionItem("III", 2)))
        ), category = "Overdrive", basedOn = "Paul Cochrane® Timmy V2/V3", svgFile = "timmy-od.svg"),
        50331682 to EffectDefinition(50331682, "Lazaro", ModuleType.DST, listOf(
            KnobParam("Sustain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Fuzz", basedOn = "Electro-Harmonix® Big Muff Pi", svgFile = "lazaro.svg"),
        50331684 to EffectDefinition(50331684, "Red Haze", ModuleType.DST, listOf(
            KnobParam("Fuzz", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f)
        ), category = "Fuzz", basedOn = "Dallas-Arbiter® Fuzz Face", svgFile = "red-haze.svg"),
        50331686 to EffectDefinition(50331686, "Sora Fuzz", ModuleType.DST, listOf(
            KnobParam("Fuzz", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f)
        ), category = "Fuzz", basedOn = "Sola Sound® Tone Bender", svgFile = "sora-fuzz.svg"),
        50331689 to EffectDefinition(50331689, "Plustortion", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f)
        ), category = "Distortion", basedOn = "MXR® M104 Distortion+", svgFile = "plustortion.svg"),
        50331690 to EffectDefinition(50331690, "SM Dist", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Distortion", basedOn = "Boss® DS-1 Distortion", svgFile = "sm-dist.svg"),
        50331691 to EffectDefinition(50331691, "Darktale", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Filter", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Distortion", basedOn = "ProCo™ RAT (early LM308 op-amp)", svgFile = "darktale.svg"),
        50331693 to EffectDefinition(50331693, "Chief", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Distortion", basedOn = "Marshall® Guv'nor", svgFile = "chief.svg"),
        50331694 to EffectDefinition(50331694, "Master Dist", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Contour", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Distortion", basedOn = "Marshall® Shredmaster", svgFile = "master-dist.svg"),
        50331696 to EffectDefinition(50331696, "La Charger", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Distortion", basedOn = "MI Audio® Crunch Box", svgFile = "la-charger.svg"),
        50331698 to EffectDefinition(50331698, "Revolt", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            ComboBoxParam("Mode", 3, 0f, listOf(OptionItem("I", 0), OptionItem("II", 1), OptionItem("III", 2)))
        ), category = "Distortion", basedOn = "Suhr® Riot", svgFile = "revolt.svg"),
        50331711 to EffectDefinition(50331711, "Flex OD", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            ComboBoxParam("Mode", 3, 0f, listOf(OptionItem("Normal", 0), OptionItem("Scoop", 1), OptionItem("Edge", 2))),
            KnobParam("Blend", 4, 50f, 0f, 100f, 1f)
        ), category = "Bass Drive", basedOn = "Valeton original: drive for guitar and bass", svgFile = "flex-od.svg"),
        50331712 to EffectDefinition(50331712, "Bass OD", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Blend", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Bass Drive", basedOn = "Valeton original: bass OD / distortion / boost", svgFile = "bass-od.svg"),
        50331716 to EffectDefinition(50331716, "Black Bass", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Blend", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Low", 3, 50f, 0f, 100f, 1f),
            KnobParam("Lo-mid", 4, 50f, 0f, 100f, 1f),
            KnobParam("Hi-mid", 6, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 8, 50f, 0f, 100f, 1f),
            ComboBoxParam("Attack", 9, 2f, listOf(OptionItem("Cut", 0), OptionItem("Boost", 1), OptionItem("Flat", 2)))
        ), category = "Bass Preamp", basedOn = "Darkglass® Microtubes B7K", svgFile = "black-bass.svg"),
        50331728 to EffectDefinition(50331728, "Empire OD", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            ComboBoxParam("Mode", 3, 0f, listOf(OptionItem("Clean", 0), OptionItem("OD", 1), OptionItem("Dist", 2)))
        ), category = "Overdrive", basedOn = "Analog Man® Prince of Tone", svgFile = "empire-od.svg"),
        50331729 to EffectDefinition(50331729, "Bass Hammer", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Master", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Mid Freq", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Drive", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Bass Preamp", basedOn = "Bass preamp (likely Aguilar® Tone Hammer)", svgFile = "bass-hammer.svg"),
        50331730 to EffectDefinition(50331730, "Flagman Dist", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 3, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 4, 50f, 0f, 100f, 1f),
            KnobParam("Tight", 5, 50f, 0f, 100f, 1f)
        ), category = "Distortion", basedOn = "Friedman® BE-OD", svgFile = "flagman-dist.svg"),
        67108864 to EffectDefinition(67108864, "A-Chorus", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Tone", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Chorus", basedOn = "Boss® CE-3 Chorus", svgFile = "a-chorus.svg"),
        67108865 to EffectDefinition(67108865, "G-Chorus", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Chorus", basedOn = "Roland® CE-1 Chorus Ensemble", svgFile = "g-chorus.svg"),
        67108866 to EffectDefinition(67108866, "C-Chorus", ModuleType.MOD, listOf(
            ComboBoxParam("Mode", 0, 0f, listOf(OptionItem("1", 0), OptionItem("2", 1), OptionItem("3", 2), OptionItem("4", 3)))
        ), category = "Chorus", basedOn = "Boss® DC-2 Dimension C", svgFile = "c-chorus.svg"),
        67108872 to EffectDefinition(67108872, "B-Chorus", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Chorus", basedOn = "Chorus voiced for bass (Boss® CEB-3 style)", svgFile = "b-chorus.svg"),
        67108879 to EffectDefinition(67108879, "M-Chorus", ModuleType.MOD, listOf(
            KnobParam("Mix", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Filter", 2, 50f, 0f, 100f, 1f),
            KnobParam("Depth L", 3, 50f, 0f, 100f, 1f),
            KnobParam("Depth C", 4, 50f, 0f, 100f, 1f),
            KnobParam("Depth R", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Chorus", basedOn = "Boss® CH-1 Super Chorus", svgFile = "m-chorus.svg"),
        67108881 to EffectDefinition(67108881, "Jet", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Pre Delay", 2, 50f, 0f, 100f, 1f),
            KnobParam("Feedback", 3, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Flanger", basedOn = "Boss® BF-2 Flanger", svgFile = "jet.svg"),
        67108882 to EffectDefinition(67108882, "B-Jet", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Pre Delay", 2, 50f, 0f, 100f, 1f),
            KnobParam("Feedback", 3, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Flanger", basedOn = "Boss® BF-2 style flanger for bass", svgFile = "b-jet.svg"),
        67108883 to EffectDefinition(67108883, "N-Jet", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Pre Delay", 2, 50f, 0f, 100f, 1f),
            KnobParam("Feedback", 3, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Flanger", basedOn = "Negative-feedback flanger: \"underwater\" voicing", svgFile = "n-jet.svg"),
        67108884 to EffectDefinition(67108884, "Trem Jet", ModuleType.MOD, listOf(
            KnobParam("Flg Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Flg Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Feedback", 2, 50f, 0f, 100f, 1f),
            KnobParam("Trm Depth", 3, 50f, 0f, 100f, 1f),
            KnobParam("Trm Rate", 4, 0.5f, 0.1f, 10f, 0.1f),
            SwitchParam("Flg Sync", 5, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trm Sync", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 4)
        ), category = "Flanger", basedOn = "Valeton original: flanger + tremolo combo", svgFile = "trem-jet.svg"),
        67108885 to EffectDefinition(67108885, "V-Roto", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            SwitchParam("Sync", 2, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Vibrato", basedOn = "Boss® VB-2 Vibrato (BBD)", svgFile = "v-roto.svg"),
        67108886 to EffectDefinition(67108886, "G-Roto", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Vibrato", basedOn = "Roland® CE-1 (vibrato mode)", svgFile = "g-roto.svg"),
        67108887 to EffectDefinition(67108887, "Vibrato", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Vibrato", basedOn = "Boss® VB-2 style, wider range", svgFile = "vibrato.svg"),
        67108888 to EffectDefinition(67108888, "Vibrato T", ModuleType.MOD, listOf(
            KnobParam("Sens", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Output", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Vibrato", basedOn = "Valeton original: touch-sensitive vibrato", svgFile = "vibrato-t.svg"),
        67108889 to EffectDefinition(67108889, "O-Phase", ModuleType.MOD, listOf(
            KnobParam("Rate", 0, 0.5f, 0.1f, 10f, 0.1f),
            SwitchParam("Sync", 1, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 0)
        ), category = "Phaser", basedOn = "MXR® M101 Phase 90", svgFile = "o-phase.svg"),
        67108890 to EffectDefinition(67108890, "G-Phase", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            SwitchParam("Sync", 2, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Phaser", basedOn = "Boss® PH-1 Phaser", svgFile = "g-phase.svg"),
        67108891 to EffectDefinition(67108891, "S-Phase", ModuleType.MOD, listOf(
            ComboBoxParam("Color", 0, 0f, listOf(OptionItem("Warm", 0), OptionItem("Sharp", 1))),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            SwitchParam("Sync", 2, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Phaser", basedOn = "Electro-Harmonix® Small Stone", svgFile = "s-phase.svg"),
        67108894 to EffectDefinition(67108894, "Pan Phase", ModuleType.MOD, listOf(
            KnobParam("Phs Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Phs Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Pan Depth", 2, 50f, 0f, 100f, 1f),
            KnobParam("Pan Rate", 3, 0.5f, 0.1f, 10f, 0.1f),
            SwitchParam("Phs Sync", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Pan Sync", 5, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 3)
        ), category = "Phaser", basedOn = "Valeton original: phaser + tremolo/pan blend", svgFile = "pan-phase.svg"),
        67108895 to EffectDefinition(67108895, "M-Vibe", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            SwitchParam("Sync", 2, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Phaser", basedOn = "Voodoo Lab® Micro Vibe", svgFile = "m-vibe.svg"),
        67108896 to EffectDefinition(67108896, "Vibe", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            ComboBoxParam("Mode", 3, 0f, listOf(OptionItem("Chorus", 0), OptionItem("Vibrato", 1))),
            SwitchParam("Sync", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Phaser", basedOn = "Shin-Ei® Uni-Vibe", svgFile = "vibe.svg"),
        67108897 to EffectDefinition(67108897, "O-Trem", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            SwitchParam("Sync", 2, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Tremolo", basedOn = "Demeter® TRM-1 Tremulator", svgFile = "o-trem.svg"),
        67108902 to EffectDefinition(67108902, "Sine Trem", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Tremolo", basedOn = "Valeton original: sine-wave tremolo", svgFile = "sine-trem.svg"),
        67108903 to EffectDefinition(67108903, "Triangle Trem", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1)
        ), category = "Tremolo", basedOn = "Valeton original: triangle-wave tremolo", svgFile = "triangle-trem.svg"),
        67108904 to EffectDefinition(67108904, "Bias Trem", ModuleType.MOD, listOf(
            KnobParam("Depth", 0, 50f, 0f, 100f, 1f),
            KnobParam("Rate", 1, 0.5f, 0.1f, 10f, 0.1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            KnobParam("Bias", 4, 50f, 0f, 100f, 1f)
        ), category = "Tremolo", basedOn = "Valeton original: tube-bias style tremolo", svgFile = "bias-trem.svg"),
        67108909 to EffectDefinition(67108909, "Auto Swell", ModuleType.PRE, listOf(
            KnobParam("Attack", 0, 1000f, 80f, 4000f, 1f),
            ComboBoxParam("Curve", 1, 0f, listOf(OptionItem("Line", 0), OptionItem("Exp", 1), OptionItem("Log", 2)))
        ), category = "Special", basedOn = "Valeton original: violin-style auto volume swell", svgFile = "auto-swell.svg"),
        67108911 to EffectDefinition(67108911, "Hold", ModuleType.PRE, listOf(
            KnobParam("Volume", 0, 50f, 0f, 100f, 1f),
            SwitchParam("Activate", 1, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Special", basedOn = "Valeton original: holds a note like a short loop", svgFile = "hold.svg"),
        67108912 to EffectDefinition(67108912, "Freeze", ModuleType.PRE, listOf(
            KnobParam("Volume", 0, 50f, 0f, 100f, 1f),
            KnobParam("Attack", 1, 30f, 0f, 100f, 1f),
            KnobParam("Release", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Activate", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Special", basedOn = "Valeton original: sustains the sound at the moment of activation", svgFile = "freeze.svg"),
        83886081 to EffectDefinition(83886081, "V-Wah", ModuleType.WAH, listOf(
            KnobParam("Range", 0, 50f, 0f, 100f, 1f),
            KnobParam("Q", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Position", 3, 50f, 0f, 100f, 1f)
        ), category = "Wah", basedOn = "VOX® V846", svgFile = "v-wah.svg"),
        83886086 to EffectDefinition(83886086, "S-Wah", ModuleType.WAH, listOf(
            KnobParam("Range", 0, 50f, 0f, 100f, 1f),
            KnobParam("Q", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Position", 3, 50f, 0f, 100f, 1f)
        ), category = "Wah", basedOn = "Valeton original: classic wah voicing", svgFile = "s-wah.svg"),
        83886087 to EffectDefinition(83886087, "B-Wah", ModuleType.WAH, listOf(
            KnobParam("Range", 0, 50f, 0f, 100f, 1f),
            KnobParam("Q", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Position", 3, 50f, 0f, 100f, 1f)
        ), category = "Wah", basedOn = "Valeton original: wah voiced for bass", svgFile = "b-wah.svg"),
        83886088 to EffectDefinition(83886088, "C-Wah", ModuleType.WAH, listOf(
            KnobParam("Range", 0, 50f, 0f, 100f, 1f),
            KnobParam("Q", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Position", 3, 50f, 0f, 100f, 1f)
        ), category = "Wah", basedOn = "Dunlop® Cry Baby® (classic 60's)", svgFile = "c-wah.svg"),
        83886090 to EffectDefinition(83886090, "P-Wah", ModuleType.WAH, listOf(
            KnobParam("Range", 0, 50f, 0f, 100f, 1f),
            KnobParam("Q", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Position", 3, 50f, 0f, 100f, 1f),
            SwitchParam("EQ", 4, 1f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Wah", basedOn = "Dunlop® JP95 John Petrucci signature wah", svgFile = "p-wah.svg"),
        100663299 to EffectDefinition(100663299, "Volume", ModuleType.VOL, listOf(
            KnobParam("Volume", 0, 100f, 0f, 100f, 1f)
        ), category = "Volume", basedOn = "Volume pedal block", svgFile = "volume.svg"),
        117440513 to EffectDefinition(117440513, "Tweedy", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Fender® Tweed Deluxe", svgFile = "tweedy.svg"),
        117440515 to EffectDefinition(117440515, "Bellman 59N", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Fender® '59 Bassman (normal channel)", svgFile = "bellman-59n.svg"),
        117440516 to EffectDefinition(117440516, "Dark Twin", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 35f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 40f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 60f, 0f, 100f, 1f),
            SwitchParam("Bright", 5, 1f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Clean Amp", basedOn = "Fender® '65 Twin Reverb", svgFile = "dark-twin--amp.svg"),
        117440517 to EffectDefinition(117440517, "Dark DLX", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Volume ", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 3, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Fender® Deluxe Reverb", svgFile = "dark-dlx.svg"),
        117440521 to EffectDefinition(117440521, "Dark Vibra", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 3, 50f, 0f, 100f, 1f),
            SwitchParam("Bright", 4, 1f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Clean Amp", basedOn = "Fender® Vibroverb 6G16 (1963)", svgFile = "dark-vibra.svg"),
        117440525 to EffectDefinition(117440525, "Silver Twin", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Fender® Silverface Twin Reverb", svgFile = "silver-twin.svg"),
        117440527 to EffectDefinition(117440527, "SUPDual CL", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Supro® Dual-Tone 1624T (CH1)", svgFile = "supdual-cl.svg"),
        117440528 to EffectDefinition(117440528, "Foxy 15TB", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Tone Cut", 1, 60f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "VOX® AC-100 (vintage)", svgFile = "foxy-15tb.svg"),
        117440529 to EffectDefinition(117440529, "Foxy 30N", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Tone Cut", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Bright", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Clean Amp", basedOn = "VOX® AC30HW (clean)", svgFile = "foxy-30n.svg"),
        117440532 to EffectDefinition(117440532, "J-120 CL", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 1, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 2, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 3, 50f, 0f, 100f, 1f),
            SwitchParam("Bright", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Clean Amp", basedOn = "Roland® JC-120 Jazz Chorus", svgFile = "j-120-cl.svg"),
        117440533 to EffectDefinition(117440533, "Match CL", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 35f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Matchless™ Chieftain 212 (clean)", svgFile = "match-cl.svg"),
        117440537 to EffectDefinition(117440537, "L-Star CL", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Mesa/Boogie® Lone Star (CH1)", svgFile = "l-star-cl.svg"),
        117440538 to EffectDefinition(117440538, "BogSV CL", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f),
            SwitchParam("Bright", 5, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Clean Amp", basedOn = "Bogner® Shiva 20th Anniversary (clean)", svgFile = "bogsv-cl.svg"),
        117440539 to EffectDefinition(117440539, "Z38 CL", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 35f, 0f, 100f, 1f),
            KnobParam("Tone Cut", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Dr. Z® Maz 38 Sr. (clean)", svgFile = "z38-cl.svg"),
        117440543 to EffectDefinition(117440543, "Knights CL", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 35f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Grindrod® Pendragon PG20C (clean, bright off)", svgFile = "knights-cl.svg"),
        117440546 to EffectDefinition(117440546, "Bad-KT CL", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 35f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Bad Cat® Hot Cat 30 (clean)", svgFile = "bad-kt-cl.svg"),
        117440547 to EffectDefinition(117440547, "Solo100 CL", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Soldano® SLO-100 (clean)", svgFile = "solo100-cl.svg"),
        117440548 to EffectDefinition(117440548, "Bellman 59B", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 35f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Fender® '59 Bassman (bright channel)", svgFile = "bellman-59b.svg"),
        117440551 to EffectDefinition(117440551, "Foxy 30TB", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Tone Cut", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f),
            ComboBoxParam("Char", 5, 0f, listOf(OptionItem("Cool", 0), OptionItem("Hot", 1)))
        ), category = "Drive Amp", basedOn = "VOX® AC30HW (top boost, driven)", svgFile = "foxy-30tb.svg"),
        117440552 to EffectDefinition(117440552, "SUPDual OD", ModuleType.AMP, listOf(
            KnobParam("Gain 1", 0, 50f, 0f, 100f, 1f),
            KnobParam("Tone 1", 1, 50f, 0f, 100f, 1f),
            KnobParam("Gain 2", 2, 50f, 0f, 100f, 1f),
            KnobParam("Tone 2", 3, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 4, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Supro® Dual-Tone 1624T (CH2)", svgFile = "supdual-od.svg"),
        117440554 to EffectDefinition(117440554, "UK 45", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 25f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 65f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 45f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 65f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Marshall® JTM45 (normal channel)", svgFile = "uk-45.svg"),
        117440555 to EffectDefinition(117440555, "UK 45+", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 45f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Marshall® JTM45 \"Plexi\"", svgFile = "uk-45-plus.svg"),
        117440556 to EffectDefinition(117440556, "UK 45JP", ModuleType.AMP, listOf(
            KnobParam("Gain 1", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f),
            KnobParam("Gain 2", 6, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Marshall® JTM45 (channels jumpered)", svgFile = "uk-45jp.svg"),
        117440557 to EffectDefinition(117440557, "UK 50", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Marshall® JMP50 (normal channel)", svgFile = "uk-50.svg"),
        117440558 to EffectDefinition(117440558, "UK 50+", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 40f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Marshall® JMP50 (1966)", svgFile = "uk-50-plus.svg"),
        117440559 to EffectDefinition(117440559, "UK 50JP", ModuleType.AMP, listOf(
            KnobParam("Gain 1", 0, 40f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f),
            KnobParam("Gain 2", 6, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Marshall® JMP50 (channels jumpered)", svgFile = "uk-50jp.svg"),
        117440560 to EffectDefinition(117440560, "UK SLP", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Marshall® 1959HW Super Lead Plexi (mid-60's)", svgFile = "uk-slp.svg"),
        117440565 to EffectDefinition(117440565, "UK 800", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Marshall® JCM800", svgFile = "uk-800.svg"),
        117440569 to EffectDefinition(117440569, "Mess2C+ 1", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Mesa/Boogie® Mark IIC+ (voicing 1)", svgFile = "mess2c-plus-1.svg"),
        117440570 to EffectDefinition(117440570, "Mess2C+ 2", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Mesa/Boogie® Mark IIC+ (voicing 2)", svgFile = "mess2c-plus-2.svg"),
        117440571 to EffectDefinition(117440571, "Mess2C+ 3", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Mesa/Boogie® Mark IIC+ (voicing 3)", svgFile = "mess2c-plus-3.svg"),
        117440573 to EffectDefinition(117440573, "BogSV OD", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Bogner® Shiva 20th Anniversary (overdrive)", svgFile = "bogsv-od.svg"),
        117440574 to EffectDefinition(117440574, "Juice30 OD", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Orange® AD30 (dirty channel)", svgFile = "juice30-od.svg"),
        117440576 to EffectDefinition(117440576, "Flagman 1", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Friedman® BE-100 (plexi channel)", svgFile = "flagman-1.svg"),
        117440577 to EffectDefinition(117440577, "Flagman 2", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Friedman® BE-100 Deluxe (plexi channel)", svgFile = "flagman-2.svg"),
        117440579 to EffectDefinition(117440579, "Bog BlueV", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Bogner® Ecstasy (blue channel, vintage)", svgFile = "bog-bluev.svg"),
        117440580 to EffectDefinition(117440580, "Bog BlueM", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Bogner® Ecstasy (blue channel, modern)", svgFile = "bog-bluem.svg"),
        117440583 to EffectDefinition(117440583, "Solo100 OD", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Soldano® SLO-100 (crunch)", svgFile = "solo100-od.svg"),
        117440584 to EffectDefinition(117440584, "Match OD", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Matchless™ Chieftain 212 (overdrive)", svgFile = "match-od.svg"),
        117440585 to EffectDefinition(117440585, "Z38 OD", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Tone Cut", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Dr. Z® Maz 38 Sr. (overdrive)", svgFile = "z38-od.svg"),
        117440586 to EffectDefinition(117440586, "L-Star OD", ModuleType.AMP, listOf(
            KnobParam("Input", 0, 50f, 0f, 100f, 1f),
            KnobParam("Gain", 1, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 2, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 3, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 4, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 5, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 6, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Mesa/Boogie® Lone Star (CH2)", svgFile = "l-star-od.svg"),
        117440587 to EffectDefinition(117440587, "Bad-KT OD", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Edge", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Bad Cat® Hot Cat 30 (overdrive)", svgFile = "bad-kt-od.svg"),
        117440590 to EffectDefinition(117440590, "UK 900", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Marshall® JCM900", svgFile = "uk-900.svg"),
        117440595 to EffectDefinition(117440595, "Juice R100", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Orange® Rockerverb 100 (dirty channel)", svgFile = "juice-r100.svg"),
        117440597 to EffectDefinition(117440597, "Mess4 LD", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Mesa/Boogie® Mark IV (lead 1)", svgFile = "mess4-ld.svg"),
        117440598 to EffectDefinition(117440598, "Mess4 LD 2", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Mesa/Boogie® Mark IV (lead 2)", svgFile = "mess4-ld-2.svg"),
        117440599 to EffectDefinition(117440599, "Mess4 LD 3", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Mesa/Boogie® Mark IV (lead 3)", svgFile = "mess4-ld-3.svg"),
        117440601 to EffectDefinition(117440601, "Solo100 LD", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Soldano® SLO-100 (overdrive)", svgFile = "solo100-ld.svg"),
        117440602 to EffectDefinition(117440602, "EV 51", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 6, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Peavey® 5150 (lead channel)", svgFile = "ev-51.svg"),
        117440605 to EffectDefinition(117440605, "Flagman+ 1", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Friedman® BE-100 (brown-eye channel)", svgFile = "flagman-plus-1.svg"),
        117440606 to EffectDefinition(117440606, "Flagman+ 2", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Friedman® BE-100 Deluxe (brown-eye channel)", svgFile = "flagman-plus-2.svg"),
        117440607 to EffectDefinition(117440607, "Eagle 120", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "ENGL® Savage 120", svgFile = "eagle-120.svg"),
        117440608 to EffectDefinition(117440608, "Eagle 120+", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "ENGL® Savage 120 (boosted)", svgFile = "eagle-120-plus.svg"),
        117440611 to EffectDefinition(117440611, "Power LD", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "ENGL® Powerball II E645/2 (CH4)", svgFile = "power-ld.svg"),
        117440613 to EffectDefinition(117440613, "Dizz VH", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Diezel® VH4 (CH1 clean)", svgFile = "dizz-vh.svg"),
        117440614 to EffectDefinition(117440614, "Dizz VH S", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Diezel® VH4 (CH2 crunch)", svgFile = "dizz-vh-s.svg"),
        117440616 to EffectDefinition(117440616, "Mess DualV", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Mesa/Boogie® Dual Rectifier (vintage mode)", svgFile = "mess-dualv.svg"),
        117440617 to EffectDefinition(117440617, "Mess DualM", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Mesa/Boogie® Dual Rectifier (modern mode)", svgFile = "mess-dualm.svg"),
        117440618 to EffectDefinition(117440618, "Dizz VH+", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Diezel® VH4 (CH3 heavy)", svgFile = "dizz-vh-plus.svg"),
        117440619 to EffectDefinition(117440619, "Dizz VH+ S", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Diezel® VH4 (CH4 ultra)", svgFile = "dizz-vh-plus-s.svg"),
        117440621 to EffectDefinition(117440621, "Bog RedV", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Bogner® Ecstasy (red channel, vintage)", svgFile = "bog-redv.svg"),
        117440622 to EffectDefinition(117440622, "Bog RedM", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Presence", 1, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 2, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 3, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 4, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 5, 50f, 0f, 100f, 1f)
        ), category = "Hi-Gain Amp", basedOn = "Bogner® Ecstasy (red channel, modern)", svgFile = "bog-redm.svg"),
        117440627 to EffectDefinition(117440627, "Classic Bass", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 1, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 2, 50f, 0f, 100f, 1f),
            ComboBoxParam("Midrange", 3, 1f, listOf(OptionItem("220Hz", 0), OptionItem("450Hz", 1), OptionItem("800Hz", 2), OptionItem("1.6kHz", 3), OptionItem("3kHz", 4))),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 5, 50f, 0f, 100f, 1f)
        ), category = "Bass Amp", basedOn = "Ampeg® SVT", svgFile = "classic-bass.svg"),
        117440629 to EffectDefinition(117440629, "Foxy Bass", ModuleType.AMP, listOf(
            KnobParam("Volume", 0, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 1, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 2, 50f, 0f, 100f, 1f)
        ), category = "Bass Amp", basedOn = "VOX® AC-100 bass (vintage)", svgFile = "foxy-bass.svg"),
        117440631 to EffectDefinition(117440631, "Mess Bass", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Bass Amp", basedOn = "Mesa/Boogie® Bass 400", svgFile = "mess-bass.svg"),
        117440635 to EffectDefinition(117440635, "Knights CL+", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 20f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Clean Amp", basedOn = "Grindrod® Pendragon PG20C (clean, bright on)", svgFile = "knights-cl-plus.svg"),
        117440636 to EffectDefinition(117440636, "Knights OD", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 30f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Drive Amp", basedOn = "Grindrod® Pendragon PG20C (overdrive)", svgFile = "knights-od.svg"),
        134217845 to EffectDefinition(134217845, "Mini Bass", ModuleType.AMP, listOf(
            KnobParam("Volume", 0, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 1, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 2, 50f, 0f, 100f, 1f)
        ), category = "Bass Amp", basedOn = "Ampeg® B-15 \"Flip Top\"", svgFile = "mini-bass.svg"),
        134217846 to EffectDefinition(134217846, "Bass Pre", ModuleType.AMP, listOf(
            KnobParam("Volume", 0, 50f, 0f, 100f, 1f),
            SwitchParam("Bright", 1, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1))),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "Bass Amp", basedOn = "Alembic™ F-2B tube preamp", svgFile = "bass-pre.svg"),
        134217850 to EffectDefinition(134217850, "AC Pre", ModuleType.AMP, listOf(
            KnobParam("Volume", 0, 50f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Balance", 2, 50f, 0f, 100f, 1f),
            KnobParam("EQ Freq", 3, 50f, 0f, 100f, 1f),
            KnobParam("EQ Q", 4, 50f, 0f, 100f, 1f),
            KnobParam("EQ Gain", 5, 50f, 0f, 100f, 1f)
        ), category = "Acoustic Preamp", basedOn = "AER® Colourizer 2 (EQ F1: 90 Hz – 1.6 kHz)", svgFile = "ac-pre.svg"),
        134217851 to EffectDefinition(134217851, "AC Pre 2", ModuleType.AMP, listOf(
            KnobParam("Volume", 0, 50f, 0f, 100f, 1f),
            KnobParam("Tone", 1, 50f, 0f, 100f, 1f),
            KnobParam("Balance", 2, 50f, 0f, 100f, 1f),
            KnobParam("EQ Freq", 3, 50f, 0f, 100f, 1f),
            KnobParam("EQ Q", 4, 50f, 0f, 100f, 1f),
            KnobParam("EQ Gain", 5, 50f, 0f, 100f, 1f)
        ), category = "Acoustic Preamp", basedOn = "AER® Colourizer 2 (EQ F2: 680 Hz – 11 kHz)", svgFile = "ac-pre-2.svg"),
        167772160 to EffectDefinition(167772160, "SUP ZEP", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Supro® 1×6\" cab (oval speaker)", svgFile = "sup-zep.svg"),
        167772161 to EffectDefinition(167772161, "TWD CP", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Vintage Fender® Champ 1×8\"", svgFile = "twd-cp.svg"),
        167772162 to EffectDefinition(167772162, "TWD PRC", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Vintage Fender® Princeton 1×10\"", svgFile = "twd-prc.svg"),
        167772163 to EffectDefinition(167772163, "DARK LUX", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Vintage Fender® Deluxe 1×12\"", svgFile = "dark-lux.svg"),
        167772164 to EffectDefinition(167772164, "DARK VIT", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Vintage Fender® Vibrolux 1×12\"", svgFile = "dark-vit.svg"),
        167772165 to EffectDefinition(167772165, "ROUT", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Carr® Rambler 1×12\"", svgFile = "rout.svg"),
        167772166 to EffectDefinition(167772166, "BogSV", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Bogner® Shiva 1×12\"", svgFile = "bogsv.svg"),
        167772167 to EffectDefinition(167772167, "Bad-KT", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Bad Cat® Hot Cat 1×12\"", svgFile = "bad-kt.svg"),
        167772168 to EffectDefinition(167772168, "FOXY 1", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Vintage VOX® AC15 1×12\"", svgFile = "foxy-1.svg"),
        167772169 to EffectDefinition(167772169, "D STAR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Mesa/Boogie® Lone Star 1×12\"", svgFile = "d-star.svg"),
        167772170 to EffectDefinition(167772170, "TOM OPEN", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Swart® Atomic Space 1×12\"", svgFile = "tom-open.svg"),
        167772171 to EffectDefinition(167772171, "TWD LUX", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Fender® Tweed Deluxe 1×12\"", svgFile = "twd-lux.svg"),
        167772172 to EffectDefinition(167772172, "US STO", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "1980's Mesa/Boogie® 1×12\"", svgFile = "us-sto.svg"),
        167772173 to EffectDefinition(167772173, "ACE", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Morgan® AC20 Deluxe 1×12\"", svgFile = "ace.svg"),
        167772174 to EffectDefinition(167772174, "UK G12", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Marshall® 1×12\"", svgFile = "uk-g12.svg"),
        167772175 to EffectDefinition(167772175, "FOXY 2", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Vintage VOX® AC30 2×12\"", svgFile = "foxy-2.svg"),
        167772176 to EffectDefinition(167772176, "Match", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Matchless® Chieftain 2×12\"", svgFile = "match.svg"),
        167772177 to EffectDefinition(167772177, "J-120", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Roland® Jazz Chorus 2×12\"", svgFile = "j-120.svg"),
        167772178 to EffectDefinition(167772178, "Dark Twin", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Vintage Fender® '65 Twin Reverb 2×12\"", svgFile = "dark-twin--cab.svg"),
        167772179 to EffectDefinition(167772179, "UK GRN 1", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Marshall® 2550 Silver Jubilee 2×12\"", svgFile = "uk-grn-1.svg"),
        167772180 to EffectDefinition(167772180, "TWD SUP", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Custom Fender® Tweed 2×10\"", svgFile = "twd-sup.svg"),
        167772181 to EffectDefinition(167772181, "BOUTI", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Unique custom 2×12\"", svgFile = "bouti.svg"),
        167772182 to EffectDefinition(167772182, "Bellman 1", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Vintage Fender® \"Piggyback\" Bassman 2×12\"", svgFile = "bellman-1.svg"),
        167772183 to EffectDefinition(167772183, "SUP", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Supro® 1624T 2×12\"", svgFile = "sup.svg"),
        167772184 to EffectDefinition(167772184, "MATT TWD", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Matchless® 2×12\"", svgFile = "matt-twd.svg"),
        167772185 to EffectDefinition(167772185, "SUP Star", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Mesa/Boogie® Lone Star 2×12\"", svgFile = "sup-star.svg"),
        167772186 to EffectDefinition(167772186, "Freed", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Fryette® Deliverance 2×12\"", svgFile = "freed.svg"),
        167772187 to EffectDefinition(167772187, "Dark CS", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Custom-modified Fender® 2×12\"", svgFile = "dark-cs.svg"),
        167772188 to EffectDefinition(167772188, "DB Rock", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Two-Rock® 2×12\"", svgFile = "db-rock.svg"),
        167772189 to EffectDefinition(167772189, "Blue SK", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Custom 2×12\" w/ Celestion® Alnico Blue", svgFile = "blue-sk.svg"),
        167772190 to EffectDefinition(167772190, "Bellman 2", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Fender® '59 Bassman 4×10\"", svgFile = "bellman-2.svg"),
        167772191 to EffectDefinition(167772191, "UK LD", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Marshall® 1960AV 4×12\"", svgFile = "uk-ld.svg"),
        167772192 to EffectDefinition(167772192, "UK TD", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "'68 Marshall® basketweave 4×12\"", svgFile = "uk-td.svg"),
        167772193 to EffectDefinition(167772193, "UK MD", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Custom-modified Marshall® 4×12\"", svgFile = "uk-md.svg"),
        167772194 to EffectDefinition(167772194, "UK GRN 2", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Vintage Marshall® 4×12\" w/ Celestion® Greenbacks", svgFile = "uk-grn-2.svg"),
        167772195 to EffectDefinition(167772195, "EV", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Peavey® 6505 4×12\"", svgFile = "ev.svg"),
        167772196 to EffectDefinition(167772196, "Mess", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Mesa/Boogie® Rectifier 4×12\"", svgFile = "mess.svg"),
        167772197 to EffectDefinition(167772197, "Bog", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Bogner® 4×12\"", svgFile = "bog.svg"),
        167772198 to EffectDefinition(167772198, "Eagle", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "ENGL® 4×12\"", svgFile = "eagle.svg"),
        167772199 to EffectDefinition(167772199, "Uban", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Bogner® Über Kab 4×12\"", svgFile = "uban.svg"),
        167772200 to EffectDefinition(167772200, "Solo", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Soldano® 4×12\"", svgFile = "solo.svg"),
        167772201 to EffectDefinition(167772201, "Juice", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Orange® PPC412 4×12\"", svgFile = "juice.svg"),
        167772202 to EffectDefinition(167772202, "H-WAY", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Vintage Hiwatt® SE4123 4×12\"", svgFile = "h-way.svg"),
        167772203 to EffectDefinition(167772203, "UK Dark", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "1968 Marshall® 4×12\"", svgFile = "uk-dark.svg"),
        167772204 to EffectDefinition(167772204, "Way", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Vintage WEM® 4×12\"", svgFile = "way.svg"),
        167772205 to EffectDefinition(167772205, "Dumb", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Dumble® 4×12\"", svgFile = "dumb.svg"),
        167772206 to EffectDefinition(167772206, "Dizz", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Diezel® 4×12\"", svgFile = "dizz.svg"),
        167772207 to EffectDefinition(167772207, "TRP", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Hughes & Kettner® Triamp 4×12\"", svgFile = "trp.svg"),
        167772208 to EffectDefinition(167772208, "UK 75", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Marshall® 4×12\" w/ Celestion® G12T-75", svgFile = "uk-75.svg"),
        167772209 to EffectDefinition(167772209, "King", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Mesa/Boogie® Road King 4×12\"", svgFile = "king.svg"),
        167772210 to EffectDefinition(167772210, "ADM 1", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "David Eden® 1×15\" bass cab", svgFile = "adm-1.svg"),
        167772211 to EffectDefinition(167772211, "Workman 1", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "SWR® 1×15\" bass cab", svgFile = "workman-1.svg"),
        167772212 to EffectDefinition(167772212, "F-TOP", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Ampeg® PF-115HE 1×15\" bass cab", svgFile = "f-top.svg"),
        167772213 to EffectDefinition(167772213, "US BASS", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Mesa/Boogie® 2×10\" bass cab", svgFile = "us-bass.svg"),
        167772214 to EffectDefinition(167772214, "MATT", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Markbass® 2×10\" bass cab", svgFile = "matt.svg"),
        167772215 to EffectDefinition(167772215, "ADM 2", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "David Eden® 4×10\" bass cab", svgFile = "adm-2.svg"),
        167772216 to EffectDefinition(167772216, "AMPG 1", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Ampeg® SVT-410HE 4×10\" bass cab", svgFile = "ampg-1.svg"),
        167772217 to EffectDefinition(167772217, "Workman 2", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "SWR® Workingman's 4×10\" bass cab", svgFile = "workman-2.svg"),
        167772218 to EffectDefinition(167772218, "HACK", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Hartke® 4×12\" (aluminum cones)", svgFile = "hack.svg"),
        167772219 to EffectDefinition(167772219, "AMPG 2", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Cabinet", basedOn = "Ampeg® SVT-810E 8×10\" fridge", svgFile = "ampg-2.svg"),
        167772220 to EffectDefinition(167772220, "AC", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Acoustic IR", basedOn = "Dreadnought acoustic simulation 1", svgFile = "ac.svg"),
        167772221 to EffectDefinition(167772221, "AC Dream", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Acoustic IR", basedOn = "Dreadnought acoustic simulation 2", svgFile = "ac-dream.svg"),
        167772222 to EffectDefinition(167772222, "OM", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Acoustic IR", basedOn = "OM-body acoustic simulation", svgFile = "om.svg"),
        167772223 to EffectDefinition(167772223, "JUMBO", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Acoustic IR", basedOn = "Jumbo acoustic simulation", svgFile = "jumbo.svg"),
        167772224 to EffectDefinition(167772224, "Bird", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Acoustic IR", basedOn = "Iconic \"H-Bird\" acoustic simulation", svgFile = "bird.svg"),
        167772225 to EffectDefinition(167772225, "GA", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Acoustic IR", basedOn = "Grand Auditorium acoustic simulation", svgFile = "ga.svg"),
        167772226 to EffectDefinition(167772226, "Classic AC", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Acoustic IR", basedOn = "Classical (nylon) guitar simulation", svgFile = "classic-ac.svg"),
        167772227 to EffectDefinition(167772227, "Mandolin", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Acoustic IR", basedOn = "Mandolin simulation", svgFile = "mandolin.svg"),
        167772228 to EffectDefinition(167772228, "Fretless Bass", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Acoustic IR", basedOn = "Fretless acoustic bass simulation", svgFile = "fretless-bass.svg"),
        167772229 to EffectDefinition(167772229, "Double Bass", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "Acoustic IR", basedOn = "Double bass simulation", svgFile = "double-bass.svg"),
        168820736 to EffectDefinition(168820736, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820737 to EffectDefinition(168820737, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820738 to EffectDefinition(168820738, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820739 to EffectDefinition(168820739, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820740 to EffectDefinition(168820740, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820741 to EffectDefinition(168820741, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820742 to EffectDefinition(168820742, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820743 to EffectDefinition(168820743, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820744 to EffectDefinition(168820744, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820745 to EffectDefinition(168820745, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820746 to EffectDefinition(168820746, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820747 to EffectDefinition(168820747, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820748 to EffectDefinition(168820748, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820749 to EffectDefinition(168820749, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820750 to EffectDefinition(168820750, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820751 to EffectDefinition(168820751, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820752 to EffectDefinition(168820752, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820753 to EffectDefinition(168820753, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820754 to EffectDefinition(168820754, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        168820755 to EffectDefinition(168820755, "User IR", ModuleType.CAB, listOf(
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Low Cut", 5, 19f, 19f, 2000f, 1f),
            KnobParam("High Cut", 6, 20001f, 2000f, 20001f, 1f)
        ), category = "User IR", basedOn = "Your imported IR (44.1 kHz / 1024-sample WAV)", svgFile = "user-ir.svg"),
        184549376 to EffectDefinition(184549376, "Pure", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Valeton original: pristine digital delay", svgFile = "pure.svg"),
        184549377 to EffectDefinition(184549377, "Analog", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Warm BBD-style analog delay", svgFile = "analog.svg"),
        184549378 to EffectDefinition(184549378, "Tape", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Roland® RE-201 Space Echo", svgFile = "tape.svg"),
        184549379 to EffectDefinition(184549379, "Dual Echo", ModuleType.DLY, listOf(
            KnobParam("Mix A", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time A", 2, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback A", 1, 20f, -100f, 100f, 1f),
            KnobParam("Mix B", 3, 20f, 0f, 100f, 1f),
            KnobParam("Time B", 5, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback B", 4, 20f, -100f, 100f, 1f),
            SwitchParam("Sync A", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 2),
            SwitchParam("Sync B", 7, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 5),
            SwitchParam("Trail", 8, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Dual L/R delay (Andy Timmons \"Halo\"-style)", svgFile = "dual-echo.svg"),
        184549380 to EffectDefinition(184549380, "Ping Pong", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 2, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 1, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 2),
            SwitchParam("Trail", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Valeton original: stereo bouncing repeats", svgFile = "ping-pong.svg"),
        184549381 to EffectDefinition(184549381, "Slapback", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 150f, 20f, 300f, 1f),
            KnobParam("Feedback", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Classic 50s-style short slapback echo", svgFile = "slapback.svg"),
        184549382 to EffectDefinition(184549382, "Sweep Echo", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 2, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 1, 50f, 0f, 100f, 1f),
            KnobParam("Sweep Depth", 3, 50f, 0f, 100f, 1f),
            KnobParam("Sweep Rate", 4, 50f, 0f, 100f, 1f),
            SwitchParam("Sweep Sync", 5, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 4),
            SwitchParam("Time Sync", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 2),
            SwitchParam("Trail", 7, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Valeton original: filter-swept repeats", svgFile = "sweep-echo.svg"),
        184549385 to EffectDefinition(184549385, "Ring Echo", ModuleType.DLY, listOf(
            KnobParam("Dly Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 2, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 1, 50f, 0f, 100f, 1f),
            KnobParam("Ring Mix", 3, 50f, 0f, 100f, 1f),
            KnobParam("Freq", 4, 50f, 0f, 100f, 1f),
            KnobParam("Tone", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 2),
            SwitchParam("Trail", 7, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Valeton original: ring-modulated repeats", svgFile = "ring-echo.svg"),
        184549387 to EffectDefinition(184549387, "Tube", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 30f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Binson® Echorec magnetic-drum echo", svgFile = "tube--dly.svg"),
        184549388 to EffectDefinition(184549388, "M-Echo", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 30f, 0f, 100f, 1f),
            KnobParam("Tone", 3, 50f, 0f, 100f, 1f),
            KnobParam("Mode", 4, 5f, 1f, 12f, 1f),
            SwitchParam("Sync", 5, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "4-head multi-tap tape echo machine", svgFile = "m-echo.svg"),
        184549389 to EffectDefinition(184549389, "Sweet Echo", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 30f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Maxon® AD900 Analog Delay", svgFile = "sweet-echo.svg"),
        184549394 to EffectDefinition(184549394, "999 Echo", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 30f, 0f, 100f, 1f),
            SwitchParam("Sync", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Vintage 80's rack delay (likely Boss® RDD-10)", svgFile = "999-echo.svg"),
        184549396 to EffectDefinition(184549396, "Vintage Rack", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 2, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 1, 50f, 0f, 100f, 1f),
            KnobParam("Mod", 3, 50f, 0f, 100f, 1f),
            KnobParam("Tone", 4, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 5, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 2),
            SwitchParam("Trail", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Vintage 1980's rack-mount digital delay", svgFile = "vintage-rack.svg"),
        184549405 to EffectDefinition(184549405, "BBD Delay S", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 20f, -100f, 100f, 1f),
            KnobParam("Time R%", 3, 61.8f, 1f, 100f, 1f),
            KnobParam("Spread", 4, 100f, -100f, 100f, 1f),
            KnobParam("Level", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 7, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Boss® DM-3 stereo analog delay (BBD)", svgFile = "bbd-delay-s.svg"),
        184549407 to EffectDefinition(184549407, "Digital Delay S", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 20f, -100f, 100f, 1f),
            KnobParam("Time R%", 3, 61.8f, 1f, 100f, 1f),
            KnobParam("Spread", 4, 100f, -100f, 100f, 1f),
            KnobParam("Level", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 7, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Boss® DD-3 stereo digital delay", svgFile = "digital-delay-s.svg"),
        184549409 to EffectDefinition(184549409, "Tape Delay S", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 20f, -100f, 100f, 1f),
            KnobParam("Time R%", 3, 61.8f, 1f, 100f, 1f),
            KnobParam("Spread", 4, 100f, -100f, 100f, 1f),
            KnobParam("Wow &amp;", 5, 50f, 0f, 100f, 1f),
            ComboBoxParam("Age", 6, 0f, listOf(OptionItem("Past", 0), OptionItem("Present", 1), OptionItem("Future", 2))),
            KnobParam("Scrape", 7, 100f, 0f, 100f, 1f),
            KnobParam("Drive", 8, 0f, 0f, 100f, 1f),
            KnobParam("Level", 9, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 10, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 11, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Roland® RE-201 Space Echo (stereo)", svgFile = "tape-delay-s.svg"),
        184549414 to EffectDefinition(184549414, "Lofi Echo", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 20f, -100f, 100f, 1f),
            KnobParam("Crush", 3, 20f, 0f, 100f, 1f),
            KnobParam("Bit", 4, 20f, 0f, 100f, 1f),
            SwitchParam("Sync", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 7, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Valeton original: degraded lo-fi repeats", svgFile = "lofi-echo.svg"),
        184549416 to EffectDefinition(184549416, "Rev Echo", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 20f, -100f, 100f, 1f),
            KnobParam("Level", 3, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 4, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 5, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Valeton original: reversed-feedback delay", svgFile = "rev-echo.svg"),
        184549418 to EffectDefinition(184549418, "Ambience 1", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 30f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 50f, 0f, 100f, 1f),
            KnobParam("Level", 3, 50f, 0f, 100f, 1f),
            KnobParam("Mod", 4, 100f, 0f, 100f, 1f),
            KnobParam("Tone", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 7, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Electro-Harmonix® Deluxe Memory Man (multi-tap voicing 1)", svgFile = "ambience-1.svg"),
        184549422 to EffectDefinition(184549422, "Ambience 2", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 30f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 50f, 0f, 100f, 1f),
            KnobParam("Level", 3, 50f, 0f, 100f, 1f),
            KnobParam("Mod", 4, 100f, 0f, 100f, 1f),
            KnobParam("Tone", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 7, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Electro-Harmonix® Deluxe Memory Man (multi-tap voicing 2)", svgFile = "ambience-2.svg"),
        184549426 to EffectDefinition(184549426, "Broken Delay", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 30f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 4000f, 1f),
            KnobParam("Feedback", 2, 20f, 0f, 100f, 1f),
            KnobParam("Latch", 3, 50f, 0f, 100f, 1f),
            KnobParam("Cut", 4, 100f, 0f, 100f, 1f),
            SwitchParam("Sync", 5, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Valeton original: glitch/stutter fragment delay", svgFile = "broken-delay.svg"),
        184549427 to EffectDefinition(184549427, "Ice Delay", ModuleType.DLY, listOf(
            KnobParam("Mix", 0, 20f, 0f, 100f, 1f),
            KnobParam("Time", 1, 500f, 20f, 2000f, 1f),
            KnobParam("Feedback", 2, 30f, 0f, 100f, 1f),
            KnobParam("Tone", 3, 50f, 0f, 100f, 1f),
            KnobParam("Mod", 4, 30f, 0f, 100f, 1f),
            ComboBoxParam("Pitch", 5, 28f, listOf(OptionItem("-Oct", 0), OptionItem("-11", 1), OptionItem("-10", 2), OptionItem("-9", 3), OptionItem("-8", 4), OptionItem("-7", 5), OptionItem("-6", 6), OptionItem("-5", 7), OptionItem("-4", 8), OptionItem("-3", 9), OptionItem("-2", 10), OptionItem("-1", 11), OptionItem("-50 cents", 12), OptionItem("-20 cents", 13), OptionItem("0", 14), OptionItem("+20 cents", 15), OptionItem("+50 cents", 16), OptionItem("1", 17), OptionItem("2", 18), OptionItem("3", 19), OptionItem("4", 20), OptionItem("5", 21), OptionItem("6", 22), OptionItem("7", 23), OptionItem("8", 24), OptionItem("9", 25), OptionItem("10", 26), OptionItem("11", 27), OptionItem("+Oct", 28))),
            ComboBoxParam("Slice", 6, 2f, listOf(OptionItem("Short", 0), OptionItem("Medium", 1), OptionItem("Long", 2))),
            KnobParam("Direction", 7, -100f, -100f, 100f, 1f),
            KnobParam("Blend", 8, 40f, 0f, 100f, 1f),
            KnobParam("Smooth", 9, 0f, 0f, 100f, 1f),
            KnobParam("Level", 10, 50f, 0f, 100f, 1f),
            SwitchParam("Sync", 11, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)), boundParamIndex = 1),
            SwitchParam("Trail", 12, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Delay", basedOn = "Valeton original: crystalline ambient delay", svgFile = "ice-delay.svg"),
        201326592 to EffectDefinition(201326592, "Room", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 30f, 0f, 100f, 1f),
            KnobParam("Pre Delay", 1, 50f, 0f, 100f, 1f),
            KnobParam("Decay", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Room simulation", svgFile = "room.svg"),
        201326593 to EffectDefinition(201326593, "Hall", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 30f, 0f, 100f, 1f),
            KnobParam("Pre Delay", 1, 50f, 0f, 100f, 1f),
            KnobParam("Decay", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Concert-hall simulation", svgFile = "hall.svg"),
        201326594 to EffectDefinition(201326594, "Church", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 30f, 0f, 100f, 1f),
            KnobParam("Pre Delay", 1, 50f, 0f, 100f, 1f),
            KnobParam("Decay", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Church / cathedral simulation", svgFile = "church.svg"),
        201326595 to EffectDefinition(201326595, "Plate", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 30f, 0f, 100f, 1f),
            KnobParam("Decay", 1, 50f, 0f, 100f, 1f),
            KnobParam("High Damp", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "EMT® 140 studio plate", svgFile = "plate.svg"),
        201326596 to EffectDefinition(201326596, "Spring", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 30f, 0f, 100f, 1f),
            KnobParam("Decay", 1, 50f, 0f, 100f, 1f),
            KnobParam("Tone", 2, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 3, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Roland® RE-201 spring tank", svgFile = "spring.svg"),
        201326598 to EffectDefinition(201326598, "N-Star", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 30f, 0f, 100f, 1f),
            KnobParam("Decay", 1, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 2, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Valeton original: lush bright decays", svgFile = "n-star.svg"),
        201326599 to EffectDefinition(201326599, "Deepsea", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 30f, 0f, 100f, 1f),
            KnobParam("Decay", 1, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 2, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Valeton original: huge deep decays", svgFile = "deepsea.svg"),
        201326600 to EffectDefinition(201326600, "Sweet Space", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 30f, 0f, 100f, 1f),
            KnobParam("Pre Delay", 1, 50f, 0f, 100f, 1f),
            KnobParam("Decay", 2, 50f, 0f, 100f, 1f),
            KnobParam("Lo End", 3, 0f, -50f, 50f, 1f),
            KnobParam("Hi End", 4, 0f, -50f, 50f, 1f),
            SwitchParam("Trail", 5, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Valeton original: lush modulated reverb", svgFile = "sweet-space.svg"),
        201326601 to EffectDefinition(201326601, "Shimmer", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 30f, 0f, 100f, 1f),
            KnobParam("Pre Delay", 1, 50f, 0f, 100f, 1f),
            KnobParam("Decay", 2, 50f, 0f, 100f, 1f),
            KnobParam("Lo End", 3, 0f, -50f, 50f, 1f),
            KnobParam("Hi End", 4, 0f, -50f, 50f, 1f),
            SwitchParam("Trail", 5, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Valeton original: octave-up shimmer trails", svgFile = "shimmer.svg"),
        201326603 to EffectDefinition(201326603, "Studio", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 25f, 0f, 100f, 1f),
            KnobParam("Pre Delay", 1, 0f, 0f, 300f, 1f),
            KnobParam("Decay", 2, 50f, 0f, 100f, 1f),
            KnobParam("Low Damp", 3, 50f, 0f, 100f, 1f),
            KnobParam("Hi Damp", 4, 50f, 0f, 100f, 1f),
            KnobParam("Mod", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Recording-studio room simulation", svgFile = "studio.svg"),
        201326604 to EffectDefinition(201326604, "Club", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 25f, 0f, 100f, 1f),
            KnobParam("Pre Delay", 1, 0f, 0f, 300f, 1f),
            KnobParam("Decay", 2, 70f, 0f, 100f, 1f),
            KnobParam("Low Damp", 3, 50f, 0f, 100f, 1f),
            KnobParam("Hi Damp", 4, 50f, 0f, 100f, 1f),
            KnobParam("Mod", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Small club simulation", svgFile = "club.svg"),
        201326605 to EffectDefinition(201326605, "Concert", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 25f, 0f, 100f, 1f),
            KnobParam("Pre Delay", 1, 100f, 0f, 300f, 1f),
            KnobParam("Decay", 2, 50f, 0f, 100f, 1f),
            KnobParam("Low Damp", 3, 50f, 0f, 100f, 1f),
            KnobParam("Hi Damp", 4, 50f, 0f, 100f, 1f),
            KnobParam("Mod", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Concert venue simulation", svgFile = "concert.svg"),
        201326606 to EffectDefinition(201326606, "Arena", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 25f, 0f, 100f, 1f),
            KnobParam("Pre Delay", 1, 100f, 0f, 300f, 1f),
            KnobParam("Decay", 2, 70f, 0f, 100f, 1f),
            KnobParam("Low Damp", 3, 50f, 0f, 100f, 1f),
            KnobParam("Hi Damp", 4, 50f, 0f, 100f, 1f),
            KnobParam("Mod", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Arena simulation", svgFile = "arena.svg"),
        201326609 to EffectDefinition(201326609, "Amp Spring", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 25f, 0f, 100f, 1f),
            KnobParam("Pre Delay", 1, 0f, 0f, 300f, 1f),
            KnobParam("Decay", 2, 50f, 0f, 100f, 1f),
            KnobParam("Low Damp", 3, 50f, 0f, 100f, 1f),
            KnobParam("Hi Damp", 4, 50f, 0f, 100f, 1f),
            KnobParam("Mod", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "Solid-state combo amp spring reverb", svgFile = "amp-spring.svg"),
        201326610 to EffectDefinition(201326610, "Tube Spring", ModuleType.RVB, listOf(
            KnobParam("Mix", 0, 25f, 0f, 100f, 1f),
            KnobParam("Pre Delay", 1, 0f, 0f, 300f, 1f),
            KnobParam("Decay", 2, 50f, 0f, 100f, 1f),
            KnobParam("Low Damp", 3, 50f, 0f, 100f, 1f),
            KnobParam("Hi Damp", 4, 50f, 0f, 100f, 1f),
            KnobParam("Mod", 5, 50f, 0f, 100f, 1f),
            SwitchParam("Trail", 6, 0f, listOf(OptionItem("OFF", 0), OptionItem("ON", 1)))
        ), category = "Reverb", basedOn = "60's Fender® tube spring reverb unit", svgFile = "tube-spring.svg"),
        251658240 to EffectDefinition(251658240, "SnapTone", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "NAM Player", basedOn = "Neural Amp Modeler capture player (amp captures)", svgFile = "snaptone--amp.svg"),
        251658241 to EffectDefinition(251658241, "SnapTone", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "NAM Player", basedOn = "Neural Amp Modeler capture player (amp captures)", svgFile = "snaptone--amp.svg"),
        251658242 to EffectDefinition(251658242, "SnapTone", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "NAM Player", basedOn = "Neural Amp Modeler capture player (amp captures)", svgFile = "snaptone--amp.svg"),
        251658243 to EffectDefinition(251658243, "SnapTone", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "NAM Player", basedOn = "Neural Amp Modeler capture player (amp captures)", svgFile = "snaptone--amp.svg"),
        251658244 to EffectDefinition(251658244, "SnapTone", ModuleType.AMP, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "NAM Player", basedOn = "Neural Amp Modeler capture player (amp captures)", svgFile = "snaptone--amp.svg"),
        251658245 to EffectDefinition(251658245, "SnapTone", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "NAM Player", basedOn = "Neural Amp Modeler capture player (drive captures)", svgFile = "snaptone--dst.svg"),
        251658246 to EffectDefinition(251658246, "SnapTone", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "NAM Player", basedOn = "Neural Amp Modeler capture player (drive captures)", svgFile = "snaptone--dst.svg"),
        251658247 to EffectDefinition(251658247, "SnapTone", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "NAM Player", basedOn = "Neural Amp Modeler capture player (drive captures)", svgFile = "snaptone--dst.svg"),
        251658248 to EffectDefinition(251658248, "SnapTone", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "NAM Player", basedOn = "Neural Amp Modeler capture player (drive captures)", svgFile = "snaptone--dst.svg"),
        251658249 to EffectDefinition(251658249, "SnapTone", ModuleType.DST, listOf(
            KnobParam("Gain", 0, 50f, 0f, 100f, 1f),
            KnobParam("Volume", 1, 50f, 0f, 100f, 1f),
            KnobParam("Bass", 2, 50f, 0f, 100f, 1f),
            KnobParam("Middle", 3, 50f, 0f, 100f, 1f),
            KnobParam("Treble", 4, 50f, 0f, 100f, 1f)
        ), category = "NAM Player", basedOn = "Neural Amp Modeler capture player (drive captures)", svgFile = "snaptone--dst.svg"),
    )

    fun getAllEffects(): List<EffectDefinition> = effectsMap.values.toList()

    fun getEffectById(id: Int): EffectDefinition? = effectsMap[id]

    /** PRST stores 15 floats; send supported wire IDs, with selectors before dependent knobs. */
    fun liveParameterIndices(effectId: Int, storedParameterCount: Int): List<Int> =
        effectsMap[effectId]?.params?.sortedBy { if (it is KnobParam) 1 else 0 }?.map { it.index }?.filter { it in 0 until minOf(storedParameterCount, 15) }
            ?: (0 until minOf(storedParameterCount, 15)).toList()

    fun getEffectsByModule(module: ModuleType): List<EffectDefinition> {
        return effectsMap.values.filter { it.module == module }.sortedBy { it.name }
    }

    fun getModuleName(effectId: Int): ModuleType {
        return effectsMap[effectId]?.module ?: ModuleType.UNKNOWN
    }

    private val categoriesByModule: Map<ModuleType, List<String>> by lazy {
        ModuleType.entries.associateWith { module ->
            val categories = effectsMap.values
                .filter { it.module == module }
                .map { it.category }
                .distinct()
            val nonOther = categories.filter { it != "Other" }.sorted()
            if ("Other" in categories) nonOther + "Other" else nonOther
        }
    }

    fun getCategoriesForModule(module: ModuleType): List<String> {
        return categoriesByModule[module] ?: emptyList()
    }
}
