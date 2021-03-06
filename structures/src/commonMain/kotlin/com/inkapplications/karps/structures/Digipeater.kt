package com.inkapplications.karps.structures

data class Digipeater(
    val address: Address,
    val heard: Boolean = false
) {
    override fun toString() = "${address}${if (heard) "*" else ""}"
}
