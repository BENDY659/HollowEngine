package ru.hollowhorizon.hollowengine.common.capabilities

import kotlinx.serialization.Serializable
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.client.utils.nbt.ForResourceLocation
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2

@HollowCapabilityV2(Player::class)
class PlayerStoryCapability : CapabilityInstance() {
    val aimMarks by syncableList<AimMark>()
    val quests by syncableList<String>()
}

@Serializable
class AimMark(
    val x: Double,
    val y: Double,
    val z: Double,
    val icon: @Serializable(ForResourceLocation::class) ResourceLocation,
    val ignoreY: Boolean
)