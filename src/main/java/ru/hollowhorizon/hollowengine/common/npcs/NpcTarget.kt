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

package ru.hollowhorizon.hollowengine.common.npcs

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.util.INBTSerializable
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity

class NpcTarget(val level: Level) : INBTSerializable<CompoundTag> {
    var movingPos: Vec3? = null
    var movingEntity: Entity? = null
    var movingGroup: List<ServerPlayer>? = null
    var lookingPos: Vec3? = null
    var lookingEntity: Entity? = null
    var lookingGroup: List<ServerPlayer>? = null

    fun tick(entity: NPCEntity) {
        if (movingPos != null) {
            entity.navigation.moveTo(entity.navigation.createPath(BlockPos(movingPos!!), 0), 1.0)
        }
        if (lookingPos != null) entity.lookControl.setLookAt(lookingPos!!.x, lookingPos!!.y, lookingPos!!.z, 10f, 30f)

        if (this.movingEntity != null) entity.navigation.moveTo(this.movingEntity!!, 1.0)
        if (this.lookingEntity != null) {
            val eyes = lookingEntity!!.eyePosition
            entity.lookControl.setLookAt(eyes.x, eyes.y, eyes.z, 10f, 30f)
        }

        if (this.movingGroup != null) {
            val nearest = this.movingGroup!!.minByOrNull { entity.distanceTo(it) } ?: return
            entity.navigation.moveTo(nearest, 1.0)
        }
        if (this.lookingGroup != null) {
            val nearest = this.lookingGroup!!.minByOrNull { entity.distanceTo(it) } ?: return
            entity.lookControl.setLookAt(nearest.eyePosition.x, nearest.eyePosition.y, nearest.eyePosition.z, 10f, 30f)
        }
    }

    override fun serializeNBT() = CompoundTag().apply {
        if (movingPos != null) {
            putDouble("mpos_x", movingPos!!.x)
            putDouble("mpos_y", movingPos!!.y)
            putDouble("mpos_z", movingPos!!.z)
        }
        if (movingEntity != null) putUUID("mentity", movingEntity!!.uuid)

        if (lookingPos != null) {
            putDouble("lpos_x", lookingPos!!.x)
            putDouble("lpos_y", lookingPos!!.y)
            putDouble("lpos_z", lookingPos!!.z)
        }
        if (lookingEntity != null) putUUID("lentity", lookingEntity!!.uuid)
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        if (nbt.contains("mpos_x")) {
            movingPos = Vec3(
                nbt.getDouble("mpos_x"),
                nbt.getDouble("mpos_y"),
                nbt.getDouble("mpos_z")
            )
        }
        if (nbt.contains("mentity")) {
            val level = level as? ServerLevel ?: return
            movingEntity = level.getEntity(nbt.getUUID("mentity"))
        }

        if (nbt.contains("lpos_x")) {
            lookingPos = Vec3(
                nbt.getDouble("lpos_x"),
                nbt.getDouble("lpos_y"),
                nbt.getDouble("lpos_z")
            )
        }
        if (nbt.contains("lentity")) {
            val level = level as? ServerLevel ?: return
            lookingEntity = level.getEntity(nbt.getUUID("lentity"))
        }
    }

}