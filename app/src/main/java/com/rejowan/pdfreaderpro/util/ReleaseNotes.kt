package com.rejowan.pdfreaderpro.util

/**
 * Turns a GitHub release body into blocks the update sheet can draw.
 *
 * A release body is Markdown and carries more than the user needs inside the
 * app: download tables, install steps, checksums and the list of merged pull
 * requests. Only the "What's New" section is kept, and only the Markdown that
 * release notes actually use is understood: headings, bullet lists, bold,
 * inline code and links. Anything else is shown as plain text.
 */
object ReleaseNotes {

    sealed interface Block {
        data class Heading(val level: Int, val text: List<Span>) : Block
        data class Bullet(val depth: Int, val text: List<Span>) : Block
        data class Paragraph(val text: List<Span>) : Block
    }

    sealed interface Span {
        data class Plain(val text: String) : Span
        data class Bold(val text: String) : Span
        data class Code(val text: String) : Span
        data class Link(val text: String, val url: String) : Span
    }

    private val WHATS_NEW = Regex("""^##\s+What['’]s New\s*$""", RegexOption.IGNORE_CASE)
    private val SECTION = Regex("""^##\s+\S""")
    private val HEADING = Regex("""^(#{1,6})\s+(.*?)\s*#*\s*$""")
    private val BULLET = Regex("""^(\s*)[-*+]\s+(.*)$""")
    private val RULE = Regex("""^\s*([-*_])(\s*\1){2,}\s*$""")
    private val INLINE = Regex("""\*\*(.+?)\*\*|__(.+?)__|`([^`]+)`|\[([^\]]+)]\(([^)\s]+)\)""")

    /** The "What's New" section of [body], or the whole body when it has none. */
    fun whatsNew(body: String): String {
        val lines = body.replace("\r\n", "\n").lines()
        val start = lines.indexOfFirst { WHATS_NEW.matches(it.trim()) }
        if (start < 0) return body.trim()
        val end = (start + 1 until lines.size)
            .firstOrNull { SECTION.containsMatchIn(lines[it]) }
            ?: lines.size
        return lines.subList(start + 1, end).joinToString("\n").trim()
    }

    fun parse(markdown: String): List<Block> {
        val blocks = mutableListOf<Block>()
        val paragraph = StringBuilder()

        fun flushParagraph() {
            if (paragraph.isNotBlank()) blocks += Block.Paragraph(inline(paragraph.toString().trim()))
            paragraph.clear()
        }

        for (line in markdown.replace("\r\n", "\n").lines()) {
            val heading = HEADING.matchEntire(line.trim())
            val bullet = BULLET.matchEntire(line)
            when {
                line.isBlank() || RULE.matches(line) -> flushParagraph()
                heading != null -> {
                    flushParagraph()
                    blocks += Block.Heading(heading.groupValues[1].length, inline(heading.groupValues[2]))
                }
                bullet != null -> {
                    flushParagraph()
                    val indent = bullet.groupValues[1].replace("\t", "  ").length
                    blocks += Block.Bullet(depth = indent / 2, text = inline(bullet.groupValues[2]))
                }
                else -> {
                    if (paragraph.isNotEmpty()) paragraph.append(' ')
                    paragraph.append(line.trim())
                }
            }
        }
        flushParagraph()
        return blocks
    }

    internal fun inline(text: String): List<Span> {
        val spans = mutableListOf<Span>()
        var last = 0
        for (match in INLINE.findAll(text)) {
            if (match.range.first > last) spans += Span.Plain(text.substring(last, match.range.first))
            val (bold, boldAlt, code, linkText, url) = match.destructured
            spans += when {
                bold.isNotEmpty() -> Span.Bold(bold)
                boldAlt.isNotEmpty() -> Span.Bold(boldAlt)
                code.isNotEmpty() -> Span.Code(code)
                else -> Span.Link(linkText, url)
            }
            last = match.range.last + 1
        }
        if (last < text.length) spans += Span.Plain(text.substring(last))
        return spans
    }
}
