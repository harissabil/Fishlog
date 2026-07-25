package com.harissabil.fisch.core.common.util

/**
 * True for an empty string, a plain integer, or a decimal with at most one '.' (e.g. "12", "12.", "12.5").
 * Used to gate text field input so users can only type valid weight/length values.
 */
fun String.isValidDecimal(): Boolean {
    if (isEmpty()) return true
    return matches(Regex("^\\d*\\.?\\d*$"))
}
