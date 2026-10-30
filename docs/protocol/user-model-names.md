# GP-200 user IR and SnapTone names

Evidence: a `usbmon` capture of the official desktop editor (GP-200.exe V1.8.1, run under Wine) talking to the maintainer's **GP-200 LT** (USB `84ef:002c`) on 2026-10-29. Firmware was not recorded. During the capture the maintainer renamed `nam1→nam1t→nam1` (DST), `nam2→nam2t→nam2` (AMP) and `ir1→ir1t→ir1` (CAB). This is one physical device; other variants and firmwares are unverified.

Frames use the envelope and nibble encoding of `SysExCodec.buildFrame`. Unlike preset writes, the decoded payload has no `[00 00 04 00 mode slot]` prefix: it starts with the record ID.

## Name reads

Request, command `0x11`, 28 decoded bytes, one per index:

```
[recordId LE16] 18 00 | index LE16 | 01 00 | 20-byte tail
```

Response, command `0x12`, 28 decoded bytes:

```
[recordId LE16] 18 00 | index LE16 | 00 00 | 00 00 00 00 | name[16]
```

| Record | Kind | Indices | Catalog effect IDs |
|--------|------|---------|--------------------|
| `0x1009` | User IR (CAB) | 0..19 | `0x0A100000 + i` (168820736..168820755) |
| `0x1012` | SnapTone / NAM | 0..9 (0..4 AMP, 5..9 DST) | `0x0F000000 + i` (251658240..251658249) |

The name is NUL-terminated ASCII. Bytes after the NUL are stale buffer contents (`ir1\0lid\0…`) and must be discarded. Empty IR slots answered `User IR`; SnapTone slots without a user capture answered factory names such as `APP-5153-Ivory-` or `APP-SD1-Boost-I`.

The desktop editor fills the 20-byte request tail with uninitialized stack memory, and the bytes change between requests. The app sends zeros. **Hardware check (2026-10-29, GP-200 LT, app debug build):** with a zeroed tail the pedal answered all 30 reads in about 160 ms, with the same names as the capture (`ir1`, `2`, `ir 3`, `nam2` at SnapTone 1, `nam1` at SnapTone 5). The captured tail `5e 9e 67 00 ec 4e 1c 25 6c 5d 78 02 b0 f9 92 02 01 fa d1 07` is kept as `SysExCodec.USER_MODEL_CAPTURED_TAIL` as a fallback.

### Mapping evidence

When the maintainer opened each block, the editor sent effect changes `16 00 08 00 <block> 00 0e 03 <effectId LE32>`:

- `0x0F000005` in DST, where `nam1` sits at SnapTone index 5;
- `0x0F000001` in AMP, where `nam2` sits at index 1;
- `0x0A100000` in CAB, where `ir1` sits at IR index 0.

### Observed sequence

Right after connecting, the editor sent one request at a time. Each next request went out only after the previous response arrived, about 20 ms apart. The order was 20 IR reads, then 20 rhythm-name reads (record `0x000a`), then 10 SnapTone reads.

## Renames (observed, not implemented)

Command `0x12`, no prefix:

- SnapTone: `15 10 14 00 index 00 67 00 name[16]`
- IR: `0c 10 14 00 index 00 67 00 name[16]`

The meaning of `0x67` is unknown.
