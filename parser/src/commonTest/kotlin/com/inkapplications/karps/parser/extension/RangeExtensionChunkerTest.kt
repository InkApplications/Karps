package com.inkapplications.karps.parser.extension

import com.inkapplications.karps.structures.unit.miles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class RangeExtensionChunkerTest {
    @Test
    fun validRange() {
        val given = "RNG0050Test"

        val result = RangeExtensionChunker.popChunk(given)

        assertEquals(50.miles, result.result.value, "Range is parsed as miles.")
        assertEquals("Test", result.remainingData, "Parsed data is removed")
    }

    @Test
    fun illegalValue() {
        val given = "RNGHello World"

        assertFails("Should not parse illegal characters") { RangeExtensionChunker.popChunk(given) }
    }

    @Test
    fun missingControl() {
        val given = "PHG0050Test"

        assertFails("Should not parse with wrong control.") { RangeExtensionChunker.popChunk(given) }
    }
}
