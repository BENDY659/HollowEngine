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
import net.minecraft.network.chat.Component
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget
import ru.hollowhorizon.hc.client.utils.GuiAnimator
import ru.hollowhorizon.hc.client.utils.ScissorUtil
import ru.hollowhorizon.hc.client.utils.math.Interpolation
import ru.hollowhorizon.hc.client.utils.mcText

class FadeInLabelWidget(x: Int, y: Int, width: Int, height: Int) : HollowWidget(x, y, width, height, "".mcText) {
    private var text = "".mcText
    private var onFadeInComplete: Runnable? = null
    private var animator: GuiAnimator? = null
    private var isUpdated = false
    override fun init() {
        super.init()
        animator = GuiAnimator.Single(0, width, 20, Interpolation.EXPO_OUT::invoke)
    }

    fun setText(text: Component) {
        this.text = text.copy()
    }

    fun setText(text: String) {
        setText(text.mcText)
    }

    fun reset() {
        animator?.reset()
    }

    fun onFadeInComplete(onFadeInComplete: Runnable) {
        this.onFadeInComplete = onFadeInComplete
    }

    override fun renderButton(stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        super.renderButton(stack, mouseX, mouseY, ticks)
        ScissorUtil.push(x, y, animator!!.value.toInt(), height)
        stack.pushPose()
        stack.translate(0.0, 0.0, 500.0)
        font.drawShadow(stack, text, x.toFloat(), y + height / 2f - font.lineHeight / 2f, 0xFFFFFF)
        stack.popPose()
        ScissorUtil.pop()
    }

    override fun tick() {
        if (animator?.isFinished() == true && !isUpdated) {
            onFadeInComplete?.run()
            isUpdated = true
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return false
    }

    val isComplete: Boolean
        get() = animator!!.isFinished()

    fun complete() {
        animator!!.value = animator!!.end.toFloat()
    }

    fun getText(): String {
        return text.string
    }
}
