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

package ru.hollowhorizon.hollowengine.client.screen.widget.dialogue

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget
import ru.hollowhorizon.hc.client.utils.GuiAnimator
import ru.hollowhorizon.hc.client.utils.ScissorUtil
import ru.hollowhorizon.hc.client.utils.drawScaled
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.common.ui.Anchor

class DialogueTextBox(x: Int, y: Int, width: Int, height: Int) : HollowWidget(x, y, width, height, "".mcText) {
    var animator: GuiAnimator? = null
    var text: Component = "".mcText
        set(value) {
            field = value
            currentLine = 0
            animator?.reset()
        }
    private var currentLine = 0
    private var linesCount = 0
    var complete: Boolean
        get() = currentLine >= linesCount
        set(v) {
            if (v) currentLine = 99
            else {
                currentLine = 0
                animator?.reset()
            }
        }
    val scale = 1.3f

    override fun init() {
        animator = GuiAnimator.Single(0, width, 40) { f -> f }
    }

    override fun renderButton(stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        super.renderButton(stack, mouseX, mouseY, ticks)

        val lines = font.split(text, (width / scale).toInt())
        linesCount = lines.size

        animator?.update(ticks)

        val fontHeight = (font.lineHeight * scale).toInt()
        stack.pushPose()
        stack.translate(0.0, 0.0, 500.0)
        lines.forEachIndexed { i, line ->
            val lineWidth = if (i < currentLine) width
            else if (i == currentLine) animator?.value?.toInt() ?: 0
            else 0

            if (this.y + i * fontHeight + fontHeight > Minecraft.getInstance().window.guiScaledHeight) return@forEachIndexed
            ScissorUtil.push(this.x, this.y + i * fontHeight, lineWidth, fontHeight)
            font.drawScaled(stack, Anchor.START, line, this.x, (this.y + i * fontHeight), 0xFFFFFF, scale)
            ScissorUtil.pop()
        }
        stack.popPose()

        if (animator?.isFinished() == true && currentLine < linesCount) {
            animator?.reset()
            currentLine++
        }
    }
}
