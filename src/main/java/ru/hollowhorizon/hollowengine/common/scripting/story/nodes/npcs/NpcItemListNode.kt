package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.npcs

import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.utils.mcTranslate
import ru.hollowhorizon.hollowengine.client.screen.overlays.DrawMousePacket
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node

open class GiveItemList {
    val items = mutableListOf<ItemStack>()
    var text = "hollowengine.npc_need"

    operator fun ItemStack.unaryPlus() {
        items.add(this)
    }
}

fun IContextBuilder.collectItems(block: GiveItemList.() -> Unit) = +ItemListNode(block)


class ItemListNode(itemList: GiveItemList.() -> Unit) : Node() {
    private val itemList by lazy { GiveItemList().apply(itemList) }

    override fun tick(): Boolean {
        var hasItems = false

        manager.team.onlineMembers.forEach { member ->
            hasItems = hasItems ||
                    itemList.items.all { item ->
                        member.inventory.items.any { it.isItemStackEqual(item) }
                    }
        }

        return !hasItems
    }

    override fun serializeNBT() = CompoundTag()
    override fun deserializeNBT(nbt: CompoundTag) {}
}

private fun ItemStack.isItemStackEqual(it: ItemStack): Boolean {
    if (this.item != it.item) return false
    if (this.count != it.count) return false
    return true
}

class NpcItemListNode(itemList: GiveItemList.() -> Unit, npcConsumer: NPCProperty) : Node() {
    val npc by lazy { npcConsumer() }
    val itemList by lazy { GiveItemList().apply(itemList) }
    var isStarted = false

    override fun tick(): Boolean {
        if (!isStarted) {
            isStarted = true
            npc.shouldGetItem = { entityItem ->
                val item = itemList.items.find { it.item == entityItem.item }

                if (item != null) {
                    val remaining = item.count
                    item.shrink(entityItem.count)
                    if (item.isEmpty) {
                        itemList.items.remove(item)
                        entityItem.shrink(remaining)
                    }
                }
                itemList.items.any { entityItem.item == it.item }
            }
            DrawMousePacket(enable = true, onlyOnNpc = true).send(*manager.team.onlineMembers.toTypedArray())
            npc.onInteract = { player ->
                player.sendSystemMessage(itemList.text.mcTranslate)
                itemList.items.forEach {
                    player.sendSystemMessage(Component.literal("- ").append(it.displayName).append(" x${it.count}"))
                }
            }
        }
        val hasItems = itemList.items.isNotEmpty()
        if (!hasItems) {
            DrawMousePacket(enable = false, onlyOnNpc = false).send(*manager.team.onlineMembers.toTypedArray())
            npc.shouldGetItem = { false }
            npc.onInteract = NPCEntity.EMPTY_INTERACT
        }
        return hasItems
    }

    override fun serializeNBT() = CompoundTag().apply {
        put("items", ListTag().apply {
            addAll(itemList.items.map { it.save(CompoundTag()) })
        })
    }

    override fun deserializeNBT(nbt: CompoundTag) {
        itemList.items.clear()
        nbt.getList("items", 10).forEach {
            itemList.items.add(ItemStack.of(it as CompoundTag))
        }
    }
}