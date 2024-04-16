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

package ru.hollowhorizon.hollowengine.client.screen.widget.box

import ru.hollowhorizon.hc.client.screens.widget.HollowWidget
import ru.hollowhorizon.hc.client.screens.widget.layout.box
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.mcTranslate
import ru.hollowhorizon.hc.common.ui.Alignment
import ru.hollowhorizon.hollowengine.client.screen.widget.TextFieldChoicerWidget

class TypeBox(
    x: Int, y: Int, width: Int, height: Int,
    val text: String = "hollowengine.input_model_path", // translatable
    val textFieldText: String = "hollowengine.model_example", // translatable
    val onValueChange: (TypeBox) -> Unit = {},
) : HollowWidget(x, y, width, height, "".mcText) {
    var currentText: String = ""

    override fun init() {
        super.init()
        box {
            box {
                align = Alignment.TOP_CENTER
                size = 100.pc x 30.pc
                elements {
                    +TextBox(0, 0, 100.pc.w().value, 100.pc.h().value, this@TypeBox.text.mcTranslate)
                }
            }

            box {
                align = Alignment.BOTTOM_CENTER
                size = 90.pc x 50.pc
                elements {
                    +TextFieldChoicerWidget(
                        0, 0, 100.pc.w().value, 100.pc.h().value, this@TypeBox.textFieldText
                    ) { text ->
                        this@TypeBox.currentText = text
                        onValueChange(this@TypeBox)
                    }

                }
            }
        }
    }
}