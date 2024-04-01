package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.util

import ru.hollowhorizon.hc.client.utils.rl

object PostEffect {
    val SEPIA = "hc:shaders/post/sepia.post.json".rl
    val GRAY = "hc:shaders/post/gray.post.json".rl
    val SHAKE = "hc:shaders/post/shake.post.json".rl
    val VIGNETTE = "hc:shaders/post/vig.post.json".rl
    val GLITCH = "hc:shaders/post/glitch.post.json".rl
    val BLUR = "shaders/post/blur.post.json".rl

    fun create(string: String) = string.rl
}