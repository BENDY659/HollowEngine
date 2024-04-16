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

package ru.hollowhorizon.hollowengine.common.scripting.story

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.SubscribeEvent

interface IForgeEventScriptSupport {
    val forgeEvents: MutableSet<ForgeEvent<*>>
}

inline fun <reified T : Event> waitForgeEvent(noinline function: (T) -> Boolean) {
    val event = ForgeEvent(T::class.java, function)
    MinecraftForge.EVENT_BUS.register(event)
    event.waitEvent()
}

inline fun <reified T : Event> IForgeEventScriptSupport.whenForgeEvent(noinline function: (T) -> Unit) {
    val event = ForgeEvent(T::class.java) { function(it); return@ForgeEvent false }
    MinecraftForge.EVENT_BUS.register(event)
    this.forgeEvents.add(event)
}

class ForgeEvent<T : Event>(private val type: Class<T>, private val function: (T) -> Boolean) {
    private val waiter = Object()


    @SubscribeEvent
    fun onEvent(event: T) {
        val etype = event::class.java

        if (!etype.isAssignableFrom(type)) return

        try {
            if (function(event)) {
                MinecraftForge.EVENT_BUS.unregister(this)
                synchronized(waiter) { waiter.notifyAll() }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun waitEvent() {
        synchronized(waiter) { waiter.wait() }
    }
}