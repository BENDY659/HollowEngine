package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.npcs

import ru.hollowhorizon.hollowengine.common.entities.NPCEntity

class NPCProperty(val npc: () -> NPCEntity?) : () -> NPCEntity {
    val isLoaded get() = npc() != null

    override operator fun invoke() = npc()!!

    companion object
}
