package ru.hollowhorizon.hollowengine.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import dev.ftb.mods.ftbteams.FTBTeamsAPI
import imgui.ImGui
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImBoolean
import imgui.type.ImDouble
import imgui.type.ImString
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import ru.hollowhorizon.hc.client.imgui.ImguiHandler
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.nbt.ForVec3
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hollowengine.client.gui.widget.ComboFilterState
import ru.hollowhorizon.hollowengine.client.gui.widget.ImInputCombo
import ru.hollowhorizon.hollowengine.common.commands.listModels
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity
import ru.hollowhorizon.hollowengine.common.npcs.NPCCapability

class NPCCreatorGui(x: Double, y: Double, z: Double) : HollowScreen() {
    private val state = ComboFilterState()
    val npcName = ImString().apply {
        set(
            arrayOf(
                "Элдриан",
                "Лунара",
                "Зефир",
                "Аурелиан",
                "Нираллия",
                "Гриммар",
                "Лирель",
                "Силвана",
                "Фэйт",
                "Эландор",
                "Изабель",
                "Торган",
                "Амаранта",
                "Финнрод",
                "Элисия",
                "Лордран",
                "Алевтина",
                "Северин",
                "Эвелина",
                "Дракондор",
                "Иллирия",
                "Варгрим",
                "Феодора",
                "Леонин",
                "Иридия",
                "Таэлин",
                "Ксантия",
                "Лиандор",
                "Ясмина",
                "Верендил",
                "Аэлара",
                "Ренгар",
                "Эмберлин",
                "Гвиндор",
                "Лиллиана",
                "Азариэль",
                "Лавиния",
                "Моргрим",
                "Селеста",
                "Эльрик",
                "Лунис",
                "Изольда",
                "Фэррон",
                "Элинор",
                "Дариан",
                "Нефелия",
                "Стормгард",
                "Сарафина",
                "Галадриэль",
                "Демитриус"
            ).random()
        )
    }
    val npcModel = ImString().apply {
        set("hollowengine:models/entity/player_model.gltf")
    }
    val npcWorld = ImString().apply {
        set(Minecraft.getInstance().level?.dimension()?.location() ?: "minecraft:overworld")
    }
    val npcX = ImDouble().apply { set(x) }
    val npcY = ImDouble().apply { set(y) }
    val npcZ = ImDouble().apply { set(z) }
    val showName = ImBoolean().apply { set(true) }

    val isOpen = ImBoolean().apply { set(false) }

    override fun render(pPoseStack: PoseStack, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick)
        ImguiHandler.drawFrame {
            val window = Minecraft.getInstance().window
            ImGui.setNextWindowPos(0f, 0f)
            ImGui.setNextWindowSize(window.width.toFloat(), window.height.toFloat())
            ImGui.begin(
                "Редактор Персонажей",
                ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.HorizontalScrollbar or
                        ImGuiWindowFlags.NoCollapse
            )
            ImGui.pushItemWidth(400f)
            ImGui.inputText("Имя персонажа", npcName)
            ImInputCombo.inputCombo("Модель персонажа", npcModel, state, *listModels().toTypedArray())

            ImGui.inputText("Мир", npcWorld)
            ImGui.popItemWidth()

            ImGui.pushItemWidth(120f)
            ImGui.inputDouble("x", npcX)
            ImGui.sameLine()
            ImGui.inputDouble("y", npcY)
            ImGui.sameLine()
            ImGui.inputDouble("z", npcZ)
            ImGui.checkbox("Показывать имя", showName)
            if (ImGui.button("Создать")) {
                onClose()
                NPCCreatorPacket(
                    npcName.get(),
                    npcModel.get(),
                    npcWorld.get(),
                    Vec3(npcX.get(), npcY.get(), npcZ.get()),
                    showName.get()
                ).send()
            }
            ImGui.popItemWidth()
            ImGui.end()
        }
    }
}

@HollowPacketV2
@Serializable
class NPCCreatorPacket(
    val name: String,
    val model: String,
    val world: String,
    val pos: @Serializable(ForVec3::class) Vec3,
    val showName: Boolean,
) : HollowPacketV3<NPCCreatorPacket> {
    override fun handle(player: Player, data: NPCCreatorPacket) {
        val entity = NPCEntity(player.level).apply {
            setPos(pos)
            level.addFreshEntity(this)
        }

        entity[AnimatedEntityCapability::class].apply {
            model = this@NPCCreatorPacket.model
        }
        entity[NPCCapability::class].teamUUID = FTBTeamsAPI.getPlayerTeam(player as ServerPlayer).id.toString()
        entity.moveTo(pos.x, pos.y, pos.z, player.yHeadRot, player.xRot)

        entity.isCustomNameVisible = this@NPCCreatorPacket.showName && this@NPCCreatorPacket.name.isNotEmpty()
        entity.customName = this@NPCCreatorPacket.name.mcText

    }

}