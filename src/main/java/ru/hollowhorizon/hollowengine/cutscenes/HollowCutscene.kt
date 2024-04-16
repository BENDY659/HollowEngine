/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hollowengine.cutscenes

import kotlinx.serialization.Serializable
import net.minecraft.world.level.Level
import ru.hollowhorizon.hollowengine.cutscenes.actor.SceneActor
import ru.hollowhorizon.hollowengine.cutscenes.camera.SceneCamera
import ru.hollowhorizon.hollowengine.cutscenes.custom.SceneScript
import ru.hollowhorizon.hollowengine.cutscenes.world.SceneWorld

@Serializable
class HollowCutscene {
    val sceneActors = mutableListOf<SceneActor>()
    val sceneCamera = SceneCamera()
    val sceneWorld = SceneWorld()
    val sceneScript = SceneScript()

    var index = 0
    val maxIndex: Int
        get() = maxOf(sceneCamera.maxIndex(), sceneWorld.maxIndex(), sceneScript.maxIndex())

    fun init(level: Level) {
        sceneActors.forEach { it.init(level) }
    }

    fun update() {
        sceneActors.forEach { it.update(index) }
        if (++index > maxIndex) index = 0
    }
}