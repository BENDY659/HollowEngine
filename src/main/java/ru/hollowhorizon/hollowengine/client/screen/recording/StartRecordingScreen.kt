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

package ru.hollowhorizon.hollowengine.client.screen.recording

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.toasts.SystemToast
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.api.IAutoScaled
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.screens.widget.button.BaseButton
import ru.hollowhorizon.hc.client.screens.widget.layout.PlacementType
import ru.hollowhorizon.hc.client.screens.widget.layout.box
import ru.hollowhorizon.hc.client.utils.*
import ru.hollowhorizon.hc.common.ui.Alignment
import ru.hollowhorizon.hc.common.ui.Anchor
import ru.hollowhorizon.hollowengine.client.screen.npcs.LabelWidget
import ru.hollowhorizon.hollowengine.client.screen.overlays.RecordingDriver
import ru.hollowhorizon.hollowengine.client.screen.widget.HollowTextFieldWidget
import ru.hollowhorizon.hollowengine.cutscenes.replay.ToggleRecordingPacket

class StartRecordingScreen : HollowScreen(), IAutoScaled {

    init {
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true)
    }

    override fun init() {
        super.init()

        box {
            size = 90.pc x 90.pc
            renderer = { stack, x, y, w, h ->
                fill(stack, x, y, x + w, y + h, 0x8800173D.toInt())
                fill(stack, x, y, x + w, y + 2, 0xFF07BBDB.toInt())
                fill(stack, x, y + h - 2, x + w, y + h, 0xFF07BBDB.toInt())
                fill(stack, x, y, x + 2, y + h, 0xFF07BBDB.toInt())
                fill(stack, x + w - 2, y, x + w, y + h, 0xFF07BBDB.toInt())

            }

            elements {
                align = Alignment.CENTER
                spacing = 4.pc x 4.pc
                placementType = PlacementType.GRID

                +LabelWidget(
                    "hollowengine.enter_replay".mcTranslate,
                    anchor = Anchor.START,
                    color = 0xFFFFFF,
                    hoveredColor = 0xFFFFFF,
                    scale = 1.5f
                )
                //lineBreak()
                val replayName = +HollowTextFieldWidget(
                    font, 0, 0, 90.pc.w().value, 20,
                    "".mcText,
                    "hollowengine:textures/gui/text_field.png".rl
                )

                +LabelWidget(
                    "hollowengine.enter_model_path".mcTranslate,
                    anchor = Anchor.START,
                    color = 0xFFFFFF,
                    hoveredColor = 0xFFFFFF,
                    scale = 1.2f
                )
                //lineBreak()
                val modelName = +HollowTextFieldWidget(
                    font, 0, 0, 90.pc.w().value, 20,
                    "".mcText,
                    "hollowengine:textures/gui/text_field.png".rl
                )
                modelName.value = "hollowengine:models/entity/player_model.gltf"
                modelName.setResponder {
                    if (!ResourceLocation.isValidResourceLocation(it) || !it.rl.exists() ||
                        !(it.endsWith(".gltf") || it.endsWith(".glb"))
                    ) {
                        modelName.setTextColor(0xF54242)
                    } else {
                        modelName.setTextColor(0x42f542)
                    }
                }
                //lineBreak()

                +BaseButton(
                    0, 0, 43.pc.w().value, 20,
                    "hollowengine.start".mcTranslate,
                    {
                        if (modelName.value.rl.exists() && modelName.value.isNotEmpty()) {
                            startRecording(replayName.value, modelName.value)
                            onClose()
                        } else {
                            Minecraft.getInstance().toasts.addToast(
                                SystemToast(
                                    SystemToast.SystemToastIds.TUTORIAL_HINT,
                                    "HollowEngine Error".mcText,
                                    "Invalid model path!".mcText
                                )
                            )
                        }
                    },
                    "hollowengine:textures/gui/long_button.png".rl
                )
                +BaseButton(
                    0, 0, 43.pc.w().value, 20,
                    "hollowengine.cancel".mcTranslate,
                    { onClose() },
                    "hollowengine:textures/gui/long_button.png".rl
                )
            }
        }
    }

    override fun onClose() {
        super.onClose()
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false)
    }
}

fun startRecording(replayName: String, modelName: String) {
    Minecraft.getInstance().player!![AnimatedEntityCapability::class].model = modelName
    ToggleRecordingPacket(replayName).send()
    RecordingDriver.resetTime()
    RecordingDriver.enable = true
}