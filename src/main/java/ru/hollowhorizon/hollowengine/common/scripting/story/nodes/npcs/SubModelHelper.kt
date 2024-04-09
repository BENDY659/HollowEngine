package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.npcs

import net.minecraft.server.level.ServerPlayer
import ru.hollowhorizon.hc.client.models.gltf.Transform
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.SubModel
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity
import ru.hollowhorizon.hollowengine.common.util.Safe

fun subModel(body: SubModel.() -> Unit): SubModel {
    return SubModel(
        "hollowengine:models/entity/player_model.gltf",
        ArrayList(), HashMap(), Transform(), HashMap()
    ).apply(body)
}

val Safe<NPCEntity>.asSubModel: SubModel
    get() {
        val npc = this()
        val original = npc[AnimatedEntityCapability::class]
        return subModel {
            model = original.model
            layers.addAll(original.layers)
            textures.putAll(original.textures)
            transform = original.transform
            subModels.putAll(original.subModels)
        }
    }

val Safe<NPCEntity>.subModels get() = this()[AnimatedEntityCapability::class].subModels