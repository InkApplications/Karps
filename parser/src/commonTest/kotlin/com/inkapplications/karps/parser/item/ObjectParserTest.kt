package com.inkapplications.karps.parser.item

import com.inkapplications.karps.parser.TestData
import com.inkapplications.karps.parser.assertEquals
import com.inkapplications.karps.structures.ReportState
import com.inkapplications.karps.structures.at
import com.inkapplications.karps.structures.unit.Knots
import inkapplications.spondee.spatial.Degrees
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.test.*

class ObjectParserTest {
    @Test
    fun liveObject() {
        val given = "LEADER   *092345z4903.50N/07201.75W>088/036"

        val result = ObjectParser().parse(TestData.prototype.copy(body = given))
        val resultDateTime = result.timestamp?.toLocalDateTime(TimeZone.UTC)

        assertEquals("LEADER", result.name)
        assertEquals(ReportState.Live, result.state)
        assertEquals(9, resultDateTime?.dayOfMonth)
        assertEquals(23, resultDateTime?.hour)
        assertEquals(45, resultDateTime?.minute)
        assertEquals(49.0583, result.coordinates.latitude.asDecimal, 0.0001)
        assertEquals(-72.0292, result.coordinates.longitude.asDecimal, 0.0001)
        assertEquals(Degrees.of(88) at Knots.of(36), result.trajectory)
    }

    @Test
    fun killedObject() {
        val given = "LEADER   _092345z4903.50N/07201.75W>088/036"

        val result = ObjectParser().parse(TestData.prototype.copy(body = given))
        val resultDateTime = result.timestamp?.toLocalDateTime(TimeZone.UTC)

        assertEquals("LEADER", result.name)
        assertEquals(ReportState.Kill, result.state)
        assertEquals(9, resultDateTime?.dayOfMonth)
        assertEquals(23, resultDateTime?.hour)
        assertEquals(45, resultDateTime?.minute)
        assertEquals(49.0583, result.coordinates.latitude.asDecimal, 0.0001)
        assertEquals(-72.0292, result.coordinates.longitude.asDecimal, 0.0001)
        assertEquals(Degrees.of(88) at Knots.of(36), result.trajectory)
    }

    @Test
    fun nonObject() {
        val given = "LEA_092345z4903.50N/07201.75W>088/036"

        assertFails { ObjectParser().parse(TestData.prototype.copy(body = given)) }
    }
}
