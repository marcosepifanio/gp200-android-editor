package com.valeton.editor.repository

import com.valeton.editor.core.model.EffectSlot
import com.valeton.editor.core.model.GP200Preset
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.time.LocalDate

class PresetLibraryRepositoryTest {

    @TempDir
    lateinit var tempDir: File

    private lateinit var repository: PresetLibraryRepository

    @BeforeEach
    fun setUp() {
        repository = PresetLibraryRepository(tempDir)
    }

    private fun createDummyPreset(name: String, tempo: Int = 120, slot: Int = 0): GP200Preset {
        return GP200Preset(
            version = "1.0",
            patchName = name,
            author = "Tester",
            effects = listOf(
                EffectSlot(slotIndex = 0, effectId = 101, enabled = true, params = listOf(50f, 25f, 0.5f)),
                EffectSlot(slotIndex = 1, effectId = 202, enabled = false, params = listOf(10f, 20f))
            ),
            routingOrder = (0..10).toList(),
            fxLoopSend = 5,
            fxLoopReturn = 6,
            patchVolume = 60,
            patchPan = 10,
            patchTempo = tempo,
            checksum = 12345
        )
    }

    @Test
    fun `isLibraryCached returns false when cache does not exist`() {
        assertFalse(repository.isLibraryCached())
        assertNull(repository.loadFullLibrary())
    }

    @Test
    fun `saveFullLibrary writes 256 presets and isLibraryCached returns true`() {
        val library = (0..255).associateWith { idx ->
            createDummyPreset("Preset $idx", tempo = 100 + (idx % 50), slot = idx)
        }

        repository.saveFullLibrary(library)

        assertTrue(repository.isLibraryCached())
        val loaded = repository.loadFullLibrary()
        assertNotNull(loaded)
        assertEquals(256, loaded!!.size)

        val p0 = loaded[0]
        assertNotNull(p0)
        assertEquals("Preset 0", p0!!.patchName)
        assertEquals("Tester", p0.author)
        assertEquals("1.0", p0.version)
        assertEquals(100, p0.patchTempo)
        assertEquals(60, p0.patchVolume)
        assertEquals(10, p0.patchPan)
        assertEquals(5, p0.fxLoopSend)
        assertEquals(6, p0.fxLoopReturn)
        assertEquals(12345, p0.checksum)
        assertEquals((0..10).toList(), p0.routingOrder)
        assertEquals(2, p0.effects.size)
        assertEquals(0, p0.effects[0].slotIndex)
        assertEquals(101, p0.effects[0].effectId)
        assertTrue(p0.effects[0].enabled)
        assertEquals(listOf(50f, 25f, 0.5f), p0.effects[0].params)
        assertEquals(1, p0.effects[1].slotIndex)
        assertEquals(202, p0.effects[1].effectId)
        assertFalse(p0.effects[1].enabled)

        assertEquals("Preset 255", loaded[255]?.patchName)
    }

    @Test
    fun `updatePreset updates single slot in cache`() {
        val library = (0..255).associateWith { idx -> createDummyPreset("Preset $idx") }
        repository.saveFullLibrary(library)

        val updated = createDummyPreset("Custom Solo Lead", tempo = 145)
        repository.updatePreset(42, updated)

        val loaded = repository.loadFullLibrary()
        assertNotNull(loaded)
        assertEquals("Custom Solo Lead", loaded!![42]?.patchName)
        assertEquals(145, loaded[42]?.patchTempo)
        assertEquals("Preset 41", loaded[41]?.patchName)
    }

    @Test
    fun `updatePreset creates cache when cache was empty`() {
        val preset = createDummyPreset("Solo Preset", tempo = 130)
        repository.updatePreset(5, preset)

        assertTrue(repository.isLibraryCached())
        val loaded = repository.loadFullLibrary()
        assertNotNull(loaded)
        assertEquals("Solo Preset", loaded!![5]?.patchName)
    }

    @Test
    fun `clearCache removes cache file and resets isLibraryCached`() {
        val library = (0..255).associateWith { idx -> createDummyPreset("Preset $idx") }
        repository.saveFullLibrary(library)
        assertTrue(repository.isLibraryCached())

        repository.clearCache()

        assertFalse(repository.isLibraryCached())
        assertNull(repository.loadFullLibrary())
    }

    @Test
    fun `corrupted JSON file returns null gracefully without crashing`() {
        val cacheFile = File(tempDir, "library_cache.json")
        cacheFile.writeText("invalid json content {{{ [[[", Charsets.UTF_8)

        val loaded = repository.loadFullLibrary()
        assertNull(loaded)
        assertFalse(repository.isLibraryCached())
    }

    @Test
    fun `saveFullLibrary and loadFullLibrary preserves rawSource byte array`() {
        val expectedRaw = byteArrayOf(0x00, 0x12, 0x7F, -1, 0x55)
        val presetWithRaw = createDummyPreset("Preset With Raw").copy(rawSource = expectedRaw)

        repository.saveFullLibrary(mapOf(10 to presetWithRaw))

        val loaded = repository.loadFullLibrary()
        assertNotNull(loaded)
        val loadedPreset = loaded!![10]
        assertNotNull(loadedPreset)
        assertNotNull(loadedPreset!!.rawSource)
        assertArrayEquals(expectedRaw, loadedPreset.rawSource)
    }

    @Test
    fun `loadFullLibrary ignores slots outside 0 to 255`() {
        val cacheFile = File(tempDir, "library_cache.json")
        val json = """
            {
                "-1": {"patchName": "Negative Slot"},
                "10": {"patchName": "Valid Slot 10"},
                "256": {"patchName": "Overflow Slot"}
            }
        """.trimIndent()
        cacheFile.writeText(json, Charsets.UTF_8)

        val loaded = repository.loadFullLibrary()
        assertNotNull(loaded)
        assertEquals(1, loaded!!.size)
        assertEquals("Valid Slot 10", loaded[10]?.patchName)
        assertNull(loaded[-1])
        assertNull(loaded[256])
    }

    @Test
    fun `isLibraryCached returns false when file exists but is empty`() {
        val cacheFile = File(tempDir, "library_cache.json")
        cacheFile.writeText("", Charsets.UTF_8)

        assertFalse(repository.isLibraryCached())
    }

    @Test
    fun `isLibraryCached returns false when cached presets have all empty effects`() {
        val emptyPresets = (0..255).associateWith { idx ->
            GP200Preset(patchName = "Patch $idx", effects = emptyList())
        }
        repository.saveFullLibrary(emptyPresets)
        assertFalse(repository.isLibraryCached(), "Cache with zero effects in all presets must be considered invalid")
    }
    @Test
    fun `cache preserves drumSync enabled and disabled`() {
        repository.saveFullLibrary(mapOf(
            0 to createDummyPreset("Synced").copy(drumSync = true),
            1 to createDummyPreset("Independent").copy(drumSync = false)
        ))
        val loaded = repository.loadFullLibrary()!!
        assertTrue(loaded.getValue(0).drumSync)
        assertFalse(loaded.getValue(1).drumSync)
    }

    @Test
    fun `legacy cache without drumSync defaults to disabled`() {
        File(tempDir, "library_cache.json").writeText("""{"0":{"patchName":"Legacy"}}""")
        assertFalse(repository.loadFullLibrary()!!.getValue(0).drumSync)
    }

    @Test
    fun `cache preserves physical controls including opaque CTRL fields`() {
        val bytes = javaClass.getResourceAsStream("/fixtures/clean.prst")?.readBytes()
            ?: File("../core-protocol/src/test/resources/fixtures/clean.prst").readBytes()
        val preset = com.valeton.editor.core.codec.PRSTDecoder(bytes).decode()
        repository.saveFullLibrary(mapOf(3 to preset))
        assertNotNull(preset.controlMappings)
        assertEquals(preset.controlMappings, repository.loadFullLibrary()!!.getValue(3).controlMappings)
    }

    @Test
    fun `legacy cache cannot turn unknown controls into defaults`() {
        repository.saveFullLibrary(mapOf(0 to createDummyPreset("Unknown")))
        assertNull(repository.loadFullLibrary()!!.getValue(0).controlMappings)
    }

    @Test
    fun `last full sync date round-trips and is null for caches without it`() {
        repository.saveFullLibrary(mapOf(0 to createDummyPreset("A")))
        assertNull(repository.lastFullSyncDate())

        repository.markFullSync(LocalDate.of(2026, 10, 30))
        assertEquals(LocalDate.of(2026, 10, 30), PresetLibraryRepository(tempDir).lastFullSyncDate())
        assertEquals("A", repository.loadFullLibrary()!!.getValue(0).patchName)
    }

    @Test
    fun `clearCache also forgets the last full sync date`() {
        repository.saveFullLibrary(mapOf(0 to createDummyPreset("A")))
        repository.markFullSync(LocalDate.of(2026, 10, 30))
        repository.clearCache()
        assertNull(repository.lastFullSyncDate())
    }

    @Test
    fun `corrupt last sync file is treated as never synced`() {
        File(tempDir, "library_last_sync.txt").writeText("ontem")
        assertNull(repository.lastFullSyncDate())
    }
}
