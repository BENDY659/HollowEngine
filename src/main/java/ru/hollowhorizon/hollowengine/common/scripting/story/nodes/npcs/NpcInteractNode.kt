package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.npcs

import net.minecraft.nbt.CompoundTag
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hollowengine.client.screen.overlays.DrawMousePacket
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity
import ru.hollowhorizon.hollowengine.common.network.MouseButton
import ru.hollowhorizon.hollowengine.common.npcs.NPCCapability
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node
import ru.hollowhorizon.hollowengine.common.util.Safe

class NpcInteractNode(val npc: Safe<NPCEntity>) : Node() {
    var hasInteracted = false
    var isStarted = false

    override fun tick(): Boolean {
        if(!npc.isLoaded) return true

        if (!isStarted) {
            isStarted = true
            DrawMousePacket(enable = true).send(*manager.server.playerList.players.toTypedArray())
            npc()[NPCCapability::class].mouseButton = MouseButton.RIGHT
            npc().onInteract = { player ->
                hasInteracted = true
            }
        }
        if (hasInteracted) {
            DrawMousePacket(enable = false).send(*manager.server.playerList.players.toTypedArray())
            npc()[NPCCapability::class].mouseButton = MouseButton.NONE
            npc().onInteract = NPCEntity.EMPTY_INTERACT
        }
        return !hasInteracted
    }

    override fun serializeNBT() = CompoundTag().apply {
        putBoolean("hasInteracted", hasInteracted)
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        hasInteracted = nbt.getBoolean("hasInteracted")
        isStarted = false
    }
}