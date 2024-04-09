package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.npcs

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node
import ru.hollowhorizon.hollowengine.common.util.Safe
import kotlin.math.abs
import kotlin.math.sqrt

open class NpcMoveToBlockNode(val npc: Safe<NPCEntity>, var pos: () -> Vec3) : Node() {
    val block by lazy { pos() }

    override fun tick(): Boolean {
        if (!npc.isLoaded) return true
        val npc = npc()

        val navigator = npc.navigation

        navigator.moveTo(navigator.createPath(block.x, block.y, block.z, 0), 1.0)

        val dist = npc.distanceToXZ(block) > 1

        if (!dist) navigator.stop()

        return dist || abs(npc.y - block.y) > 3
    }

    override fun serializeNBT() = CompoundTag().apply {
        putDouble("pos_x", block.x)
        putDouble("pos_y", block.y)
        putDouble("pos_z", block.z)
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        pos = { Vec3(nbt.getDouble("pos_x"), nbt.getDouble("pos_y"), nbt.getDouble("pos_z")) }
    }
}

class NpcMoveToEntityNode(val npc: Safe<NPCEntity>, var target: () -> Entity?) : Node() {

    override fun tick(): Boolean {
        if (!npc.isLoaded) return true
        val npc = npc()
        val navigator = npc.navigation
        val entity = target()
        navigator.moveTo(entity ?: return true, 1.0)

        val dist = npc.distanceToXZ(entity) > 1.5

        if (!dist) navigator.stop()

        return dist || abs(npc.y - entity.y) > 3
    }

    override fun serializeNBT() = CompoundTag().apply {
        val entity = target() ?: return@apply
        putString("level", entity.level.dimension().location().toString())
        putUUID("target", entity.uuid)
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        val level =
            manager.server.getLevel(manager.server.levelKeys().find { it.location() == nbt.getString("level").rl }
                ?: return) ?: return
        val entity = level.getEntity(nbt.getUUID("target")) ?: return
        target = { entity }
    }
}

class NpcMoveToGroupNode(val npc: Safe<NPCEntity>, var target: () -> List<Entity>) : Node() {

    override fun tick(): Boolean {
        if (!npc.isLoaded) return true
        val npc = npc()

        val navigator = npc.navigation
        val entity = target().minByOrNull { it.distanceTo(npc) } ?: return true

        navigator.moveTo(entity, 1.0)

        val dist = npc.distanceToXZ(entity) > 1.5

        if (!dist) navigator.stop()

        return dist || abs(npc.y - entity.y) > 3
    }

    override fun serializeNBT() = CompoundTag()
    override fun deserializeNBT(nbt: CompoundTag) {
        // Nothing to deserialize
    }
}


fun Entity.distanceToXZ(pos: Vec3) = sqrt((x - pos.x) * (x - pos.x) + (z - pos.z) * (z - pos.z))
fun Entity.distanceToXZ(npc: Entity) = distanceToXZ(npc.position())