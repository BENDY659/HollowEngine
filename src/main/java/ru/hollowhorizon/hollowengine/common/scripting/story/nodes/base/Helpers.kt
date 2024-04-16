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
import net.minecraft.nbt.ListTag
import net.minecraftforge.common.util.INBTSerializable
import ru.hollowhorizon.hollowengine.common.scripting.StoryLogger
import ru.hollowhorizon.hollowengine.common.scripting.story.StoryStateMachine
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.npcs.*

fun CompoundTag.serializeNodes(name: String, nodes: Collection<Node>) {
    put(name, ListTag().apply {
        addAll(nodes.map(INBTSerializable<CompoundTag>::serializeNBT))
    })
}

fun CompoundTag.deserializeNodes(name: String, nodes: Collection<Node>) {
    val list = getList(name, 10)
    nodes.forEachIndexed { i, node ->
        node.deserializeNBT(list.getCompound(i))
    }
}

open class NodeContextBuilder(override val stateMachine: StoryStateMachine) : IContextBuilder() {
    val tasks = ArrayList<Node>()

    override operator fun <T: Node> T.unaryPlus(): T {
        this.manager = stateMachine
        this.init()
        if(manager.isStarted) {
            StoryLogger.LOGGER.fatal("It is not possible to add a ${this.javaClass.simpleName} action after running the script! You may have forgotten to write `IContextBuilder.` before the name of your function? Or you just add action in other action?!")
            throw IllegalStateException("It is not possible to add a ${this.javaClass.simpleName} action after running the script! You may have forgotten to write `IContextBuilder.` before the name of your function? Or you just add action in other action?!")
        }
        tasks.add(this)
        return this
    }
}

class ChainNode(val nodes: ArrayList<Node>): Node() {
    var index = 0
    val isEnded get() = index >= nodes.size

    override fun tick(): Boolean {
        if(isEnded) return false
        if(!nodes[index].tick()) index++
        return !isEnded
    }

    override fun serializeNBT() = CompoundTag().apply {
        putInt("index", index)
        serializeNodes("nodes", nodes)
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        index = nbt.getInt("index")
        nbt.deserializeNodes("nodes", nodes)
    }

}