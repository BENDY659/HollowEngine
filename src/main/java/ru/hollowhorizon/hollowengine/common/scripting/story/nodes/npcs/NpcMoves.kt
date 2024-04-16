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