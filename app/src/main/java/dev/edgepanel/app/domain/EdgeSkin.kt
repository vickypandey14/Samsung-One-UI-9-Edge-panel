package dev.edgepanel.app.domain

enum class EdgeSkin {
    Classic,
    Bubblegum,
    Midnight,
    Terminal,
    Party,
    ;

    companion object {
        fun fromStored(raw: String?): EdgeSkin {
            return entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: Classic
        }
    }
}
