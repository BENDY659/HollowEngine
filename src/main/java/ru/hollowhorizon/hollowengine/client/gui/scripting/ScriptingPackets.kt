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

package ru.hollowhorizon.hollowengine.client.gui.scripting

import imgui.type.ImBoolean
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import org.apache.commons.io.FileUtils
import ru.hollowhorizon.hc.client.utils.colored
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.plus
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hollowengine.common.events.StoryHandler
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager
import ru.hollowhorizon.hollowengine.common.files.DirectoryManager.fromReadablePath
import ru.hollowhorizon.hollowengine.common.scripting.story.runScript
import ru.hollowhorizon.kotlinscript.common.events.Severity

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class RequestFilePacket(val path: String) : HollowPacketV3<RequestFilePacket> {
    override fun handle(player: Player, data: RequestFilePacket) {
        if (player.hasPermissions(2)) {
            val fileTypes = setOf(".kts", ".json", ".txt", ".mcfunction", ".md")
            if (fileTypes.any { data.path.endsWith(it) }) {
                val file = data.path.fromReadablePath().readText()
                UpdateFilePacket(data.path, file).send(player as ServerPlayer)
            }
        } else {
            player.sendSystemMessage("You don't have permissions to open scripts!".mcText)
        }
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class RequestTreePacket : HollowPacketV3<RequestTreePacket> {
    override fun handle(player: Player, data: RequestTreePacket) {
        if (!player.hasPermissions(2)) {
            player.sendSystemMessage("You don't have permissions to open scripts!".mcText)
            return
        }

        val tree = CodeEditor.tree(DirectoryManager.HOLLOW_ENGINE)
        LoadTreePacket(tree).send(player as ServerPlayer)
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class LoadTreePacket(val tree: Tree) : HollowPacketV3<LoadTreePacket> {
    override fun handle(player: Player, data: LoadTreePacket) {
        CodeEditor.tree = data.tree
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class UpdateFilePacket(val path: String, val text: String) : HollowPacketV3<UpdateFilePacket> {
    override fun handle(player: Player, data: UpdateFilePacket) {
        CodeEditor.currentFile = ""
        CodeEditor.files.removeIf { it.path == data.path }
        CodeEditor.files.add(
            ScriptData(path.substringAfterLast('/'), data.text, path, ImBoolean(true))
        )
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class SaveFilePacket(val path: String, val text: String) : HollowPacketV3<SaveFilePacket> {
    override fun handle(player: Player, data: SaveFilePacket) {
        if (!player.hasPermissions(2)) {
            player.sendSystemMessage("You don't have permissions to save scripts!".mcText)
            return
        }

        val file = data.path.fromReadablePath()
        if (file.exists()) {
            file.writeText(data.text)
            UpdateFilePacket(data.path, data.text).send(*player.server!!.playerList.players.filter { it != player }
                .toTypedArray())
        }
    }

}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class DeleteFilePacket(val path: String) : HollowPacketV3<DeleteFilePacket> {
    override fun handle(player: Player, data: DeleteFilePacket) {
        if (player.hasPermissions(2)) {
            val file = data.path.fromReadablePath()
            if (file.exists()) {
                if (file.isDirectory) FileUtils.deleteDirectory(file)
                else file.delete()
                val tree = CodeEditor.tree(DirectoryManager.HOLLOW_ENGINE)
                LoadTreePacket(tree).send(*player.server!!.playerList.players.toTypedArray())
            }
        } else {
            player.sendSystemMessage("You don't have permissions to delete scripts!".mcText)
        }
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class RenameFilePacket(val path: String, val newName: String) : HollowPacketV3<RenameFilePacket> {
    override fun handle(player: Player, data: RenameFilePacket) {
        if (player.hasPermissions(2)) {
            val file = data.path.fromReadablePath()
            if (file.exists()) {
                file.renameTo(file.parentFile.resolve(newName))
                val tree = CodeEditor.tree(DirectoryManager.HOLLOW_ENGINE)
                LoadTreePacket(tree).send(*player.server!!.playerList.players.toTypedArray())
            }
        } else {
            player.sendSystemMessage("You don't have permissions to delete scripts!".mcText)
        }
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class CreateFilePacket(val path: String) : HollowPacketV3<CreateFilePacket> {
    override fun handle(player: Player, data: CreateFilePacket) {
        if (player.hasPermissions(2)) {
            val file = data.path.fromReadablePath()
            if (!file.exists()) {
                if (!file.parentFile.exists()) file.parentFile.mkdirs()

                if (!file.name.contains(".")) file.mkdirs()
                else file.createNewFile()

                val tree = CodeEditor.tree(DirectoryManager.HOLLOW_ENGINE)
                LoadTreePacket(tree).send(*player.server!!.playerList.players.toTypedArray())

            }
        } else {
            player.sendSystemMessage("You don't have permissions to create scripts!".mcText)
        }
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class RunScriptPacket(val path: String) : HollowPacketV3<RunScriptPacket> {
    override fun handle(player: Player, data: RunScriptPacket) {

        if (player.hasPermissions(2)) {
            val server = player.server!!
            val file = data.path.fromReadablePath()

            runScript(server, file, true)
        } else {
            player.sendSystemMessage("You don't have permissions to run scripts!".mcText)
        }
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class StopScriptPacket(val path: String) : HollowPacketV3<StopScriptPacket> {
    override fun handle(player: Player, data: StopScriptPacket) {
        if (player.hasPermissions(2)) {
            StoryHandler.stopEvent(path)
        } else {
            player.sendSystemMessage("You don't have permissions to stop scripts!".mcText)
        }
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class ScriptErrorPacket(val script: String, val errors: List<ScriptError>) : HollowPacketV3<ScriptErrorPacket> {
    override fun handle(player: Player, data: ScriptErrorPacket) {
        if (CodeEditor.currentFile == script) {
            val errors = errors.filter { it.severity == Severity.ERROR || it.severity == Severity.FATAL }
                .associate { it.line to "${it.message} at column: ${it.column} " }
            CodeEditor.editor.setErrorMarkers(errors)
        }
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class ScriptStartedPacket(val script: String) : HollowPacketV3<ScriptStartedPacket> {
    override fun handle(player: Player, data: ScriptStartedPacket) {
        if (CodeEditor.currentFile == script) {
            CodeEditor.editor.setErrorMarkers(emptyMap())
            if (Minecraft.getInstance().screen is CodeEditorGui) {
                (Minecraft.getInstance().screen as CodeEditorGui).onClose()
            }
            player.sendSystemMessage(
                "[HollowEngine] ".mcText.colored(0xEBA434) +
                        "Скрипт успешно запущен!".mcText.colored(0x34eb34)
            )
        }
    }
}

@Serializable
class ScriptError(
    val severity: Severity,
    val message: String,
    val source: String,
    val line: Int,
    val column: Int,
)