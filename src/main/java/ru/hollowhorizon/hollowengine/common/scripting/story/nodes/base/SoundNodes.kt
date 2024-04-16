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

import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket
import net.minecraft.sounds.SoundSource
import net.minecraft.world.phys.Vec3
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hollowengine.common.scripting.players
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder

class SoundContainer {
    var sound = ""
    var volume = 1.0f
    var pitch = 1.0f
    var pos: Vec3? = null
}

fun IContextBuilder.playSound(sound: SoundContainer.() -> Unit) = +SimpleNode {
    val container = SoundContainer().apply(sound)
    stateMachine.server.playerList.players.forEach {
        it.connection.send(
            ClientboundCustomSoundPacket(
                container.sound.rl,
                SoundSource.MASTER,
                container.pos ?: it.position(),
                container.volume,
                container.pitch,
                it.random.nextLong()
            )
        )
    }
}

fun IContextBuilder.stopSound(sound: () -> String) = +SimpleNode {
    stateMachine.server.playerList.players.forEach {
        it.connection.send(
            ClientboundStopSoundPacket(
                sound().rl,
                SoundSource.MASTER
            )
        )
    }
}