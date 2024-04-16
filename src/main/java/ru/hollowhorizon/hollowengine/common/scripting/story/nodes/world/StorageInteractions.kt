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

package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.world

import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.server.ServerLifecycleHooks
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.npcs.GiveItemList

class StorageItemListNode(itemList: StorageItemList.() -> Unit) : Node() {
    val itemList by lazy { StorageItemList().apply(itemList) }
    var isStarted = false

    override fun tick(): Boolean {
        val block = itemList.level.getBlockEntity(itemList.bpos) ?: return true
        block.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent { itemHandler ->
            if(itemList.consumeItems) {
                itemList.items.removeIf { stack ->
                    for (i in 0 until itemHandler.slots) {
                        val item = itemHandler.getStackInSlot(i)
                        if (stack.item == item.item) {
                            val remaining = item.count
                            item.shrink(stack.count)
                            stack.shrink(remaining)
                            return@removeIf stack.isEmpty
                        }
                    }
                    false
                }
            } else {
                if(itemList.items.all { stack ->
                    var count = stack.count
                    for (i in 0 until itemHandler.slots) {
                        val item = itemHandler.getStackInSlot(i)
                        if (stack.item == item.item) {
                            count -= item.count
                        }
                    }
                    count <= 0
                }) itemList.items.clear()
            }
        }
        return itemList.items.isNotEmpty()
    }

    override fun serializeNBT() = CompoundTag().apply {
        put("items", ListTag().apply {
            addAll(itemList.items.map { it.save(CompoundTag()) })
        })
        putString("world", itemList.world)
        putDouble("x", itemList.pos.x)
        putDouble("y", itemList.pos.y)
        putDouble("z", itemList.pos.z)
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        itemList.items.clear()
        nbt.getList("items", 10).forEach {
            itemList.items.add(ItemStack.of(it as CompoundTag))
        }
        itemList.world = nbt.getString("world")
        itemList.pos = Vec3(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"))
    }
}

class StorageItemList : GiveItemList() {
    var world = "minecraft:overworld"
    var pos = Vec3.ZERO
    var consumeItems = true

    val bpos by lazy { BlockPos(pos) }
    val level by lazy {
        ServerLifecycleHooks.getCurrentServer().getLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, world.rl))
            ?: throw IllegalStateException("World $world not found")
    }
}

fun IContextBuilder.waitStorage(items: StorageItemList.() -> Unit) = +StorageItemListNode(items)