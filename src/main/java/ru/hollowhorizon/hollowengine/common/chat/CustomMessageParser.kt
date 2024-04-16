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

package ru.hollowhorizon.hollowengine.common.chat

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.minecraft.client.GuiMessage
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import org.jetbrains.kotlin.daemon.common.trimQuotes

fun main() {
    val buttons = CustomMessageParser.parse(
        FormattedCharSequence.forward(
            "\${[{\"text\":\"Привет всем))\",\"texture\":\"hollowengine:textures/gui/dialogues/choice_button.png\"}]}",
            Style.EMPTY
        )
    )

    println(buttons.contentToString())
}

object CustomMessageParser {
    fun parse(mcText: FormattedCharSequence): Array<Button> {
        var text = mcText.asString().trim()
        text = text
            .substring(2, text.length - 1)

        return Json.decodeFromString(text)
    }
}

@Serializable
class Button(val text: String, val texture: String) {
    companion object {
        fun fromString(text: String): Button {
            if (!text.startsWith("button")) throw IllegalStateException("Not a button!")
            val args = text.substring(6).split(",")
            return Button(args[0].trimQuotes(), args[1].trimQuotes())
        }
    }

    override fun toString(): String {
        return "{\"text\":\"$text\",\"texture\":\"$texture\"}"
    }
}

fun FormattedCharSequence.asString() =
    StringBuilder().apply { accept { _, _, char -> appendCodePoint(char); true } }.toString()

val GuiMessage.Line.isCustom get() = content.asString().matches(Regex("\\$\\{.*}"))