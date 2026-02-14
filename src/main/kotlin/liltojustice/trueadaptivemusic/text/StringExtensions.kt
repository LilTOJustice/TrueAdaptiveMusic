package liltojustice.trueadaptivemusic.text

fun String.prettify(): String {
    return if (contains(":")) {
        val split = split(":")
        "${split[0].prettify()} - ${split[1].prettify()}"
    }
    else if (contains("_")) {
        prettifyConst()
    }
    else if (firstOrNull()?.isLowerCase() == true) {
        prettifyCamel()
    }
    else {
        prettifyUpper()
    }
}

fun String.prettifyConst(): String {
    return split("_")
        .joinToString(" ", transform = { it.lowercase().replaceFirstChar { char -> char.uppercase() } })
}

fun String.prettifyCamel(): String {
    return split(StringExtensions.capitalRegex)
        .joinToString(" ", transform = { it.lowercase().replaceFirstChar { char -> char.uppercase() } })
}

fun String.prettifyUpper(): String {
    return lowercase().replaceFirstChar { it.uppercase() }
}

object StringExtensions {
    val capitalRegex = Regex("(?=[A-Z])")
}