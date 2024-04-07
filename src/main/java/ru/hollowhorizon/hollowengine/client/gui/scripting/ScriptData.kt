package ru.hollowhorizon.hollowengine.client.gui.scripting

import imgui.type.ImBoolean

data class ScriptData(
    val name: String,
    var code: String,
    val path: String,
    val open: ImBoolean
)