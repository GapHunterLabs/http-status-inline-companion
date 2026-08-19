package dev.gaphunter.httpstatusinlinecompanion.highlight

import com.intellij.codeHighlighting.TextEditorHighlightingPass
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import dev.gaphunter.httpstatusinlinecompanion.psi.HttpStatusLiteralHit
import dev.gaphunter.httpstatusinlinecompanion.psi.JavaHttpStatusFinder
import dev.gaphunter.httpstatusinlinecompanion.psi.KotlinHttpStatusFinder
import dev.gaphunter.httpstatusinlinecompanion.render.HttpStatusInlayManager

/**
 * Scans [file]'s PSI for numeric literals that are both a real HTTP
 * status code and sit in a signal-bearing context, and turns each one
 * into an inline inlay showing the official name (`-> Not Found`)
 * right after the literal. Same off-EDT/on-EDT split contract as
 * Regex Named Group Companion's `NamedGroupHighlightingPass`:
 * [doCollectInformation] runs off the EDT and only reads immutable
 * PSI; [doApplyInformationToEditor] runs on the EDT and is the only
 * place that touches [Editor]/[com.intellij.openapi.editor.InlayModel].
 */
class HttpStatusHighlightingPass(
    project: Project,
    private val editor: Editor,
    private val file: PsiFile,
) : TextEditorHighlightingPass(project, editor.document, false) {

    private var hits: List<HttpStatusLiteralHit> = emptyList()

    override fun doCollectInformation(progress: ProgressIndicator) {
        hits = when (file.language.id) {
            "JAVA" -> JavaHttpStatusFinder.findAll(file)
            "kotlin" -> KotlinHttpStatusFinder.findAll(file)
            else -> emptyList()
        }
    }

    override fun doApplyInformationToEditor() {
        val entries = hits.map { hit -> hit.literalEndOffset to " -> ${hit.officialName}" }
        HttpStatusInlayManager.replaceInlays(editor, entries)
    }
}
