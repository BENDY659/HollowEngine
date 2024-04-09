package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.events

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraftforge.event.ServerChatEvent
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.ForgeEventNode
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.players.PlayerProperty
import ru.hollowhorizon.hollowengine.common.util.Safe
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

data class InputContainer(var message: String = "", var player: PlayerProperty = PlayerProperty(null))

class InputNode(vararg val values: String, val players: Safe<List<ServerPlayer>>) :
    ForgeEventNode<ServerChatEvent.Submitted>(ServerChatEvent.Submitted::class.java, { true }),
    ReadWriteProperty<Any?, InputContainer> {
    val container = InputContainer()
    var hasPlayer = false
    override val action = { event: ServerChatEvent.Submitted ->
        val player = event.player

        container.player.value = player
        hasPlayer = true
        container.message = event.message.string

        val isValidPlayer = player in players()

        isValidPlayer && (values.any { it.equals(event.message.string, ignoreCase = true) } || values.isEmpty())
    }

    override fun serializeNBT(): CompoundTag {
        return super.serializeNBT().apply {
            putString("message", container.message)
            if(hasPlayer) container.player.invoke().uuid.let { putUUID("player", it) }
        }
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        super.deserializeNBT(nbt)
        container.message = nbt.getString("message")
        container.player.value =
            nbt.getUUID("player").let { uuid -> players().firstOrNull { it.uuid == uuid } } as ServerPlayer
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): InputContainer {
        return container
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: InputContainer) {
        container.message = value.message
        container.player = value.player
    }
}