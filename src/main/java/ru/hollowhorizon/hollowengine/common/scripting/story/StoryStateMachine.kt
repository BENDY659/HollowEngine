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

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.MinecraftServer
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.TickEvent.ServerTickEvent
import ru.hollowhorizon.hollowengine.common.scripting.StoryLogger
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.deserializeNodes
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.serializeNodes

open class StoryStateMachine(val server: MinecraftServer) : IContextBuilder() {
    val variables = ArrayList<StoryVariable<*>>()
    val startTasks = ArrayList<() -> Unit>()
    val scriptRequirements = ArrayList<() -> Boolean>()
    val onTickTasks = ArrayList<() -> Unit>()
    var extra = CompoundTag()
    internal val nodes = ArrayList<Node>()
    internal val asyncNodes = ArrayList<Node>()
    internal var currentIndex = 0
    val asyncNodeIds = ArrayList<Int>()
    var isStarted = false
    val isEnded get() = currentIndex >= nodes.size && asyncNodeIds.isEmpty() && onTickTasks.isEmpty()


    fun tick(event: ServerTickEvent) {
        if (event.phase != TickEvent.Phase.END) return

        // Требования для работы скрипта, например нужные npc должны быть загружены
        if (!scriptRequirements.all { it() }) return

        if (onTickTasks.isNotEmpty()) {
            onTickTasks.forEach { it() }
            onTickTasks.clear()
        }

        val toRemove = asyncNodeIds.mapNotNull { if (!asyncNodes[it].tick()) it else null }

        toRemove.forEach(asyncNodeIds::remove)

        if (currentIndex >= nodes.size) return

        if (!isEnded && !nodes[currentIndex].tick()) currentIndex++
    }

    fun serialize() = CompoundTag().apply {
        serializeNodes("\$nodes", nodes)
        putInt("\$current", currentIndex)
        put("\$variables", ListTag().apply {
            addAll(variables.map { it.serializeNBT() })
        })
        put("\$extra", extra)

        serializeNodes("\$async_nodes", asyncNodes)
        put(
            "\$async_ids",
            ListTag().apply { asyncNodeIds.forEachIndexed { index, i -> add(index, IntTag.valueOf(i)) } })
    }

    fun deserialize(nbt: CompoundTag) {
        nbt.deserializeNodes("\$nodes", nodes)
        currentIndex = nbt.getInt("\$current")
        variables.forEachIndexed { index, storyVariable ->
            storyVariable.deserializeNBT(nbt.getList("\$variables", 10).getCompound(index))
        }
        extra = nbt.getCompound("\$extra")

        nbt.deserializeNodes("\$async_nodes", asyncNodes)
        asyncNodeIds.clear()
        val list = nbt.getList("\$async_ids", 3)

        for (i in 0 until list.size) {
            asyncNodeIds.add(list.getInt(i))
        }
    }

    override val stateMachine = this
    override fun <T : Node> T.unaryPlus(): T {
        if (isStarted) {
            StoryLogger.LOGGER.fatal("It is not possible to add a ${this.javaClass.simpleName} action after running the script! You may have forgotten to write `IContextBuilder.` before the name of your function? Or you just add action in other action?!")
            throw IllegalStateException("It is not possible to add a ${this.javaClass.simpleName} action after running the script! You may have forgotten to write `IContextBuilder.` before the name of your function? Or you just add action in other action?!")
        }
        this.manager = this@StoryStateMachine
        this.init()
        nodes.add(this)
        return this
    }

    fun cleanup() {

    }
}