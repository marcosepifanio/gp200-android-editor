package com.valeton.editor.core.catalog

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class EffectParameterSchemaTest {
    @Test
    fun `all 305 models use wire IDs from the Windows algorithm schema rather than UI positions`() {
        val rows = javaClass.getResourceAsStream("/fixtures/effect-parameter-schema.tsv")!!
            .bufferedReader().readLines().filterNot { it.startsWith("#") || it.isBlank() }
        assertEquals(305, rows.size)
        val failures = rows.mapNotNull { row ->
            val fields = row.split('\t')
            val model = EffectCatalog.getEffectById(fields[0].toInt())
            val ids = fields[2].split(',').map(String::toInt)
            val names = fields[4].split('|').map { it.lowercase() }
            val reference = names.zip(ids).toMap()
            val actual = model?.params?.associate { it.name.replace("&amp;", "&").lowercase() to it.index }
            if (actual != reference) "${fields[1]}/${model?.name}: expected $reference, got $actual" else null
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
        for (row in rows) {
            val fields = row.split('\t')
            val model = EffectCatalog.getEffectById(fields[0].toInt())!!
            val expected = if (fields[3] == "-") emptyMap() else fields[3].split(',').associate {
                val pair = it.split('>').map(String::toInt)
                pair[0] to pair[1]
            }
            val actual = model.params.filterIsInstance<com.valeton.editor.core.model.SwitchParam>()
                .filter { it.boundParamIndex != null }.associate { it.index to it.boundParamIndex!! }
            assertEquals(expected, actual, "${model.name}: linked parameter wire IDs")
        }
        assertEquals(rows.size, EffectCatalog.getAllEffects().size)
    }
}
