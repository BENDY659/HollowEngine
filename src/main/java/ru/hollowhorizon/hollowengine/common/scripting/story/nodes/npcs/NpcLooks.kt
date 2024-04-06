package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.npcs

import dev.ftb.mods.ftbteams.FTBTeamsAPI
import dev.ftb.mods.ftbteams.data.Team
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.next

class NpcLookToBlockNode(val npc: NPCProperty, var pos: () -> Vec3, var speed: Vec2 = Vec2(10f, 30f)) : Node() {
    private var ticks = 30

    override fun tick(): Boolean {
        if(!npc.isLoaded) return true
        val npc = npc()!!

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

class NpcLookToEntityNode(val npc: NPCProperty, var target: () -> Entity?, var speed: Vec2 = Vec2(10f, 30f)) :
    Node() {
    private var ticks = 30

    override fun tick(): Boolean {
        if(!npc.isLoaded) return true
        val npc = npc()!!

        val look = npc.lookControl

        look.setLookAt(target() ?: return true, speed.x, speed.y)

        return ticks-- > 0
    }

    override fun serializeNBT() = CompoundTag()

    override fun deserializeNBT(nbt: CompoundTag) {
    }
}

class NpcLookToTeamNode(val npc: NPCProperty, var target: () -> Team?, var speed: Vec2 = Vec2(10f, 30f)) : Node() {
    private var ticks = 30

    override fun tick(): Boolean {
        if(!npc.isLoaded) return true
        val npc = npc()!!

        val look = npc.lookControl

        val team = target()?.onlineMembers?.minByOrNull { it.distanceToSqr(npc) } ?: return true

        look.setLookAt(team, speed.x, speed.y)

        return ticks-- > 0
    }

    override fun serializeNBT() = CompoundTag().apply {
        val team = target() ?: return@apply

        putUUID("team", team.id)
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        val team = FTBTeamsAPI.getManager().getTeamByID(nbt.getUUID("team"))
        if (team == null) HollowCore.LOGGER.warn("Team ${nbt.getUUID("team")} not found!")
        target = { team }
    }
}

