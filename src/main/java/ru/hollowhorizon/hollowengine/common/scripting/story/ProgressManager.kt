package ru.hollowhorizon.hollowengine.common.scripting.story

import net.minecraft.server.level.ServerPlayer
import ru.hollowhorizon.hollowengine.common.util.Safe

class ProgressManager(val players: Safe<List<ServerPlayer>>)

val Safe<List<ServerPlayer>>.progressManager get() = ProgressManager(this)