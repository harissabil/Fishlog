package com.harissabil.fisch.feature.logbook.catches

enum class ReleaseFilter(val value: String) {
    ALL("All"),
    RELEASED("Released"),
    KEPT("Kept"),
}

data class FilterState(
    val releaseFilter: ReleaseFilter = ReleaseFilter.ALL,
    val selectedBaits: Set<String> = emptySet(),
) {
    val isActive: Boolean
        get() = releaseFilter != ReleaseFilter.ALL || selectedBaits.isNotEmpty()
}
