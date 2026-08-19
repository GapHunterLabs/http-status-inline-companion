package dev.gaphunter.httpstatusinlinecompanion.render

import com.intellij.openapi.editor.EditorCustomElementRenderer
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle

/**
 * Renders one HTTP Status Code Inline Companion inlay: `-> Not Found`
 * pinned immediately after the numeric literal it annotates. Unlike
 * Regex Named Group Companion's end-of-line inlay, this one sits
 * *inline* right after the literal -- deliberate, because a single
 * line routinely has more than one HTTP-status-shaped literal
 * (`if (statusCode >= 400 && statusCode != 404)`), so anchoring to the
 * literal itself (not the end of the line) is what keeps the hint
 * attached to the right number. Same deliberately minimal painting as
 * Error Lens Companion / Regex Named Group Companion's own renderers
 * (flat, semi-transparent foreground, editor's own italic font) -- this
 * catalog's proven, low-risk approach to the one part of an inlay that
 * unit tests can only confirm doesn't throw, never that it actually
 * looks right on screen.
 */
class HttpStatusInlayRenderer(private val text: String) : EditorCustomElementRenderer {

    companion object {
        private const val LEFT_PADDING_PX = 6
        private const val ALPHA = 140
    }

    override fun calcWidthInPixels(inlay: Inlay<*>): Int {
        val editor = inlay.editor
        val font = editor.colorsScheme.getFont(EditorFontType.ITALIC)
        val fontMetrics = editor.contentComponent.getFontMetrics(font)
        return LEFT_PADDING_PX + fontMetrics.stringWidth(text)
    }

    override fun paint(inlay: Inlay<*>, g: Graphics, targetRegion: Rectangle, textAttributes: TextAttributes) {
        val editor = inlay.editor
        val font = editor.colorsScheme.getFont(EditorFontType.ITALIC)
        val foreground = editor.colorsScheme.defaultForeground
        g.font = font
        g.color = Color(foreground.red, foreground.green, foreground.blue, ALPHA)
        val fontMetrics = g.getFontMetrics(font)
        val baseline = targetRegion.y + fontMetrics.ascent
        g.drawString(text, targetRegion.x + LEFT_PADDING_PX, baseline)
    }
}
