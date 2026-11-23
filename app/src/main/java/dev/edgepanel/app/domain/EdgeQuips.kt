package dev.edgepanel.app.domain

object EdgeQuips {
    private val lines = listOf(
        "The edge remembers this.",
        "You found the side of the screen.",
        "Still no cloud. Just this panel.",
        "Boop accepted.",
        "The handle is doing its best.",
        "One UI brought you. The edge kept you.",
        "This panel is slightly more dramatic today.",
        "Secret level: the bezel.",
    )

    fun line(boops: Int): String {
        val index = boops.mod(lines.size)
        return lines[index]
    }
}
