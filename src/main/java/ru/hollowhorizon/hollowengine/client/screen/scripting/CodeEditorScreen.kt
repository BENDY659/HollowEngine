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

import net.minecraft.client.gui.components.MultiLineEditBox
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.screens.widget.button.BaseButton
import ru.hollowhorizon.hc.client.screens.widget.layout.PlacementType
import ru.hollowhorizon.hc.client.screens.widget.layout.box
import ru.hollowhorizon.hc.client.utils.mcTranslate
import ru.hollowhorizon.hc.client.utils.rl

class CodeEditorScreen : HollowScreen() {
    private var editBox: MultiLineEditBox? = null

    override fun init() {
        super.init()

        box {
            placementType = PlacementType.GRID

            elements {
                editBox = +HighlightEditBox(90.pc.w().value, 80.pc.h().value)

                +BaseButton(
                    0, 0, 43.pc.w().value, 20,
                    "hollowengine.save".mcTranslate,
                    { onClose() },
                    "hollowengine:textures/gui/long_button.png".rl
                )
                +BaseButton(
                    0, 0, 43.pc.w().value, 20,
                    "hollowengine.cancel".mcTranslate,
                    { onClose() },
                    "hollowengine:textures/gui/long_button.png".rl
                )
            }
        }

    }

    override fun tick() {
        super.tick()
        editBox?.tick()
    }

    override fun isPauseScreen() = false
}