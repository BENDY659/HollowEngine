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

import net.minecraft.client.Minecraft
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.screens.widget.layout.box
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.common.ui.Alignment
import ru.hollowhorizon.hollowengine.client.screen.widget.ModelPreviewWidget
import ru.hollowhorizon.hollowengine.client.screen.widget.box.TypeBox
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity

class NPCBuilderScreen : HollowScreen("".mcText) {
    val npc: NPCEntity = NPCEntity(Minecraft.getInstance().level!!)

    override fun init() {
        super.init()
        box {
            align = Alignment.CENTER
            size = 100.pc x 100.pc
            pos = 0.px x 5.pc

            box {
                align = Alignment.RIGHT_CENTER
                size = 50.pc x 100.pc

                elements {
                    +ModelPreviewWidget(npc,0, 0, 100.pc.w().value, 100.pc.h().value, this@NPCBuilderScreen.width, this@NPCBuilderScreen.height)
                }
            }

            box {
                align = Alignment.LEFT_CENTER
                size = 50.pc x 100.pc

                box {
                    align = Alignment.TOP_CENTER
                    size = 100.pc x 20.pc

                    elements {
                        +TypeBox(
                            0, 0, 100.pc.w().value, 100.pc.h().value,
                            "hollowengine.input_model_path",
                            "hollowengine.model_example"
                        )
                    }
                }

                box {
                    align = Alignment.TOP_CENTER
                    size = 100.pc x 20.pc
                    pos = 0.px x 20.pc

                    elements {
                        +TypeBox(
                            0, 0, 100.pc.w().value, 100.pc.h().value,
                            "Введите путь к модели:",
                            "модель (modid:path/to/model.gltf)"
                        )
                    }
                }

                box {
                    align = Alignment.TOP_CENTER
                    size = 100.pc x 20.pc
                    pos = 0.pc x 40.pc

                    elements {
                        +TypeBox(
                            0, 0, 100.pc.w().value, 100.pc.h().value,
                            "Введите путь к модели:",
                            "модель (modid:path/to/model.gltf)"
                        )
                    }
                }
            }
        }
    }


}