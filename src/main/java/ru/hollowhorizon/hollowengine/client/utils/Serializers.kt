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

package ru.hollowhorizon.hollowengine.client.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.world.phys.Vec2
import ru.hollowhorizon.hc.HollowCore

object ForVec2 : KSerializer<Vec2> {
    override val descriptor = buildClassSerialDescriptor("vec2") {
        element("x", Float.serializer().descriptor)
        element("2", Float.serializer().descriptor)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): Vec2 {
        val dec = decoder.beginStructure(descriptor)

        var x = 0.0f
        var y = 0.0f
        var xExists = false
        var yExists = false
        var zExists = false
        if (dec.decodeSequentially()) {
            x = dec.decodeFloatElement(descriptor, 0)
            y = dec.decodeFloatElement(descriptor, 1)
            xExists = true
            yExists = true
            zExists = true
        } else {
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break@loop
                    0 -> {
                        x = dec.decodeFloatElement(descriptor, i)
                        xExists = true
                    }

                    1 -> {
                        y = dec.decodeFloatElement(descriptor, i)
                        yExists = true
                    }

                    else -> throw SerializationException("Unknown index $i")
                }
            }
        }


        dec.endStructure(descriptor)
        if (!xExists) x = missingField("x", "Vector3f") { 0.0f }
        if (!yExists) y = missingField("y", "Vector3f") { 0.0f }

        return Vec2(x, y)
    }

    override fun serialize(encoder: Encoder, value: Vec2) {
        val compositeOutput = encoder.beginStructure(descriptor)
        compositeOutput.encodeFloatElement(descriptor, 0, value.x)
        compositeOutput.encodeFloatElement(descriptor, 1, value.y)
        compositeOutput.endStructure(descriptor)
    }
}

private inline fun <T> missingField(missingField: String, deserializing: String, defaultValue: () -> T): T {
    HollowCore.LOGGER.warn("Missing $missingField while deserializing $deserializing")
    return defaultValue()
}