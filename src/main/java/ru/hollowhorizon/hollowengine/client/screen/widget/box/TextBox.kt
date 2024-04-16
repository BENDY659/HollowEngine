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

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.network.chat.Component
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.common.ui.Anchor

class TextBox(
    x: Int, y: Int, width: Int, height: Int,
    var text: Component = "".mcText,
    var color: Int = 0xFFFFFF,
    val shade: Boolean = true,
    val anchor: Anchor = Anchor.CENTER
) : HollowWidget(x, y, width, height, "".mcText) {
    override fun renderButton(stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        super.renderButton(stack, mouseX, mouseY, ticks)
        val x =
            x + when (anchor) {
                Anchor.START -> 0
                Anchor.CENTER -> width / 2 - font.width(text) / 2
                else -> width - font.width(
                    text
                )
            }
        val y = y + height / 2 - font.lineHeight / 2

        stack.pushPose()
        stack.translate(0.0, 0.0, 100.0)
        if (shade) {
            font.drawShadow(stack, text, x.toFloat() + 1, y.toFloat() + 1, color)
        } else {
            font.draw(stack, text, x.toFloat(), y.toFloat(), color)
        }
        stack.popPose()
    }
}