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

package ru.hollowhorizon.hollowengine.client.gui

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import imgui.ImGui
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImBoolean
import net.minecraft.client.Minecraft
import ru.hollowhorizon.hc.client.imgui.ImguiHandler
import ru.hollowhorizon.hc.client.screens.HollowScreen

class FramebufferExample : HollowScreen() {
    lateinit var frameBuffer: RenderTarget
    override fun init() {
        super.init()
        val buffer = Minecraft.getInstance().mainRenderTarget
        frameBuffer = TextureTarget(buffer.width, buffer.height, true, Minecraft.ON_OSX)
        frameBuffer.setClearColor(0f, 0f, 0f, 0f)
    }

    override fun resize(pMinecraft: Minecraft, pWidth: Int, pHeight: Int) {
        frameBuffer.destroyBuffers()
        super.resize(pMinecraft, pWidth, pHeight)
    }

    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        val buffer = Minecraft.getInstance().mainRenderTarget


        buffer.unbindWrite()
        buffer.bindRead()
        frameBuffer.clear(Minecraft.ON_OSX)
        frameBuffer.copyDepthFrom(buffer)
        frameBuffer.bindWrite(true)

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderTexture(0, buffer.colorTextureId)
        blit(pPoseStack, 0, 0, 0f, 0f, width, height, width, height)
        RenderSystem.disableBlend()

        frameBuffer.unbindWrite()
        buffer.unbindRead()
        buffer.bindWrite(true)



        ImguiHandler.drawFrame {
            show(ImBoolean(true))

            val window = Minecraft.getInstance().window
            ImGui.getBackgroundDrawList().addRectFilled(
                0f,
                0f,
                window.width.toFloat(),
                window.height.toFloat(),
                ImGui.colorConvertFloat4ToU32(0f, 0f, 0f, 1f),
                0f
            )
            if (ImGui.beginMainMenuBar()) {
                if (ImGui.beginMenu("Файл")) {
                    ImGui.endMenu()
                }
                if (ImGui.beginMenu("Изменить")) {
                    ImGui.endMenu()
                }
                ImGui.endMainMenuBar()
            }

            if (ImGui.begin(
                    "Main",
                    ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoScrollbar or ImGuiWindowFlags.NoScrollWithMouse
            or ImGuiWindowFlags.NoMove
            )
            ) {
                ImGui.setWindowSize(frameBuffer.width.toFloat() / 2, frameBuffer.height.toFloat() / 2 + 50)

                ImGui.image(
                    frameBuffer.colorTextureId,
                    frameBuffer.width.toFloat() / 2,
                    frameBuffer.height.toFloat() / 2
                )
                Minecraft.getInstance().player?.let {
                    if (ImGui.isItemHovered()) {
                        it.xRot += ImGui.getMouseDragDeltaY() / 100f
                        it.yRot += ImGui.getMouseDragDeltaX() / 100f
                    }
                }
            }
            ImGui.end()
        }
    }

    override fun isPauseScreen() = false
}