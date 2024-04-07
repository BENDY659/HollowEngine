package ru.hollowhorizon.hollowengine.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import ru.hollowhorizon.hc.client.imgui.ImguiHandler
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hollowengine.client.gui.scripting.CodeEditor
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager
import java.io.File

class ExampleGui : HollowScreen() {
    val registry = Registry<State, Track>(State()).apply {
        values += Track().apply {
            label = "My Track"

            channels[EventType.MOVE] = Channel().apply {
                label = "My Channel"

                events.add(Event().apply {
                    time = 10
                    length = 5
                })

                events.add(Event().apply {
                    time = 15
                    length = 5
                })
            }
        }

    }



    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        ImguiHandler.drawFrame {
            CodeEditor.draw()
        }
    }
}