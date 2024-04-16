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

import net.minecraft.nbt.CompoundTag
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.SubscribeEvent
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node

open class ForgeEventNode<T : Event>(private val type: Class<T>, open val action: (T) -> Boolean) : Node() {
    protected var isStarted = false
    protected var isEnded = false

    @SubscribeEvent
    fun onEvent(event: T) {
        val etype = event::class.java

        if (!type.isAssignableFrom(etype)) return

        if (action(event)) {
            isEnded = true
            MinecraftForge.EVENT_BUS.unregister(this)
        }
    }

    override fun tick(): Boolean {
        if (!isStarted) {
            isStarted = true
            MinecraftForge.EVENT_BUS.register(this)
        }
        return !isEnded
    }

    override fun serializeNBT() = CompoundTag().apply {
        putBoolean("isEnded", isEnded)
        putBoolean("isStarted", isStarted)
    }
    override fun deserializeNBT(nbt: CompoundTag) {
        isEnded = nbt.getBoolean("isEnded")
        isStarted = false
    }
}

inline fun <reified T : Event> IContextBuilder.waitForgeEvent(noinline function: (T) -> Boolean) =
    +ForgeEventNode(T::class.java, function)