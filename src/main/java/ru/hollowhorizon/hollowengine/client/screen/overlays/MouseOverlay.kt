package ru.hollowhorizon.hollowengine.client.screen.overlays

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.EntityHitResult
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.use
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity
import ru.hollowhorizon.hollowengine.common.network.MouseButton
import ru.hollowhorizon.hollowengine.common.npcs.NPCCapability

object MouseOverlay {
    const val ICONS = "hollowengine:textures/gui/icons"
    var texture = "$ICONS/mouse_right.png".rl
    var enable = false
        set(value) {
            startTime = currentTime
            field = value
        }
    private val currentTime: Int
        get() = (Minecraft.getInstance().level?.gameTime ?: 0).toInt()
    private var startTime = 0

    fun draw(stack: PoseStack, x: Int, y: Int, partialTick: Float) {
        val npc = (Minecraft.getInstance().hitResult as? EntityHitResult)?.entity as? NPCEntity ?: return

        var progress = Mth.clamp((currentTime - startTime + partialTick) / 20f, 0f, 1f)
        if (!enable) progress = 1f - progress

        val button = npc[NPCCapability::class].mouseButton

        if (button != MouseButton.NONE) {
            if(!enable) enable = true
            texture = "$ICONS/mouse_$button.png".rl
        }


        if (progress > 0f) {
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()

            RenderSystem.setShaderTexture(0, texture)
            RenderSystem.setShaderColor(
                1.0f,
                1.0f,
                1.0f,
                progress
            )

            stack.use {
                Screen.blit(stack, x - 6, y - 6, 0f, 0f, 12, 12, 12, 12)
            }

            RenderSystem.setShaderColor(
                1.0f,
                1.0f,
                1.0f,
                1.0f
            )
        }
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class DrawMousePacket(private val enable: Boolean) :
    HollowPacketV3<DrawMousePacket> {
    override fun handle(player: Player, data: DrawMousePacket) {
        MouseOverlay.enable = data.enable
    }

}