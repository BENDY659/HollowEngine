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
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.ServerChatEvent
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.ForgeEventNode
import ru.hollowhorizon.hollowengine.common.util.Safe
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

data class InputContainer(
    var message: String = "",
    var player: Safe<List<ServerPlayer>> = Safe { emptyList() },
)

class InputNode(vararg val values: String, val players: Safe<List<ServerPlayer>>) :
    ForgeEventNode<ServerChatEvent.Submitted>(ServerChatEvent.Submitted::class.java, { true }),
    ReadWriteProperty<Any?, String> {
    var hasPlayer = false
    var message = ""
    override val action = { event: ServerChatEvent.Submitted ->
        val player = event.player

        hasPlayer = true
        message = event.message.string

        val isValidPlayer = player in players()

        isValidPlayer && (values.any { it.equals(event.message.string, ignoreCase = true) } || values.isEmpty())
    }

    override fun serializeNBT(): CompoundTag {
        return super.serializeNBT().apply {
            putString("message", message)
            }
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        super.deserializeNBT(nbt)
        message = nbt.getString("message")
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return message
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        message = value
    }
}