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

package ru.hollowhorizon.hollowengine.client.screen

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.HitResult
import net.minecraftforge.client.event.RenderLevelStageEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.utils.mcText

class CutsceneWorldEditScreen : HollowScreen("".mcText) {
    private var lookPos: BlockPos? = null
    private var currentButton = -1

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun render(p_230430_1_: PoseStack, mouseX: Int, mouseY: Int, p_230430_4_: Float) {
        super.render(p_230430_1_, mouseX, mouseY, p_230430_4_)

        val player = Minecraft.getInstance().player!!

        val blockPos = BlockPos(player.pick(mouseX.toDouble(), mouseY.toDouble()).location)

        val state = player.level.getBlockState(blockPos)

        if (!state.isAir) {
            this.lookPos = blockPos
        } else {
            this.lookPos = null
        }

        if (lookPos != null) {
            if (currentButton == 0) {
                player.level.destroyBlock(lookPos!!, false, player)
            } else if (currentButton == 1 && player.mainHandItem.item is BlockItem) {
                player.level.setBlockAndUpdate(
                    lookPos!!,
                    (player.mainHandItem.item as BlockItem).block.defaultBlockState()
                )
            }
        }
    }

    @SubscribeEvent
    fun renderWorld(event: RenderLevelStageEvent) {
        if(event.stage != RenderLevelStageEvent.Stage.AFTER_SKY) return

        if (lookPos != null) {
            val level = Minecraft.getInstance().level!!
            val shape = level.getBlockState(lookPos!!).getShape(level, lookPos!!)

            val buffer = Minecraft.getInstance().renderBuffers().bufferSource()
            LevelRenderer.renderVoxelShape(
                event.poseStack,
                buffer.getBuffer(RenderType.lines()),
                shape,
                -lookPos!!.x.toDouble(),
                -lookPos!!.y.toDouble(),
                -lookPos!!.z.toDouble(),
                0.0f,
                0.0f,
                0.0f,
                0.4f
            )
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, ms: Double): Boolean {
        var scroll = ms
        val direction = Minecraft.getInstance().player!!.direction
        val rotVec = Quaternion(0.0f, 0.0f, 0.0f, 1.0f)
        rotVec.mul(Vector3f.YP.rotationDegrees(Minecraft.getInstance().player!!.yHeadRot))
        rotVec.mul(Vector3f.XP.rotationDegrees(Minecraft.getInstance().player!!.xRot))

        val forward = Vector3f(0.0f, 0.0f, 1.0f)
        forward.transform(rotVec)

        if(direction == Direction.WEST || direction == Direction.EAST) scroll *= -1.0

        val offsetX = forward.x() * scroll
        val offsetY = forward.y() * scroll
        val offsetZ = forward.z() * scroll

        val oldPos = Minecraft.getInstance().player!!.position()
        Minecraft.getInstance().player!!.setPos(oldPos.x + offsetX, oldPos.y + offsetY, oldPos.z + offsetZ)

        return super.mouseScrolled(mouseX, mouseY, ms)
    }

    override fun mouseClicked(p_231044_1_: Double, p_231044_3_: Double, button: Int): Boolean {
        currentButton = button
        return super.mouseClicked(p_231044_1_, p_231044_3_, button)
    }

    override fun mouseReleased(p_231048_1_: Double, p_231048_3_: Double, button: Int): Boolean {
        currentButton = -1
        return super.mouseReleased(p_231048_1_, p_231048_3_, button)
    }

    override fun isPauseScreen(): Boolean {
        return false
    }

    override fun onClose() {
        super.onClose()
        MinecraftForge.EVENT_BUS.unregister(this)
    }
}

fun Player.pick(mouseX: Double, mouseY: Double): HitResult {
    val disctance = 50.0F

    val eyePosition = getEyePosition(0.0F)
    val lookVector = CameraHelper.getMouseBasedViewVector(Minecraft.getInstance(), this.xRot, this.yRot)
    val toVector = eyePosition.add(lookVector.x * disctance, lookVector.y * disctance, lookVector.z * disctance)

    return level.clip(
        ClipContext(
            eyePosition,
            toVector,
            ClipContext.Block.OUTLINE,
            ClipContext.Fluid.NONE,
            this
        )
    )
}
