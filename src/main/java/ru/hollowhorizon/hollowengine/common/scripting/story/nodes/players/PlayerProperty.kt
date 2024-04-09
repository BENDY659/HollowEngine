package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.players

import net.minecraft.server.level.ServerPlayer

class PlayerProperty(var value: ServerPlayer? = null) : () -> ServerPlayer {
    val isLoaded get() = value != null

    override fun invoke() = value!!
}

