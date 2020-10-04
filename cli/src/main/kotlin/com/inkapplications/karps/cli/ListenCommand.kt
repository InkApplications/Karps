package com.inkapplications.karps.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import com.inkapplications.karps.client.AprsClientModule
import com.inkapplications.karps.client.Credentials
import com.inkapplications.karps.parser.ParserModule
import com.inkapplications.karps.parser.AprsParser
import com.inkapplications.karps.structures.AprsPacket
import kimchi.logger.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext

const val esc: Char = 27.toChar()
const val lightRed = "${esc}[1;31m"
const val red = "${esc}[0;31m"
const val yellow = "${esc}[1;33m"
const val green = "${esc}[1;32m"
const val blue = "${esc}[1;34m"
const val magenta = "${esc}[1;35m"
const val normal = "${esc}[0m"

class ListenCommand: CliktCommand() {
    private val callsign by argument(
        name = "callsign",
        help = "Your personal radio callsign to use for connection"
    )

    private val server: String by option(
        names = *arrayOf("--server"),
        help = "APRS server to connect to."
    ).default("first.aprs.net")

    private val port: Int by option(
        names = *arrayOf("--port"),
        help = "APRS server to connect to."
    ).int().default(10152)

    private val filter by option(
        names = *arrayOf("--filter"),
        help = "Raw filter to specify as a server command."
    ).multiple()

    private val verbose by option(
        names = *arrayOf("--verbose")
    ).flag(default = false)

    private val debug by option(
        names = *arrayOf("--debug")
    ).flag(default = false)

    override fun run() {
        val test: CoroutineContext = Dispatchers.Main
        val writer = if (verbose) object: LogWriter by defaultWriter {
            override fun log(level: LogLevel, message: String, cause: Throwable?) {
                defaultWriter.log(level, message, cause)
                cause?.printStackTrace()
            }
        } else EmptyWriter
        val logger = ConsolidatedLogger(writer)
        val parser = ParserModule().defaultParser(logger)
        runBlocking {
            val client = AprsClientModule.createDataClient()
            client.connect(
                credentials = Credentials(callsign),
                server = server,
                port = port,
                filters = filter
            ) { read, write ->
                read.consumeAsFlow()
                    .filterNot { it.startsWith('#') }
                    .collect { data ->
                        runCatching { parser.fromString(data) }
                            .onSuccess { printPacket(it, data) }
                            .onFailure {
                                if (debug || verbose) {
                                    echo("\n${red}Parse failed for packet:${normal}")
                                    echo(" - $data")
                                    echo(" - ${it.message}")
                                    it.printStackTrace()
                                }
                            }
                }
            }
        }
    }

    private fun printPacket(packet: AprsPacket, data: String) = when (packet) {
        is AprsPacket.Position -> {
            echo("${blue.span("[${packet.source}]")}: ${packet.coordinates} ${packet.comment}")
        }
        is AprsPacket.Weather -> {
            echo("${yellow.span("[${packet.source}]")}: ${packet.temperature}")
        }
        is AprsPacket.ObjectReport -> {
            echo("${magenta.span("[${packet.source}]")}: ${packet.state.name} ${packet.name}")
        }
        is AprsPacket.ItemReport -> {
            echo("${magenta.span("[${packet.source}]")}: ${packet.state.name} ${packet.name}")
        }
        is AprsPacket.Message -> {
            echo("${green.span("[${packet.source}]")} -> ${green.span("[${packet.addressee}]")}: ${packet.message}")
        }
        is AprsPacket.Unknown -> {
            if (debug) {
                echo("${lightRed.span("[${packet.source}]")}: $data")
            } else {
                echo("${lightRed.span("[${packet.source}]")}: ${packet.body}")
            }
        }
    }

    private fun String.span(comment: String): String = "${this}${comment}${normal}"
}
