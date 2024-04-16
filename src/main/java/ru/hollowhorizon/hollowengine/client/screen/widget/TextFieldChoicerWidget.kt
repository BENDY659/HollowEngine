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

package ru.hollowhorizon.hollowengine.client.screen.widget

import ru.hollowhorizon.hc.client.screens.widget.HollowWidget
import ru.hollowhorizon.hc.client.screens.widget.layout.box
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.rl

class TextFieldChoicerWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val text: String,
    val consumer: (String) -> Unit,
) :
    HollowWidget(x, y, width, height, "".mcText) {
    var currentText: String = ""

    override fun init() {
        this.widgets.clear()

        box {
            size = 100.pc x 100.pc

            elements {
                val widget = +HollowTextFieldWidget(
                    font,
                    0,
                    0,
                    100.pc.w().value,
                    100.pc.h().value,
                    text.mcText,
                    "hollowengine:textures/gui/text_field.png".rl
                )
                widget.setResponder {
                    this@TextFieldChoicerWidget.currentText = it
                    consumer(it)
                }
            }
        }

    }

    fun search(query: String, search: String): Boolean {
        //for example query "st do" will return "stone_door"
        val queryWords = query.split("\\s+".toRegex())
        for (word in queryWords) {
            if (!search.contains(word)) return false
        }
        return true
    }
}