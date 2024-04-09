package ru.hollowhorizon.hollowengine.common.npcs

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.util.INBTSerializable
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity

class NpcTarget(val level: Level) : INBTSerializable<CompoundTag> {
    var movingPos: Vec3? = null
    var movingEntity: Entity? = null

    var lookingPos: Vec3? = null
    var lookingEntity: Entity? = null

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