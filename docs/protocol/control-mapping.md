# GP-200 physical control mappings

Evidence: static inspection of the user's installed 32-bit GP-200.exe and the repository's three original PRST fixtures. Those fixtures were later replaced in the current tree by the maintainer's `clean.prst`; the analysis below records the original evidence, not a new hardware capture. Addresses below are executable virtual addresses; no software/device settings were changed. This documents verified command construction, not a physical-device capture.

## Three quick knobs

Writer0x464580 constructs `[10 00 04 00 index block parameterLE16]`, stores it in current/selected preset object+0x48c+8*index. Index0..2. OFF selection0x463ebf sends block255/parameter255. Existing files use parameter0 for OFF; decode OFF by block alone. Blocks11 and12 are Patch Volume and Patch Tempo (render0x4640c9/0x464133); parameter255. Normal effect menu enumerates block0..9 and all catalog parameters, including discrete types; do not offer block10 as a normal quick-knob destination.

## CTRL 1

Checkbox handler0x466f30 copies `[0f 00 08 00 index opaqueByte reservedLE16 targetMaskLE32]` then changes only one low bit in mask. Eight records at object+0x4a4+12*index. CTRL1=index0, confirmed by first button label construction0x46670d..0x4668f9 and click0x46790d.

Low mask bits0..10 correspond to PRE,WAH,DST,AMP,NR,CAB,EQ,MOD,DLY,RVB,VOL, confirmed by debug format0x2295090 and argument extraction0x467152..0x4671b9. Single-target edit replaces only low11bits; retain upper21bits, opaqueByte, reserved bytes, and all seven other records. Zero lowbits means no targets. Byte5's interpretation is unknown: desktop preserves it, so the app must do the same.

## Serialization

Fixtures contain three8-byte knob records at PRST0x448 and eight12-byte CTRL records at0x460. Existing preset name/effect offsets align file vs decoded live payload by−0x28 (name0x44→0x1c, effects0xa0→0x78). Consequently live records start0x420/0x438. Native object offsets align both independently known name/effect fields and control records (object name+0x88, effects+0xe4, knobs+0x48c, CTRL+0x4a4). This is structural/fixture evidence, not a claim to have traced file serializer memcpy.

## MIDI envelope

Raw records are not sent alone. PresetManager dispatcher0x48bc50 notifies a transport listener through vtable+0x34. Registration at0x5be528..0x5be66d associates transport+4 with vtable0x229b404; entry+0x34=0x5d6350. That callback prepends `[00 00 04 00 modeLE16 slotLE16]` (0x5d638b..0x5d63ac), appends the raw record (0x5d6411..0x5d6470), then calls0x5d0f10 with command0x12 and offset0. The8-byte copy uses0x49b420 with little-endian flag1. Existing working live parameter/reorder commands use mode0/slot0, meaning the active edit buffer; writer0x4645f6 also selects the active preset for mode0. Use the same active-buffer prefix for new live writes.

Therefore a quick mapping is16 decoded bytes/46 wire bytes, and CTRL is20 decoded bytes/54 wire bytes, in the existing header/nibble encoding/F7 framing. Saving uses the existing commit and full readback; compare decoded mappings before claiming success. Hardware validation remains required.
