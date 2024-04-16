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

package ru.hollowhorizon.hollowengine.cutscenes.replay

import kotlinx.serialization.Serializable
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.common.util.FakePlayer
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.client.utils.nbt.ForBlockPos
import ru.hollowhorizon.hc.client.utils.rl


@Serializable
class ReplayBlock(
    val pos: @Serializable(ForBlockPos::class) BlockPos,
    val block: String,
) {
    fun place(level: Level, target: LivingEntity, fakePlayer: FakePlayer) {
        val block =
            ForgeRegistries.BLOCKS.getValue(block.rl) ?: throw IllegalArgumentException("Block $block not found")
        val blockItem = block.asItem()

        if (blockItem is BlockItem) {
            blockItem.useOn(
                UseOnContext(
                    fakePlayer,
                    target.usedItemHand,
                    BlockHitResult(target.lookAngle, target.direction, pos, false)
                )
            )
        } else {
            level.setBlockAndUpdate(pos, block.defaultBlockState())

            if (block is DoorBlock) {
                block.setPlacedBy(level, pos, block.defaultBlockState(), null, ItemStack.EMPTY)
            }
        }
    }

    fun placeWorld(level: Level) {
        val block =
            ForgeRegistries.BLOCKS.getValue(block.rl) ?: throw IllegalArgumentException("Block $block not found")
        level.setBlockAndUpdate(pos, block.defaultBlockState())
    }

    fun destroy(fakePlayer: FakePlayer) {
        val manager = fakePlayer.gameMode

        manager.destroyBlock(pos)
    }

    fun destroyWorld(level: Level) {
        level.destroyBlock(pos, false)
    }

    fun use(level: Level, target: LivingEntity, fakePlayer: FakePlayer) {
        val manager = fakePlayer.gameMode

        manager.useItemOn(
            fakePlayer,
            level,
            target.useItem,
            target.usedItemHand,
            BlockHitResult(target.lookAngle, target.direction, pos, false)
        )
    }
}