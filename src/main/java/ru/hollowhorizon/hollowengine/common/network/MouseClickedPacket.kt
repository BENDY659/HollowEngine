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

package ru.hollowhorizon.hollowengine.common.network

import kotlinx.serialization.Serializable
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerEvent
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hollowengine.client.ClientEvents

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class MouseClickedPacket(val button: MouseButton) : HollowPacketV3<MouseClickedPacket> {
    override fun handle(player: Player, data: MouseClickedPacket) {
        MinecraftForge.EVENT_BUS.post(ServerMouseClickedEvent(player, data.button))
    }
}

class ServerMouseClickedEvent(player: Player, val button: MouseButton) : PlayerEvent(player)

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class MouseButtonWaitPacket(val button: MouseButton) : HollowPacketV3<MouseButtonWaitPacket> {
    override fun handle(player: Player, data: MouseButtonWaitPacket) {
        ClientEvents.canceledButtons.add(data.button)
    }

}

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class MouseButtonWaitResetPacket : HollowPacketV3<MouseButtonWaitResetPacket> {
    override fun handle(player: Player, data: MouseButtonWaitResetPacket) {
        ClientEvents.canceledButtons.clear()
    }

}

@Serializable
class Container(val data: MouseButton)

enum class MouseButton {
    LEFT, RIGHT, MIDDLE, NONE;

    companion object {
        fun from(value: Int): MouseButton {
            return entries[value]
        }
    }
}
