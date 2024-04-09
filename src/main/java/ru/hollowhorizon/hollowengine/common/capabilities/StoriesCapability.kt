package ru.hollowhorizon.hollowengine.common.capabilities

import kotlinx.serialization.Serializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.Level
import ru.hollowhorizon.hc.client.utils.nbt.ForCompoundNBT
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2

@HollowCapabilityV2(Level::class)
class StoriesCapability : CapabilityInstance() {
    val stories by syncableMap<String, Story>()
    val activeNpcs by syncableMap<String, String>() // UUID -> Name
}

@Serializable
class Story(val nbt: @Serializable(ForCompoundNBT::class) CompoundTag)