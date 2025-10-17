package utils

fun simpleChunk(text: String, maxChars: Int = 2000): List<String> {
    val parts = mutableListOf<String>()
    var i = 0
    while (i < text.length) {
        val end = (i + maxChars).coerceAtMost(text.length)
        parts += text.substring(i, end)
        i = end
    }
    return parts
}