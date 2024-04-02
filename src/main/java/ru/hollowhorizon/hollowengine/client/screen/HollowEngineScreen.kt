package ru.hollowhorizon.hollowengine.client.screen

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Vector3f
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.sounds.SoundSource
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.utils.GuiAnimator
import ru.hollowhorizon.hc.client.utils.drawScaled
import ru.hollowhorizon.hc.client.utils.math.Interpolation
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.effects.ParticleEmitterInfo
import ru.hollowhorizon.hc.common.effects.ParticleHelper
import ru.hollowhorizon.hc.common.ui.Anchor
import ru.hollowhorizon.hollowengine.HollowEngine
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.util.PostEffect


object HollowEngineScreen : HollowScreen() {
    val animation = GuiAnimator.Single(0, 1, 400, Interpolation.LINEAR::invoke)

    override fun init() {
        super.init()
        animation.reset()
    }

    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick)


        animation.update(pPartialTick)
        val alpha = ((animation.value * 255).toInt() + 10).coerceAtMost(255)

        if(alpha > 120) {
            pPoseStack.translate(-90.0 * (alpha - 120.0) / 60.0, 90.0 * (alpha - 120.0) / 60.0, 0.0)
            pPoseStack.mulPose(Vector3f.ZP.rotationDegrees(90f * (alpha - 120f) / 60f))
        }
        if(alpha > 170) {
            onClose()
            Minecraft.getInstance().gameRenderer.loadEffect(PostEffect.GRAY)
            ParticleHelper.addParticle(Minecraft.getInstance().level!!, ParticleEmitterInfo("hc:example/example".rl), true)
        }

        bind(HollowEngine.MODID, "gui/joke.png")
        blit(pPoseStack, 0, 0, 0f, 0f, width, height, width, height)


        font.drawScaled(
            pPoseStack, Anchor.CENTER, "C вторым апреля!".mcText, width / 2, height / 2,
            0xFFFFFF or (alpha shl 24), 4f, shadow = true
        )
        font.drawScaled(
            pPoseStack, Anchor.CENTER, "Обновления будут...".mcText, width / 2, height / 2 + 20,
            0xFFFFFF or (alpha shl 24), 1f, shadow = true
        )

        if (alpha == 120) {
            Minecraft.getInstance().soundManager.play(
                SimpleSoundInstance(
                    "hollowengine:metal_pipe".rl,
                    SoundSource.MASTER,
                    1f,
                    1f,
                    SoundInstance.createUnseededRandom(),
                    false,
                    0,
                    SoundInstance.Attenuation.NONE,
                    0.0,
                    0.0,
                    0.0,
                    true
                )
            )
        }

    }
}