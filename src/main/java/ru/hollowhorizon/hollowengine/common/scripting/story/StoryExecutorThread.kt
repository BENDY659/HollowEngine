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

import kotlinx.coroutines.*
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.mcTranslate
import ru.hollowhorizon.kotlinscript.common.scripting.ScriptingCompiler
import ru.hollowhorizon.kotlinscript.common.scripting.errors
import ru.hollowhorizon.hollowengine.common.events.StoryHandler
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager.toReadablePath
import ru.hollowhorizon.hollowengine.common.scripting.StoryLogger
import ru.hollowhorizon.hollowengine.common.scripting.story.coroutines.ScriptContext
import java.io.File
import kotlin.concurrent.thread
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.loadDependencies
import kotlin.script.experimental.jvm.util.isError

@OptIn(DelicateCoroutinesApi::class)
fun runScript(server: MinecraftServer, file: File, isCommand: Boolean = false) =
    thread {

        StoryLogger.LOGGER.info("Starting event \"{}\".", file.toReadablePath())
        val shouldRecompile = ScriptingCompiler.shouldRecompile(file) || isCommand
        val story = ScriptingCompiler.compileFile<StoryScript>(file)

        story.errors?.let { errors ->
            errors.forEach { error ->
                server.playerList.players.forEach {
                    StoryLogger.LOGGER.error(error.replace("\\r\\n", "\n"))
                    it.sendSystemMessage("§c[ERROR]§r $error".mcText)
                }
            }
            return@thread
        }

        //TODO: Проверить, не изменился ли код после обновления мода
        val res = story.execute {
            constructorArgs(server)
            jvm {
                loadDependencies(false)
            }
        }

        val returnValue = res.valueOrThrow().returnValue

        when {
            res.isError() -> {
                (res as ResultWithDiagnostics.Failure).errors().forEach { error ->
                    server.playerList.players.forEach { it.sendSystemMessage("§c[ERROR]§r $error".mcText) }
                }
            }

            returnValue is ResultValue.Error -> {
                val error = returnValue.error
                server.playerList.players.forEach {
                    it.sendSystemMessage(Component.translatable("hollowengine.executing_error", file.toReadablePath()))
                    it.sendSystemMessage("${error.message}".mcText)
                    it.sendSystemMessage("hollowengine.check_logs".mcTranslate)
                }

                StoryLogger.LOGGER.error("(HollowEngine) Error while executing event \"${file.toReadablePath()}\"", error)
            }

            else -> {
                val resScript = res.valueOrThrow().returnValue.scriptInstance as StoryStateMachine
                StoryHandler.addStoryEvent(file.toReadablePath(), resScript, shouldRecompile)
            }
        }

    }