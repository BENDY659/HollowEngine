package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.npcs

import ru.hollowhorizon.hollowengine.common.entities.NPCEntity
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.await

class NPCProperty(val npc: () -> NPCEntity?) : () -> NPCEntity? by npc {
    val isLoaded get() = npc() != null

    companion object
}
