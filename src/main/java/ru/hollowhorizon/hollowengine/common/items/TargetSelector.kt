package ru.hollowhorizon.hollowengine.common.items

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.PacketDistributor
import ru.hollowhorizon.hollowengine.client.utils.roundTo
import ru.hollowhorizon.hollowengine.common.network.CopyTextPacket
import ru.hollowhorizon.hollowengine.common.tabs.HOLLOWENGINE_TAB

class TargetSelector : Item(Properties().stacksTo(1).tab(HOLLOWENGINE_TAB)) {
    val targets = arrayListOf<Vec3>()
    override fun use(pLevel: Level, pPlayer: Player, pUsedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (!pLevel.isClientSide && pUsedHand == InteractionHand.MAIN_HAND) {
            val result = pPlayer.pick(5.0, 0f, false)

            if (result is EntityHitResult) return super.use(pLevel, pPlayer, pUsedHand)

            if(!pPlayer.isShiftKeyDown) {
                targets.add(result.location)
            } else {
                var npc = "npc"

                val item = pPlayer.mainHandItem
                if(item.hasCustomHoverName()) {
                    npc = item.hoverName.string
                }

                val command = targets.joinToString(separator = "\n") { "$npc moveTo { pos(${it.x.roundTo(2)}, ${it.y.roundTo(2)}, ${it.z.roundTo(2)}) }" }

                CopyTextPacket(command).send(PacketDistributor.PLAYER.with {pPlayer as ServerPlayer})

                targets.clear()
            }
        }

        return super.use(pLevel, pPlayer, pUsedHand)
    }
}