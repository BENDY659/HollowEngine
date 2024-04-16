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

package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base

import net.minecraftforge.network.PacketDistributor
import ru.hollowhorizon.hollowengine.client.screen.FadeOverlayScreenPacket
import ru.hollowhorizon.hollowengine.common.scripting.players
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder

open class FadeContainer {
    var text = ""
    var subtitle = ""
    var texture = ""
    var color = 0xFFFFFF
    open var time = 0
}

fun IContextBuilder.fadeInOut(block: FadeContainer.() -> Unit) {
    +WaitNode {
        val container = FadeContainer().apply(block)
        stateMachine.server.playerList.players.forEach {
            FadeOverlayScreenPacket(
                true,
                container.text,
                container.subtitle,
                container.color,
                container.texture,
                container.time / 2
            ).send(PacketDistributor.PLAYER.with { it })
        }
        container.time / 2
    }
    +WaitNode {
        val container = FadeContainer().apply(block)
        stateMachine.server.playerList.players.forEach {
            FadeOverlayScreenPacket(
                false,
                container.text,
                container.subtitle,
                container.color,
                container.texture,
                container.time / 2
            ).send(PacketDistributor.PLAYER.with { it })
        }
        container.time / 2
    }
}

fun IContextBuilder.fadeIn(block: FadeContainer.() -> Unit) = +WaitNode {
    val container = FadeContainer().apply(block)
    stateMachine.server.playerList.players.forEach {
        FadeOverlayScreenPacket(
            true,
            container.text,
            container.subtitle,
            container.color,
            container.texture,
            container.time
        ).send(PacketDistributor.PLAYER.with { it })
    }
    container.time
}

fun IContextBuilder.fadeOut(block: FadeContainer.() -> Unit) = +WaitNode {
    val container = FadeContainer().apply(block)
    stateMachine.server.playerList.players.forEach {
        FadeOverlayScreenPacket(
            false,
            container.text,
            container.subtitle,
            container.color,
            container.texture,
            container.time
        ).send(PacketDistributor.PLAYER.with { it })
    }
    container.time
}