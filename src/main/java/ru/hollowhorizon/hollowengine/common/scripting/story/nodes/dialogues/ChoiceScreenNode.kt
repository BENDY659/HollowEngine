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

package ru.hollowhorizon.hollowengine.common.scripting.story.nodes.dialogues

import kotlinx.serialization.Serializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.network.PacketDistributor
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hollowengine.client.screen.ChoiceScreen
import ru.hollowhorizon.hollowengine.common.scripting.players
import ru.hollowhorizon.hollowengine.common.scripting.story.StoryStateMachine
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.HasInnerNodes
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.IContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.Node
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.NodeContextBuilder
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.SimpleNode
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.deserializeNodes
import ru.hollowhorizon.hollowengine.common.scripting.story.nodes.base.serializeNodes

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class ChoiceScreenPacket(
    val choices: MutableList<String> = ArrayList(),
    var text: String = "",
    var background: String = "hollowengine:textures/gui/bg/choice.png",
    var textX: Int = 0,
    var textY: Int = 0,
    var buttonX: Int = 0,
    var buttonY: Int = 0,
    var entityX: Int = 0,
    var entityY: Int = 0,
    var entitySize: Float = 1.0f,
    var open: Boolean = true,
) : HollowPacketV3<ChoiceScreenPacket> {
    override fun handle(player: Player, data: ChoiceScreenPacket) {
        if (data.open) ChoiceScreen.open(data)
        else ChoiceScreen.onClose()
    }

}

class ChoiceContext(val stateMachine: StoryStateMachine) {
    val choices = LinkedHashMap<Component, List<Node>>()
    var text: String = ""
    var background: String = "hollowengine:textures/gui/bg/choice.png"
    var textX: Int = 0
    var textY: Int = 0
    var buttonX: Int = 0
    var buttonY: Int = 0
    var entityX: Int = 0
    var entityY: Int = 0
    var entitySize = 1.0f
    var open: Boolean = true

    operator fun String.invoke(tasks: NodeContextBuilder.() -> Unit) {
        choices[this.mcText] = NodeContextBuilder(stateMachine).apply(tasks).tasks
    }
}

class ChoiceScreenNode(val choiceContext: ChoiceContext) : Node(), HasInnerNodes {
    val choices = choiceContext.choices
    var index = 0
    var performedChoice: List<Node>? = null
    var performedChoiceIndex = 0
    var isStarted = false
    val isEnded get() = performedChoice != null && index >= (performedChoice?.size ?: 0)
    override val currentNode get() = performedChoice?.get(index) ?: SimpleNode {}

    override fun tick(): Boolean {
        if (!isStarted) {
            isStarted = true
            MinecraftForge.EVENT_BUS.register(this)
            manager.server.playerList.players.forEach {
                ChoiceScreenPacket(
                    choices.keys.map { it.string }.toMutableList(),
                    choiceContext.text,
                    choiceContext.background,
                    choiceContext.textX,
                    choiceContext.textY,
                    choiceContext.buttonX,
                    choiceContext.buttonY,
                    choiceContext.entityX,
                    choiceContext.entityY,
                    choiceContext.entitySize
                ).send(PacketDistributor.PLAYER.with { it })
            }
        }

        if ((performedChoice?.size ?: 0) > 0) {
            if (!performedChoice!![index].tick()) index++
        }

        return !isEnded
    }

    @SubscribeEvent
    fun onChoice(event: ApplyChoiceEvent) {

        performedChoice = choices.values.filterIndexed { index, _ -> index == event.choice }.firstOrNull()
        performedChoiceIndex = event.choice
        MinecraftForge.EVENT_BUS.unregister(this)
        manager.server.playerList.players.forEach {
            ChoiceScreenPacket(open = false).send(PacketDistributor.PLAYER.with { it })
        }
        index = 0

    }

    override fun serializeNBT() = CompoundTag().apply {
        putInt("index", index)
        putInt("pindex", performedChoiceIndex)
        if (performedChoice != null) serializeNodes("performedChoice", performedChoice!!)

    }

    override fun deserializeNBT(nbt: CompoundTag) {
        index = nbt.getInt("index")
        performedChoiceIndex = nbt.getInt("pindex")
        if (nbt.contains("performedChoice")) {
            performedChoice = choices.values.filterIndexed { index, _ -> index == performedChoiceIndex }.firstOrNull()
            if (performedChoice != null) nbt.deserializeNodes("performedChoice", performedChoice!!)
        }

    }
}

fun IContextBuilder.choices(context: ChoiceContext.() -> Unit) =
    +ChoiceScreenNode(ChoiceContext(stateMachine).apply(context))