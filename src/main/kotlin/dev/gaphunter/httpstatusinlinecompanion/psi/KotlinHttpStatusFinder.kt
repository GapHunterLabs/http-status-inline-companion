package dev.gaphunter.httpstatusinlinecompanion.psi

import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import dev.gaphunter.httpstatusinlinecompanion.detect.HttpSignalNames
import dev.gaphunter.httpstatusinlinecompanion.model.HttpStatusRegistry
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtSimpleNameExpression
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.KtValueArgument

/**
 * Kotlin counterpart of [JavaHttpStatusFinder]. Same name-based (not
 * resolved-symbol-based) signal contract, same reasons -- see that
 * class's doc comment. Kotlin adds one real shape Java doesn't have as
 * cleanly: named arguments (`call(status = 404)`), checked directly
 * via [KtValueArgument.getArgumentName] rather than needing a separate
 * annotation-attribute code path like Java's [com.intellij.psi.PsiNameValuePair].
 */
object KotlinHttpStatusFinder {

    fun findAll(file: PsiFile): List<HttpStatusLiteralHit> {
        val hits = mutableListOf<HttpStatusLiteralHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitConstantExpression(expression: KtConstantExpression) {
                super.visitConstantExpression(expression)
                hitFor(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitFor(expression: KtConstantExpression): HttpStatusLiteralHit? {
        // Only plain integer constants (KtNodeTypes.INTEGER_CONSTANT under the hood) --
        // text().toIntOrNull() already rejects float/boolean/char/string constants cleanly,
        // no separate elementType check needed.
        val code = expression.text.toIntOrNull() ?: return null
        val officialName = HttpStatusRegistry.nameFor(code) ?: return null
        if (!hasSignal(expression)) return null
        return HttpStatusLiteralHit(code, officialName, expression.textRange.endOffset)
    }

    private fun hasSignal(literal: KtConstantExpression): Boolean {
        return isArgumentOfSignalCall(literal) ||
            isArgumentOfSignalAnnotation(literal) ||
            isComparedAgainstSignalName(literal)
    }

    /**
     * `response.status(404)` (signal from the callee's own name) OR a
     * named argument like `respondWith(status = 503)` (signal from the
     * *argument's* name instead -- real Kotlin shape a Java-only check
     * on the callee name alone would miss, since the callee itself,
     * e.g. `respondWith`, need not look HTTP-related at all).
     */
    private fun isArgumentOfSignalCall(literal: KtConstantExpression): Boolean {
        val valueArgument = literal.parent as? KtValueArgument ?: return false

        val argumentName = valueArgument.getArgumentName()?.asName?.asString()
        if (argumentName != null && HttpSignalNames.looksLikeStatusHolderName(argumentName)) return true

        val call = PsiTreeUtil.getParentOfType(valueArgument, KtCallExpression::class.java) ?: return false
        val calleeName = (call.calleeExpression as? KtSimpleNameExpression)?.getReferencedName() ?: return false
        return HttpSignalNames.isSignalMethodName(calleeName)
    }

    /** `@ResponseStatus(404)` / `@ResponseStatus(value = 404)`. */
    private fun isArgumentOfSignalAnnotation(literal: KtConstantExpression): Boolean {
        val annotation = PsiTreeUtil.getParentOfType(literal, KtAnnotationEntry::class.java) ?: return false
        val annotationName = annotation.shortName?.asString() ?: return false
        return HttpSignalNames.isSignalAnnotationName(annotationName)
    }

    /** `statusCode == 404` / `404 != response.status`. */
    private fun isComparedAgainstSignalName(literal: KtConstantExpression): Boolean {
        val binary = literal.parent as? KtBinaryExpression ?: return false
        val operator = binary.operationToken
        if (operator !in COMPARISON_TOKENS) return false

        val otherSide: KtExpression = if (binary.left == literal) binary.right ?: return false else binary.left ?: return false
        val identifierName = extractIdentifierName(otherSide) ?: return false
        return HttpSignalNames.looksLikeStatusHolderName(identifierName)
    }

    /** Reads the trailing simple name off a (possibly qualified) reference: `response.status` -> "status", `statusCode` -> "statusCode". */
    private fun extractIdentifierName(expression: KtExpression): String? {
        return when (expression) {
            is KtDotQualifiedExpression -> (expression.selectorExpression as? KtNameReferenceExpression)?.getReferencedName()
                ?: (expression.selectorExpression as? KtCallExpression)?.calleeExpression?.text
            is KtNameReferenceExpression -> expression.getReferencedName()
            is KtCallExpression -> expression.calleeExpression?.text
            else -> null
        }
    }

    private val COMPARISON_TOKENS = setOf(
        KtTokens.EQEQ,
        KtTokens.EXCLEQ,
        KtTokens.LT,
        KtTokens.LTEQ,
        KtTokens.GT,
        KtTokens.GTEQ,
    )
}
