package com.valeton.editor.ui.components

import com.valeton.editor.core.catalog.EffectCatalog
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class PedalArtworkAssetTest {

    @Test
    fun `all effect definitions reference existing svg assets`() {
        val assetDir = File("src/main/assets/pedals")
        assertTrue(assetDir.exists(), "Asset dir must exist at src/main/assets/pedals")

        val missing = EffectCatalog.getAllEffects()
            .filter { it.svgFile.isNotBlank() }
            .filterNot { File(assetDir, it.svgFile).exists() }

        assertTrue(
            missing.isEmpty(),
            "Missing SVG assets for effects: ${missing.map { "${it.name} -> ${it.svgFile}" }}"
        )
    }

    @Test
    fun `pedal asset directory contains exactly 278 svg files`() {
        val assetDir = File("src/main/assets/pedals")
        val svgFiles = assetDir.listFiles { _, name -> name.endsWith(".svg") } ?: emptyArray()
        assertTrue(svgFiles.size == 278, "Expected 278 SVG files, found ${svgFiles.size}")
    }
}
