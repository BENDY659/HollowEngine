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

@file:UseSerializers(ForResourceLocation::class)

package ru.hollowhorizon.hollowengine.cutscenes.camera

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.client.utils.nbt.ForResourceLocation

@Serializable
class GuiOverlay {
    val labels = arrayListOf<Label>()
    val images = arrayListOf<Image>()

    @Serializable
    data class Label(
        val text: String,
        val x: Int,
        val y: Int,
        val size: Float,
        val color: Int,
        val align: TextAlign = TextAlign.LEFT,
    ) {
        enum class TextAlign(val value: Float) {
            LEFT(0f), CENTER(0.5f), RIGHT(1f)
        }
    }

    @Serializable
    data class Image(
        val location: ResourceLocation,
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val blend: Boolean = false,
    )
}
