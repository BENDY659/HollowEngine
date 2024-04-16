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

@file:UseSerializers(ForVector3d::class, ForVector3f::class)

package ru.hollowhorizon.hollowengine.cutscenes.camera

import com.mojang.math.Vector3d
import com.mojang.math.Vector3f
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import ru.hollowhorizon.hc.client.utils.nbt.ForVector3d
import ru.hollowhorizon.hc.client.utils.nbt.ForVector3f

@Serializable
class SceneCamera {
    private val cameraTransform = mutableMapOf<Int, Pair<Vector3d, Vector3f>>()
    private val guiOverlay = mutableMapOf<Int, GuiOverlay>()

    fun maxIndex(): Int {
        val cameraMax = cameraTransform.keys.maxOrNull() ?: 0
        val guiMax = guiOverlay.keys.maxOrNull() ?: 0
        return maxOf(cameraMax, guiMax)
    }
}