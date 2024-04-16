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

package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.events

import net.minecraft.nbt.CompoundTag
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.network.PacketDistributor
import ru.hollowhorizon.hc.client.screens.ClosedGuiEvent
import ru.hollowhorizon.hc.common.ui.CURRENT_SERVER_GUI
import ru.hollowhorizon.hc.common.ui.OpenGuiPacket
import ru.hollowhorizon.hc.common.ui.Widget
import ru.hollowhorizon.hollowengine.common.scripting.players
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node

class GuiNode(gui: Widget.() -> Unit) : Node() {
    val gui by lazy {
        Widget().apply(gui)
    }
    var isStarted = false
    var isEnded = false
    override fun tick(): Boolean {
        if (!isStarted) {
            CURRENT_SERVER_GUI = gui
            manager.server.playerList.players.forEach {
                OpenGuiPacket(gui).send(PacketDistributor.PLAYER.with { it })
            }
            isStarted = true
            MinecraftForge.EVENT_BUS.register(this)
        }

        return !isEnded
    }

    @SubscribeEvent
    fun onEvent(event: ClosedGuiEvent) {
        isEnded = event.entity in manager.server.playerList.players
    }

    override fun serializeNBT() = CompoundTag().apply {
        putBoolean("isEnded", isEnded)
        putBoolean("isStarted", isStarted)
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        isEnded = nbt.getBoolean("isEnded")
        isStarted = nbt.getBoolean("isStarted")
    }

}

fun IContextBuilder.gui(gui: Widget.() -> Unit) = +GuiNode(gui)