package com.inkapplications.karps.parser.position

import com.inkapplications.karps.parser.ambiguousValue
import com.inkapplications.karps.parser.chunk.Chunker
import com.inkapplications.karps.parser.chunk.Chunk
import com.inkapplications.karps.parser.chunk.requireControl
import com.inkapplications.karps.structures.symbolOf
import inkapplications.spondee.spatial.*


internal object PlainPositionChunker: Chunker<PositionReport.Plain> {
    override fun popChunk(data: String): Chunk<PositionReport.Plain> {
        val latDegrees = data.substring(0, 2).ambiguousValue
        val latMinutes = data.substring(2, 4).ambiguousValue
        data[4].requireControl('.')
        val latSeconds = data.substring(5, 7).ambiguousValue * .6f
        val latCardinal = data[7].toCardinal()

        val tableIdentifier = data[8]

        val longDegrees = data.substring(9, 12).ambiguousValue
        val longMinutes = data.substring(12, 14).ambiguousValue
        data[14].requireControl('.')
        val longSeconds = data.substring(15, 17).ambiguousValue * .6f
        val longCardinal = data[17].toCardinal()

        val codeIdentifier = data[18]

        val latitude = latitudeOf(
            degreesComponent = latDegrees,
            minutesComponent = latMinutes,
            secondsComponent = latSeconds,
            cardinal = latCardinal
        )
        val longitude = longitudeOf(
            degreesComponent = longDegrees,
            minutesComponent = longMinutes,
            secondsComponent = longSeconds,
            cardinal = longCardinal
        )

        val coordinates = GeoCoordinates(latitude, longitude)
        val symbol = symbolOf(
            tableIdentifier = tableIdentifier,
            codeIdentifier = codeIdentifier
        )
        val symbolEmbeddedPosition = PositionReport.Plain(coordinates, symbol)

        return Chunk(symbolEmbeddedPosition, data.substring(19))
    }
}
