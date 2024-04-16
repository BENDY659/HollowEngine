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
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.HasInnerNodes
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node
import java.util.*
import kotlin.collections.ArrayList

class ConditionNode(
    private var condition: () -> Boolean,
    private val ifTasks: List<Node>,
    private val elseTasks: MutableList<Node>
) : Node(), HasInnerNodes {
    var index = 0
    private val isEnd get() = index >= if (condition()) ifTasks.size else elseTasks.size
    override val currentNode get() = if (condition()) ifTasks[index] else elseTasks[index]

    fun setElseTasks(tasks: List<Node>) {
        elseTasks.clear()
        elseTasks.addAll(tasks)
    }

    override fun tick(): Boolean {
        if(isEnd) return false

        if (!currentNode.tick()) index++

        return true
    }

    override fun serializeNBT() = CompoundTag().apply {
        serializeNodes("if_tasks", ifTasks)
        serializeNodes("else_tasks", elseTasks)
        putInt("index", index)
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        nbt.deserializeNodes("if_tasks", ifTasks)
        nbt.deserializeNodes("else_tasks", elseTasks)
        index = nbt.getInt("index")
    }
}

fun IContextBuilder.If(
    condition: () -> Boolean,
    ifTasks: NodeContextBuilder.() -> Unit,
    elseTasks: NodeContextBuilder.() -> Unit
) = +ConditionNode(
    condition,
    NodeContextBuilder(this.stateMachine).apply(ifTasks).tasks,
    NodeContextBuilder(this.stateMachine).apply(elseTasks).tasks
)

fun IContextBuilder.If(condition: () -> Boolean, ifTasks: NodeContextBuilder.() -> Unit) =
    +ConditionNode(condition, NodeContextBuilder(this.stateMachine).apply(ifTasks).tasks, ArrayList())

infix fun ConditionNode.Else(tasks: NodeContextBuilder.() -> Unit) =
    setElseTasks(NodeContextBuilder(manager).apply(tasks).tasks)

