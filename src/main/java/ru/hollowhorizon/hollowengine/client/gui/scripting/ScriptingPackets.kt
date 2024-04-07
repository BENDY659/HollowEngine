package ru.hollowhorizon.hollowengine.client.gui.scripting

import dev.ftb.mods.ftbteams.FTBTeamsAPI
import imgui.type.ImBoolean
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.client.utils.mcText
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
            val file = data.path.fromReadablePath().readText()
            UpdateFilePacket(data.path, file).send(player as ServerPlayer)
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
            FTBTeamsAPI.getPlayerTeam(player.uuid)?.onlineMembers?.let {
                UpdateFilePacket(data.path, data.text).send(*it.toTypedArray())
            }
        }
    }

}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class RunScriptPacket(val path: String) : HollowPacketV3<RunScriptPacket> {
    override fun handle(player: Player, data: RunScriptPacket) {

        if (player.hasPermissions(2)) {
            val team = FTBTeamsAPI.getPlayerTeam(player.uuid)!!
            val server = player.server!!
            val file = data.path.fromReadablePath()

            runScript(server, team, file, true)
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
            val team = FTBTeamsAPI.getPlayerTeam(player.uuid)!!

            StoryHandler.stopEvent(team, path)
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
class ScriptCompiledPacket(val script: String) : HollowPacketV3<ScriptCompiledPacket> {
    override fun handle(player: Player, data: ScriptCompiledPacket) {
        if (CodeEditor.currentFile == script) {
            CodeEditor.editor.setErrorMarkers(emptyMap())
            if(Minecraft.getInstance().screen is CodeEditorGui) {
                (Minecraft.getInstance().screen as CodeEditorGui).onClose()
            }
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