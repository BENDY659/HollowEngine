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

package ru.hollowhorizon.hollowengine.client.render.entity

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraftforge.client.ForgeHooksClient
import ru.hollowhorizon.hc.client.models.gltf.manager.IAnimated
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hollowengine.common.npcs.NPCCapability
import ru.hollowhorizon.hollowengine.common.npcs.NpcIcon

class NPCRenderer<T>(context: EntityRendererProvider.Context) :
    GLTFEntityRenderer<T>(context) where T : LivingEntity, T : IAnimated {

    override fun renderNameTag(
        pEntity: T,
        pDisplayName: Component,
        pMatrixStack: PoseStack,
        pBuffer: MultiBufferSource,
        pPackedLight: Int
    ) {
        super.renderNameTag(pEntity, pDisplayName, pMatrixStack, pBuffer, pPackedLight)

        val icon = pEntity[NPCCapability::class].icon

        if (icon == NpcIcon.EMPTY) return

        val dist = entityRenderDispatcher.distanceToSqr(pEntity)
        if (ForgeHooksClient.isNameplateInRenderDistance(pEntity, dist)) {
            val f = pEntity.bbHeight + 0.75f + icon.offsetY

            pMatrixStack.pushPose()
            pMatrixStack.translate(0.0, f.toDouble(), 0.0)
            pMatrixStack.mulPose(entityRenderDispatcher.cameraOrientation())
            pMatrixStack.scale(-0.025f, -0.025f, 0.025f)

            RenderSystem.setShaderTexture(0, icon.image)

            val size = (16f * icon.scale).toInt()
            val pos = size / 2

            Screen.blit(pMatrixStack, -pos, -pos, 0f, 0f, size, size, size, size)

            pMatrixStack.popPose()
        }
    }
}