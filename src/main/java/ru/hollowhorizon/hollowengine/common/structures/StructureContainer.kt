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

package ru.hollowhorizon.hollowengine.common.structures

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.structure.Structure
import ru.hollowhorizon.hc.client.utils.rl
import kotlin.random.Random

val STRUCTURES = HashMap<ResourceLocation, StructureContainer>()

class StructureContainer {
    var settings: SpawnSettings = SpawnSettings { true }
    var spawnMode = SpawnMode.SURFACE
    var yOffset = 0
    var minSizeY = 10
}

fun interface SpawnSettings {
    fun check(context: Structure.GenerationContext): Boolean
}

infix fun SpawnSettings.and(other: SpawnSettings) = SpawnSettings {
    this@and.check(it) && other.check(it)
}

infix fun SpawnSettings.or(other: SpawnSettings) = SpawnSettings {
    this@or.check(it) || other.check(it)
}

class StructureHeight(var height: Short, val lower: Boolean = true) : SpawnSettings {
    override fun check(context: Structure.GenerationContext): Boolean {
        val chunkPos = context.chunkPos

        val x = (chunkPos.x shl 4) + 7
        val z = (chunkPos.z shl 4) + 7
        val y = context.chunkGenerator().getFirstOccupiedHeight(
            x,
            z,
            Heightmap.Types.WORLD_SURFACE_WG,
            context.heightAccessor,
            context.randomState()
        )
        return y > height
    }

}

class StructureDimension(var dimension: String) : SpawnSettings {
    override fun check(context: Structure.GenerationContext): Boolean {
        return false
    }

}

class StructureBiome(var biome: String) : SpawnSettings {
    override fun check(context: Structure.GenerationContext): Boolean {
        return context.biomeSource.possibleBiomes().any { it.`is`(biome.rl) }
    }

}

class SpawnChance(var chance: Float) : SpawnSettings {
    override fun check(context: Structure.GenerationContext): Boolean {
        return Random.nextFloat() > 1f - chance
    }

}


enum class SpawnMode {
    AIR, UNDERGROUND, SURFACE
}