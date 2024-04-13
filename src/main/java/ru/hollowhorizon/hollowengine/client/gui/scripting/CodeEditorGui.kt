package ru.hollowhorizon.hollowengine.client.gui.scripting

import com.mojang.blaze3d.vertex.PoseStack
import ru.hollowhorizon.hc.client.imgui.ImguiHandler
import ru.hollowhorizon.hc.client.screens.HollowScreen

class CodeEditorGui: HollowScreen() {
    init {
        RequestTreePacket().send()
    }
    var shouldClose = false

    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        ImguiHandler.drawFrame {
            CodeEditor.draw()
            if(shouldClose) onClose()
        }
    }

    override fun onClose() {
        if(!shouldClose) {
            CodeEditor.shouldClose = true
            shouldClose = true
            return
        }

        super.onClose()
        CodeEditor.onClose()
        shouldClose = false
    }

    override fun isPauseScreen() = false
}