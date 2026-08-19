package dev.gaphunter.httpstatusinlinecompanion.highlight

import com.intellij.codeHighlighting.Pass
import com.intellij.codeHighlighting.TextEditorHighlightingPass
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactory
import com.intellij.codeHighlighting.TextEditorHighlightingPassFactoryRegistrar
import com.intellij.codeHighlighting.TextEditorHighlightingPassRegistrar
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * Registers [HttpStatusHighlightingPass] to run after the IDE's own
 * `Pass.UPDATE_ALL` (general highlighting) pass -- same registration
 * shape as Regex Named Group Companion's `NamedGroupPassFactory` /
 * Error Lens Companion's `ErrorLensPassFactory`. v0.1 scope is Java and
 * Kotlin only (see README "v0.1 scope" for why Python/TypeScript are
 * deferred, not silently unsupported).
 */
class HttpStatusPassFactory : TextEditorHighlightingPassFactory, TextEditorHighlightingPassFactoryRegistrar {

    override fun registerHighlightingPassFactory(registrar: TextEditorHighlightingPassRegistrar, project: Project) {
        registrar.registerTextEditorHighlightingPass(
            this,
            null,
            intArrayOf(Pass.UPDATE_ALL),
            false,
            -1,
        )
    }

    override fun createHighlightingPass(file: PsiFile, editor: Editor): TextEditorHighlightingPass? {
        if (editor.isOneLineMode) return null
        if (!file.isPhysical) return null
        if (file.language.id != "JAVA" && file.language.id != "kotlin") return null
        return HttpStatusHighlightingPass(file.project, editor, file)
    }
}
