package ru.hollowhorizon.hollowengine.client.screen.widget

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import ru.hollowhorizon.hc.client.screens.widget.button.BaseButton
import ru.hollowhorizon.hc.client.utils.GuiAnimator
import ru.hollowhorizon.hc.client.utils.math.Interpolation
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.rl

class ScaleableButton(
    x: Int,
    y: Int,
    w: Int,
    h: Int,
    val image: String,
    val tooltipText: String = "",
    onPress: BaseButton.() -> Unit = {}
) :
    BaseButton(x, y, w, h, "".mcText, onPress, image.rl) {
    var lastHovered = false
    var animation = GuiAnimator.Single(0, 0, 10, Interpolation.SINE_IN::invoke)
    override fun render(stack: PoseStack, x: Int, y: Int, f: Float) {
        val minecraft = Minecraft.getInstance()
        val isHovered = isCursorAtButton(x, y)

        if (lastHovered != isHovered) {
            animation = if (isHovered) GuiAnimator.Single(0, 10, 5, Interpolation.SINE_IN::invoke)
            else GuiAnimator.Single(10, 0, 5, Interpolation.SINE_IN::invoke)
        }

        animation.update(f)
        val progress = animation.value / 10f

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()

        stack.pushPose()

        stack.translate(this.x.toDouble(), this.y.toDouble(), 0.0)
        val scale = 1f + 0.6f * progress
        stack.scale(scale, scale, scale)
        val tX = (scale * width - width) / 4.0
        val tY = (scale * height - height) / 4.0
        stack.translate(-tX, -tY, 70.0)

        val color = 0.6f + 0.4f * progress
        RenderSystem.setShaderColor(color, color, color, color)
        RenderSystem.setShaderTexture(0, image.rl)
        blit(stack, 0, 0, 0f, 0f, width, height, width, height)

        stack.popPose()

        if (minecraft.screen != null && isHovered && tooltipText != "") {
            minecraft.screen!!.renderTooltip(
                stack,
                minecraft.font.split(tooltipText.mcText, (width / 2 - 43).coerceAtLeast(170)),
                x,
                y
            )
        }

        lastHovered = isHovered
    }
}
