package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.npcs

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hollowengine.common.capabilities.StoriesCapability
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.util.NpcContainer
import java.util.*
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class NpcDelegate(
    settings: () -> NpcContainer,
) : Node(), ReadOnlyProperty<Any?, NPCProperty> {
    val settings by lazy { settings() }
    var entityUUID: UUID? = null
    private val property = NPCProperty(npc = {
        val world = this.settings.world.rl
        val model = this.settings.model
        check(ResourceLocation.isValidResourceLocation(model)) { "Invalid model path: $model" }

        val dimension = manager.server.levelKeys().find { it.location() == world }
            ?: throw IllegalStateException("Dimension $world not found. Or not loaded!")
        val level = manager.server.getLevel(dimension)
            ?: throw IllegalStateException("Dimension $world not found. Or not loaded")

        level.getEntity(entityUUID ?: return@NPCProperty null) as NPCEntity
    })

    override fun init() {
        check(ResourceLocation.isValidResourceLocation(settings.model)) { "Invalid model path: ${settings.model}" }

        val dimension = manager.server.levelKeys().find { it.location() == settings.world.rl }
            ?: throw IllegalStateException("Dimension ${settings.world} not found. Or not loaded!")
        val level = manager.server.getLevel(dimension)
            ?: throw IllegalStateException("Dimension ${settings.world} not found. Or not loaded")

        if (entityUUID != null) return

        val entity = NPCEntity(level).apply {
            setPos(settings.pos.x, settings.pos.y, settings.pos.z)

            this[AnimatedEntityCapability::class].apply {
                model = settings.model
                animations.clear()
                animations.putAll(settings.animations)
                textures.clear()
                textures.putAll(settings.textures)
                transform = settings.transform
                switchHeadRot = settings.switchHeadRot
                subModels.clear()
                subModels.putAll(settings.subModels)
            }
            moveTo(settings.pos.x, settings.pos.y, settings.pos.z, settings.rotation.x, settings.rotation.y)

            settings.attributes.attributes.forEach { (name, value) ->
                getAttribute(ForgeRegistries.ATTRIBUTES.getValue(name.rl) ?: return@forEach)?.baseValue =
                    value.toDouble()
            }

            setDimensions(settings.size)
            refreshDimensions()

            isCustomNameVisible = settings.showName && settings.name.isNotEmpty()
            customName = settings.name.mcText

            level.addFreshEntity(this)
            level[StoriesCapability::class].activeNpcs[this.uuid.toString()] = settings.name
        }

        entityUUID = entity.uuid

        manager.scriptRequirements += {
            property.isLoaded || !level[StoriesCapability::class].activeNpcs.containsKey(entityUUID.toString())
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): NPCProperty {
        return this.property
    }

    override fun tick(): Boolean {


        return !property.isLoaded
    }

    override fun serializeNBT() = CompoundTag().apply {
        putUUID("entity", entityUUID ?: return@apply)
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        if (nbt.contains("entity")) entityUUID = nbt.getUUID("entity")
    }
}

class NPCFindContainer(var uuid: String = "", var name: String = "", var world: String = "minecraft:overworld")

class NPCFindDelegate(
    settings: () -> NPCFindContainer,
) : Node(), ReadOnlyProperty<Any?, NPCProperty> {
    private val settings by lazy { settings() }
    private val property = NPCProperty(npc = {
        val name = this.settings.name
        val world = this.settings.world.rl

        val dimension = manager.server.levelKeys().find { it.location() == world }
            ?: throw IllegalStateException("Dimension $world not found. Or not loaded!")
        val level = manager.server.getLevel(dimension)
            ?: throw IllegalStateException("Dimension $world not found. Or not loaded")

        val uuid = UUID.fromString(this.settings.uuid.ifEmpty {
            level[StoriesCapability::class].activeNpcs.entries.find { it.value == name }?.key
                ?: throw IllegalStateException("NPC with name \"$name\" not found!")
        })

        level.getEntity(uuid) as NPCEntity
    })

    override fun tick(): Boolean {
        return !property.isLoaded
    }

    override fun serializeNBT() = CompoundTag()

    override fun deserializeNBT(nbt: CompoundTag) {}

    override fun getValue(thisRef: Any?, property: KProperty<*>): NPCProperty {
        return this.property
    }
}