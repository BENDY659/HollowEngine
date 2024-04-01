package ru.hollowhorizon.hollowengine.common.items

import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import ru.hollowhorizon.hc.client.utils.nbt.ForVec3
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hollowengine.client.gui.NPCCreatorGui
import ru.hollowhorizon.hollowengine.client.screen.npcs.ModelEditScreen
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity
import ru.hollowhorizon.hollowengine.common.tabs.HOLLOWENGINE_TAB

class NpcTool : Item(Properties().tab(HOLLOWENGINE_TAB).stacksTo(1)) {
    override fun interactLivingEntity(
        pStack: ItemStack,
        pPlayer: Player,
        pInteractionTarget: LivingEntity,
        pUsedHand: InteractionHand,
    ): InteractionResult {
        if (pUsedHand == InteractionHand.MAIN_HAND && pPlayer.level.isClientSide && pInteractionTarget is NPCEntity) {
            Minecraft.getInstance().setScreen(ModelEditScreen(pInteractionTarget))
        }

        return super.interactLivingEntity(pStack, pPlayer, pInteractionTarget, pUsedHand)
    }

    override fun use(pLevel: Level, pPlayer: Player, pUsedHand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (!pLevel.isClientSide && pUsedHand == InteractionHand.MAIN_HAND) {
            val result = pPlayer.pick(5.0, 0f, false)

            if (result is EntityHitResult) return super.use(pLevel, pPlayer, pUsedHand)

            val pos = result.location

            OpenEditorScreen(pos).send(pPlayer as ServerPlayer)
        }

        return super.use(pLevel, pPlayer, pUsedHand)
    }
}

@HollowPacketV2
@Serializable
class OpenEditorScreen(val pos: @Serializable(ForVec3::class) Vec3) : HollowPacketV3<OpenEditorScreen> {
    override fun handle(player: Player, data: OpenEditorScreen) {
        Minecraft.getInstance().setScreen(NPCCreatorGui(pos.x, pos.y, pos.z))
    }

}