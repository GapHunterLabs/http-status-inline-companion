package dev.gaphunter.httpstatusinlinecompanion.render

import com.intellij.testFramework.fixtures.BasePlatformTestCase

/**
 * Exercises the real `InlayModel.addInlineElement` / `Disposer` calls --
 * same de-risking Regex Named Group Companion's
 * `NamedGroupInlayManagerTest` already established for this pattern
 * (adapted to inline, not after-line-end, elements since this plugin's
 * hints sit right after each literal, not at end of line). Does NOT
 * verify actual on-screen pixels (see README.md "Known limitations").
 */
class HttpStatusInlayManagerTest : BasePlatformTestCase() {

    fun testReplaceInlaysAddsOneInlayPerEntry() {
        myFixture.configureByText("Sample.txt", "line one\nline two\nline three\n")
        val editor = myFixture.editor
        val document = editor.document

        HttpStatusInlayManager.replaceInlays(
            editor,
            listOf(
                document.getLineEndOffset(0) to " -> Not Found",
                document.getLineEndOffset(1) to " -> Service Unavailable",
            ),
        )

        val inlays = editor.inlayModel.getInlineElementsInRange(0, document.textLength)
        assertEquals(2, inlays.size)
    }

    fun testReplaceInlaysDisposesPreviousInlaysInsteadOfAccumulating() {
        myFixture.configureByText("Sample.txt", "line one\nline two\n")
        val editor = myFixture.editor
        val document = editor.document

        HttpStatusInlayManager.replaceInlays(editor, listOf(document.getLineEndOffset(0) to " -> Not Found"))
        HttpStatusInlayManager.replaceInlays(editor, listOf(document.getLineEndOffset(0) to " -> Bad Request"))

        val inlays = editor.inlayModel.getInlineElementsInRange(0, document.textLength)
        assertEquals(1, inlays.size)
    }

    fun testReplaceInlaysWithEmptyListClearsExistingOnes() {
        myFixture.configureByText("Sample.txt", "line one\n")
        val editor = myFixture.editor
        val document = editor.document

        HttpStatusInlayManager.replaceInlays(editor, listOf(document.getLineEndOffset(0) to " -> Not Found"))
        HttpStatusInlayManager.replaceInlays(editor, emptyList())

        val inlays = editor.inlayModel.getInlineElementsInRange(0, document.textLength)
        assertEquals(0, inlays.size)
    }
}
