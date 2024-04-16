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

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.common.ui.Anchor
import ru.hollowhorizon.hc.client.utils.drawScaled
import ru.hollowhorizon.hc.client.utils.mcText
import java.util.regex.Pattern

val PATTERN: Pattern = Pattern.compile("-?(\\d+)?(\\.)?(\\d+)?")

class FloatTextField(float: Float, width: Int, height: Int, texture: ResourceLocation, responder: (Float) -> Unit) :
    HollowTextFieldWidget(
        Minecraft.getInstance().font, 0, 0, width, height, float.toString().mcText, texture
    ) {
    val font: Font = Minecraft.getInstance().font
    var modifier = 1.0f

    init {
        setResponder {
            val value = if (it.isEmpty()) 0f else java.lang.Float.parseFloat(it)
            responder(value)
        }
        setFilter { text -> PATTERN.matcher(text).matches() || text.isEmpty() }
    }

    override fun render(stack: PoseStack, mouseX: Int, mouseY: Int, p_230430_4_: Float) {
        super.render(stack, mouseX, mouseY, p_230430_4_)
        val color1 = if (mouseX in x - 5..x && mouseY in y..y + height) 0xFFFFFF else 0x888888
        val color2 =
            if (mouseX in x + getWidth() - 10..x + getWidth() - 5 && mouseY in y..y + height) 0xFFFFFF else 0x888888
        font.drawScaled(stack, Anchor.START, "<".mcText, x - 5, y, color1, 0.75f)
        font.drawScaled(stack, Anchor.END, ">".mcText, x + getWidth() - 5, y, color2, 0.75f)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, pButton: Int): Boolean {
        val keyModifier = when {
            Screen.hasShiftDown() && Screen.hasControlDown() -> 0.01f
            Screen.hasShiftDown() -> 0.1f
            Screen.hasControlDown() -> 5f
            else -> 1f
        }

        if (mouseX in x - 5.0..x.toDouble() && mouseY in y.toDouble()..y + height.toDouble()) {
            float -= modifier * keyModifier
        }
        if (mouseX in x.toDouble() + getWidth() - 10..x.toDouble() + getWidth() - 5 && mouseY in y.toDouble()..y + height.toDouble()) {
            float += modifier * keyModifier
        }
        return super.mouseClicked(mouseX, mouseY, pButton)
    }

    var float
        get() = java.lang.Float.parseFloat(value)
        set(value) {
            val rounded = Math.round(value * 1000f) / 1000f
            this.value = rounded.toString()
        }

    override fun mouseScrolled(pMouseX: Double, pMouseY: Double, pDelta: Double): Boolean {
        if (pMouseX in x.toDouble()..x.toDouble() + width.toDouble() && pMouseY in y.toDouble()..y + height.toDouble()) float += modifier * pDelta.toFloat()
        return super.mouseScrolled(pMouseX, pMouseY, pDelta)
    }

    override fun getValue(): String {
        val value = super.getValue()
        return value.ifEmpty { "0" }
    }
}