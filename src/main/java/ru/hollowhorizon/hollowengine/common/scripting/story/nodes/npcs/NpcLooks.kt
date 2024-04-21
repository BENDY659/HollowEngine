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
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node
import ru.hollowhorizon.hollowengine.common.util.Safe

class NpcLookToBlockNode(val npc: Safe<NPCEntity>, var pos: () -> Vec3, var speed: Vec2 = Vec2(10f, 30f)) : Node() {
    private var ticks = 30

    override fun tick(): Boolean {
        if (!npc.isLoaded) return true
        val npc = npc()

        val look = npc.lookControl

        val newPos = pos()

        look.setLookAt(newPos.x, newPos.y, newPos.z, speed.x, speed.y)

        return ticks-- > 0
    }

    override fun serializeNBT() = CompoundTag().apply {
        val newPos = pos()
        putDouble("pos_x", newPos.x)
        putDouble("pos_y", newPos.y)
        putDouble("pos_z", newPos.z)
        putFloat("speed_x", speed.x)
        putFloat("speed_y", speed.y)
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        pos = { Vec3(nbt.getDouble("pos_x"), nbt.getDouble("pos_y"), nbt.getDouble("pos_z")) }
        speed = Vec2(nbt.getFloat("speed_x"), nbt.getFloat("speed_y"))
    }
}

class NpcLookToEntityNode(val npc: Safe<NPCEntity>, var target: () -> Entity?, var speed: Vec2 = Vec2(10f, 30f)) :
    Node() {
    private var ticks = 30

    override fun tick(): Boolean {
        val npc = npc()

        val look = npc.lookControl

        look.setLookAt(target() ?: return true, speed.x, speed.y)

        return ticks-- > 0
    }

    override fun serializeNBT() = CompoundTag()

    override fun deserializeNBT(nbt: CompoundTag) {
    }
}

class NpcLookAtGroupNode(val npc: Safe<NPCEntity>, var target: () -> List<Entity>, var speed: Vec2 = Vec2(10f, 30f)) :
    Node() {
    private var ticks = 30
    override fun tick(): Boolean {
        val npc = npc()

        val look = npc.lookControl

        look.setLookAt(target().minByOrNull { it.distanceTo(npc) } ?: return true, speed.x, speed.y)

        return ticks-- > 0
    }

    override fun serializeNBT() = CompoundTag()
    override fun deserializeNBT(nbt: CompoundTag) {
        // Nothing to deserialize
    }
}

