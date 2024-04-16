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
import net.minecraft.network.chat.Component
import ru.hollowhorizon.hollowengine.common.events.StoryHandler
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager.fromReadablePath
import ru.hollowhorizon.hollowengine.common.scripting.StoryLogger
import ru.hollowhorizon.hollowengine.common.scripting.players
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node
import ru.hollowhorizon.hollowengine.common.scripting.story.runScript

open class SimpleNode(val task: SimpleNode.() -> Unit) : Node() {
    override fun tick(): Boolean {
        task()
        return false
    }

    override fun serializeNBT() = CompoundTag()

    override fun deserializeNBT(nbt: CompoundTag) {
        //Ничего сериализуемого нет
    }
}

fun IContextBuilder.next(block: SimpleNode.() -> Unit) = +SimpleNode(block)

fun IContextBuilder.send(text: Component) = +SimpleNode {
    manager.server.playerList.players.forEach { it.sendSystemMessage(text) }
}

fun IContextBuilder.startScript(text: () -> String) = next {
    val file = text().fromReadablePath()
    if (!file.exists()) manager.server.playerList.players.forEach {
        it.sendSystemMessage(
            Component.translatable(
                "hollowengine.scripting.story.script_not_found",
                file.absolutePath
            )
        )
    }

    runScript(manager.server, file)
}

fun IContextBuilder.stopScript(file: () -> String) = next {
    StoryHandler.stopEvent(file())
}

fun IContextBuilder.restartScript() = next {
    StoryHandler.getEventByScript(this@restartScript.stateMachine)?.let {
        StoryHandler.restartEvent(it)
    }
}

fun IContextBuilder.execute(command: () -> String) = +SimpleNode {
    val server = this@execute.stateMachine.server
    val src = server.createCommandSourceStack()
        .withPermission(4)
        .withSuppressedOutput()

    if (server.commands.performPrefixedCommand(src, command()) == 0) {
        StoryLogger.LOGGER.warn("Command \"${command()}\" execution failed!")
    }
}