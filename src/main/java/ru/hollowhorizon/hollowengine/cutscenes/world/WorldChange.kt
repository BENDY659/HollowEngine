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

@file:UseSerializers(ForBlockPos::class)

package ru.hollowhorizon.hollowengine.cutscenes.world

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import net.minecraft.core.BlockPos
import ru.hollowhorizon.hc.client.utils.nbt.ForBlockPos

@Serializable
class WorldChange {
    var blockChanges = mutableMapOf<Int, BlockChange>()
    var timeChanges = mutableMapOf<Int, Long>()
    var weatherChanges = mutableMapOf<Int, WeatherChange>()

    @Serializable
    class BlockChange(val block: String, val pos: BlockPos, val state: ChangeType) {
        enum class ChangeType {
            ADD, REMOVE, USE
        }
    }

    @Serializable
    class WeatherChange(val type: WeatherType) {
        enum class WeatherType {
            CLEAR, RAIN, THUNDER
        }
    }
}
