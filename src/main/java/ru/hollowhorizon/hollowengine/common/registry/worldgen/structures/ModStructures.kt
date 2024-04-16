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

package ru.hollowhorizon.hollowengine.common.registry.worldgen.structures

import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.data.BuiltinRegistries
import net.minecraft.tags.TagKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.GenerationStep
import net.minecraft.world.level.levelgen.structure.Structure
import net.minecraft.world.level.levelgen.structure.StructureSet
import net.minecraft.world.level.levelgen.structure.StructureType
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.RegistryObject
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hollowengine.common.structures.ScriptedStructure
import ru.hollowhorizon.hollowengine.common.structures.StructureContainer
import kotlin.random.Random


object ModStructures {
    val STRUCTURES: DeferredRegister<Structure> = DeferredRegister.create(Registry.STRUCTURE_REGISTRY, "hollowengine")
    val STRUCTURE_TYPES: DeferredRegister<StructureType<*>> =
        DeferredRegister.create(Registry.STRUCTURE_TYPE_REGISTRY, "hollowengine")


    val TYPE: RegistryObject<StructureType<*>> =
        STRUCTURE_TYPES.register("hollow_structure_type") { StructureType { ScriptedStructure.CODEC } }

    fun addStructure(location: String, builder: StructureContainer.() -> Unit = {  }) =
        STRUCTURES.register(location) {
            ScriptedStructure(
                createSettings(ModBiomeTags.HOLLOW_STRUCTURE),
                "hollowengine:$location".rl
            )
        }.apply {
            ru.hollowhorizon.hollowengine.common.structures.STRUCTURES["hollowengine:$location".rl] = StructureContainer().apply(builder)
            ModStructureSets.STRUCTURE_SETS.register(
                location + "_set"
            ) {
                StructureSet(
                    this.holder.get() as Holder<Structure>,
                    RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, Random.nextInt(99999999))
                )
            }
        }


    fun createSettings(location: TagKey<Biome>) = Structure.StructureSettings(
        BuiltinRegistries.BIOME.getOrCreateTag(
            location
        ),
        mapOf(),
        GenerationStep.Decoration.SURFACE_STRUCTURES,
        TerrainAdjustment.BEARD_THIN
    )

}