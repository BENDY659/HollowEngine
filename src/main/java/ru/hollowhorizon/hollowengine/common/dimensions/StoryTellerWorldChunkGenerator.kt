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

package ru.hollowhorizon.hollowengine.common.dimensions

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.resources.RegistryOps
import net.minecraft.server.level.WorldGenRegion
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.NoiseColumn
import net.minecraft.world.level.StructureManager
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.biome.BiomeSource
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.level.levelgen.RandomState
import net.minecraft.world.level.levelgen.blending.Blender
import net.minecraft.world.level.levelgen.structure.StructureSet
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

class StoryTellerWorldChunkGenerator(structures: Registry<StructureSet>, biomeSource: BiomeSource) :
    ChunkGenerator(structures, Optional.empty(), biomeSource) {
    override fun codec() = CODEC
    override fun applyCarvers(
        pLevel: WorldGenRegion,
        pSeed: Long,
        pRandom: RandomState,
        pBiomeManager: BiomeManager,
        pStructureManager: StructureManager,
        pChunk: ChunkAccess,
        pStep: GenerationStep.Carving
    ) {}

    override fun buildSurface(
        pLevel: WorldGenRegion,
        pStructureManager: StructureManager,
        pRandom: RandomState,
        pChunk: ChunkAccess
    ) {}

    override fun spawnOriginalMobs(pLevel: WorldGenRegion) {}

    override fun getGenDepth(): Int = 384

    override fun fillFromNoise(
        pExecutor: Executor,
        pBlender: Blender,
        pRandom: RandomState,
        pStructureManager: StructureManager,
        pChunk: ChunkAccess
    ): CompletableFuture<ChunkAccess> {
        if (pChunk.pos.x == 0 && pChunk.pos.z == 0) {
            val heightmapOcean = pChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG)
            val heightmapSurface = pChunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG)
            pChunk.setBlockState(BlockPos(0, 49, 0), Blocks.DIAMOND_BLOCK.defaultBlockState(), false)
            heightmapOcean.update(0, 49, 0, Blocks.DIAMOND_BLOCK.defaultBlockState())
            heightmapSurface.update(0, 49, 0, Blocks.DIAMOND_BLOCK.defaultBlockState())
        }

        return CompletableFuture.completedFuture(pChunk)
    }

    override fun getSeaLevel(): Int = -63

    override fun getMinY(): Int = 0

    override fun getBaseHeight(
        pX: Int,
        pZ: Int,
        pType: Heightmap.Types,
        pLevel: LevelHeightAccessor,
        pRandom: RandomState
    ): Int {
        return pLevel.minBuildHeight
    }

    override fun getBaseColumn(pX: Int, pZ: Int, pHeight: LevelHeightAccessor, pRandom: RandomState): NoiseColumn {
        return NoiseColumn(0, arrayOf())
    }

    override fun addDebugScreenInfo(pInfo: MutableList<String>, pRandom: RandomState, pPos: BlockPos) {}

    companion object {
        val CODEC: Codec<StoryTellerWorldChunkGenerator> =
            RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<StoryTellerWorldChunkGenerator> ->
                instance.group(
                    RegistryOps.retrieveRegistry(Registry.STRUCTURE_SET_REGISTRY).forGetter { it.structureSets },
                    BiomeSource.CODEC.fieldOf("biome_source").forGetter { it.biomeSource }
                ).apply(instance, ::StoryTellerWorldChunkGenerator)
            }
    }
}