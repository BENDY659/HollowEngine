package ru.hollowhorizon.hollowengine.client.gui.scripting

import com.mojang.blaze3d.vertex.PoseStack
import ru.hollowhorizon.hc.client.imgui.ImguiHandler
import ru.hollowhorizon.hc.client.screens.HollowScreen

class CodeEditorGui: HollowScreen() {
    init {
        RequestTreePacket().send()
    }

    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        ImguiHandler.drawFrame {
            CodeEditor.draw()
        }
    }

    override fun onClose() {
        super.onClose()

        CodeEditor.onClose()
    }
}