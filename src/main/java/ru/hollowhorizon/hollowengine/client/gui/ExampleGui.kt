package ru.hollowhorizon.hollowengine.client.gui

import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import imgui.ImGui
import imgui.flag.ImGuiWindowFlags
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3
import ru.hollowhorizon.hc.client.imgui.ImguiHandler
import ru.hollowhorizon.hc.client.screens.HollowScreen

class ExampleGui : HollowScreen() {
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

        frameBuffer.unbindWrite()
        buffer.unbindRead()
        buffer.bindWrite(true)

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        ImguiHandler.drawFrame {
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
                ImGui.endMainMenuBar();
            }

            //ImGui.setCursorPos(0f, 0f)
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