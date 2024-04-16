/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hollowengine.client.screen.scripting

import kotlinx.serialization.Serializable
import java.util.regex.Pattern

@Serializable
data class SyntaxStyle(
    var title: String = "Monokai",
    var shadow: Boolean = true,
    var primary: Int = 0xf92472,
    var secondary: Int = 0x1da6ff,
    var methods: Int = 0xffe599,
    var properties: Int = 0x67d8ef,
    var special: Int = 0x6aa84f,
    var strings: Int = 0xe7db74,
    var comments: Int = 0x74705d,
    var numbers: Int = 0xac80ff,
    var other: Int = 0x6aa84f,
    var lineNumbers: Int = 0x90918b,
    var background: Int = 0x282923
)

open class SyntaxHighlighter(
    val style: SyntaxStyle = SyntaxStyle()
) {
    val operators = HashSet<String>()
    val primaryKeywords = HashSet<String>()
    val secondaryKeywords = HashSet<String>()
    val special = HashSet<String>()
    val typeKeywords = HashSet<String>()
    var functionName = Pattern.compile("function")
}

object KotlinSyntax : SyntaxHighlighter() {
    init {
        operators.addAll(arrayOf("+", "-", "=", "/", "*", "<", ">", "~", "&", "|", "!", "..", "->"))
        primaryKeywords.addAll(
            arrayOf(
                "break", "continue", "switch", "case", "try",
                "catch", "delete", "do", "while", "else", "finally", "if",
                "else", "for", "is", "as", "in", "instanceof",
                "new", "throw", "typeof", "with", "yield", "when", "return",
                "by", "constructor", "delegate", "field", "get", "set", "init", "value",
                "where", "actual", "annotation", "companion", "field", "external",
                "infix", "inline", "inner", "internal",
                "open", "operator", "out", "override", "suspend", "vararg",
                "file"
            )
        )
        secondaryKeywords.addAll(
            arrayOf(
                "abstract", "extends", "final", "implements", "interface", "super", "throws",
                "data", "class", "fun", "var", "val", "import"
            )
        )
        special.addAll(arrayOf("this", "it"))
        typeKeywords.addAll(arrayOf("true", "false", "null", "undefined", "enum"))
        functionName = Pattern.compile("[\\w_][\\d\\w_]*", Pattern.CASE_INSENSITIVE)
    }
}