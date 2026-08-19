package dev.gaphunter.httpstatusinlinecompanion.psi

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiBinaryExpression
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiExpression
import com.intellij.psi.PsiExpressionList
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.PsiNameValuePair
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.util.PsiTreeUtil
import dev.gaphunter.httpstatusinlinecompanion.detect.HttpSignalNames
import dev.gaphunter.httpstatusinlinecompanion.model.HttpStatusRegistry

/**
 * Finds Java integer literals that are both a real HTTP status code
 * AND sit in a context with reasonable signal (see
 * [HttpSignalNames] / this class's own [hasSignal] for the exact
 * rules) -- deliberately name-based rather than resolved-symbol-based
 * (no `PsiMethod.resolve()` / type-checking against
 * `javax.servlet.HttpServletResponse` or similar). Two real reasons,
 * not just convenience:
 *
 * 1. **Resolution is expensive and this runs on every daemon pass.**
 *    A highlighting pass that resolves every method call it walks past
 *    is a well-known source of editor lag on large files -- this
 *    catalog already avoids that class of bug elsewhere (see
 *    `CONSTITUTION.md` §6 "cómputo pesado siempre fuera del EDT"; this
 *    goes further and keeps the *whole* scan cheap enough that moving
 *    it off the EDT was never even a question).
 * 2. **The same method-name convention repeats across frameworks and
 *    hand-rolled code that resolution wouldn't unify anyway** --
 *    `response.setStatus(404)` looks identical across the Servlet API,
 *    a hand-rolled test double, or a framework this plugin has never
 *    heard of. Name matching generalizes better here than a fixed list
 *    of resolved framework types would.
 */
object JavaHttpStatusFinder {

    fun findAll(file: com.intellij.psi.PsiFile): List<HttpStatusLiteralHit> {
        val hits = mutableListOf<HttpStatusLiteralHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                super.visitElement(element)
                if (element is PsiLiteralExpression) {
                    hitFor(element)?.let { hits += it }
                }
            }
        })
        return hits
    }

    private fun hitFor(literal: PsiLiteralExpression): HttpStatusLiteralHit? {
        val code = (literal.value as? Int) ?: return null
        val officialName = HttpStatusRegistry.nameFor(code) ?: return null
        if (!hasSignal(literal)) return null
        return HttpStatusLiteralHit(code, officialName, literal.textRange.endOffset)
    }

    private fun hasSignal(literal: PsiLiteralExpression): Boolean {
        return isArgumentOfSignalMethodCall(literal) ||
            isArgumentOfSignalAnnotation(literal) ||
            isComparedAgainstSignalName(literal)
    }

    /** `response.setStatus(404)` / `HttpStatus.valueOf(404)` -- literal is (one of) the call's arguments. */
    private fun isArgumentOfSignalMethodCall(literal: PsiLiteralExpression): Boolean {
        val argList = literal.parent as? PsiExpressionList ?: return false
        val call = argList.parent as? PsiMethodCallExpression ?: return false
        val methodName = call.methodExpression.referenceName ?: return false
        return HttpSignalNames.isSignalMethodName(methodName)
    }

    /**
     * `@ResponseStatus(404)` (single-value shorthand) or
     * `@ResponseStatus(value = 404)` / `@ApiResponse(code = 404, ...)`
     * (named attribute) -- both are real, common Java annotation
     * shapes.
     */
    private fun isArgumentOfSignalAnnotation(literal: PsiLiteralExpression): Boolean {
        val nameValuePair = literal.parent as? PsiNameValuePair
        val annotation = when {
            nameValuePair != null -> PsiTreeUtil.getParentOfType(nameValuePair, PsiAnnotation::class.java)
            else -> PsiTreeUtil.getParentOfType(literal, PsiAnnotation::class.java, true, PsiExpressionList::class.java)
        } ?: return false

        val annotationName = annotation.nameReferenceElement?.referenceName ?: return false
        return HttpSignalNames.isSignalAnnotationName(annotationName)
    }

    /**
     * `statusCode == 404` / `404 != response.getStatus()` -- literal is
     * one side of a binary comparison and the *other* side's identifier
     * looks like it holds a status (see
     * [HttpSignalNames.looksLikeStatusHolderName]).
     */
    private fun isComparedAgainstSignalName(literal: PsiLiteralExpression): Boolean {
        val binary = literal.parent as? PsiBinaryExpression ?: return false
        val operator = binary.operationSign.text
        if (operator !in COMPARISON_OPERATORS) return false

        val otherSide = if (binary.lOperand == literal) binary.rOperand else binary.lOperand
        val identifierName = otherSide?.let { extractIdentifierName(it) } ?: return false
        return HttpSignalNames.looksLikeStatusHolderName(identifierName)
    }

    /** Reads the trailing simple name off a (possibly qualified) reference: `response.getStatus()` -> "getStatus", `statusCode` -> "statusCode". */
    private fun extractIdentifierName(expression: PsiExpression): String? {
        val ref = when (expression) {
            is PsiMethodCallExpression -> expression.methodExpression
            is PsiReferenceExpression -> expression
            else -> return null
        }
        return ref.referenceName
    }

    private val COMPARISON_OPERATORS = setOf("==", "!=", "<", "<=", ">", ">=")
}
