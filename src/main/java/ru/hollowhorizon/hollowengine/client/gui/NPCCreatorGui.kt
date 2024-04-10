package ru.hollowhorizon.hollowengine.client.gui

import com.mojang.blaze3d.vertex.PoseStack
import imgui.ImGui
import imgui.flag.ImGuiInputTextFlags
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImBoolean
import imgui.type.ImDouble
import imgui.type.ImInt
import imgui.type.ImString
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import ru.hollowhorizon.hc.client.imgui.ImguiHandler
import ru.hollowhorizon.hc.client.models.gltf.Transform
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.nbt.ForVec3
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hollowengine.client.gui.widget.ComboFilterState
import ru.hollowhorizon.hollowengine.client.gui.widget.ImInputCombo
import ru.hollowhorizon.hollowengine.common.commands.listModels
import ru.hollowhorizon.hollowengine.common.entities.NPCEntity

class NPCCreatorGui(x: Double, y: Double, z: Double) : HollowScreen() {
    private val state = ComboFilterState()
    val npcName = ImString().apply {
        set(
            arrayOf(
                "Элдриан", "Лунара", "Зефир", "Аурелиан", "Нираллия",
                "Гриммар", "Лирель", "Силвана", "Фэйт", "Эландор",
                "Изабель", "Торган", "Амаранта", "Финнрод", "Элисия",
                "Лордран", "Алевтина", "Северин", "Эвелина", "Дракондор",
                "Иллирия", "Варгрим", "Феодора", "Леонин", "Иридия",
                "Таэлин", "Ксантия", "Лиандор", "Ясмина", "Верендил",
                "Аэлара", "Ренгар", "Эмберлин", "Гвиндор", "Лиллиана",
                "Азариэль", "Лавиния", "Моргрим", "Селеста", "Эльрик",
                "Лунис", "Изольда", "Фэррон", "Элинор", "Дариан",
                "Нефелия", "Стормгард", "Сарафина", "Галадриэль",
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
    var model = GltfManager.getOrCreate(npcModel.get().rl)
    val animations = HashMap<String, String>()
    val textures = HashMap<String, String>()
    val tX = floatArrayOf(0f)
    val tY = floatArrayOf(0f)
    val tZ = floatArrayOf(0f)
    val rX = floatArrayOf(0f)
    val rY = floatArrayOf(0f)
    val rZ = floatArrayOf(0f)
    val sX = floatArrayOf(1f)
    val sY = floatArrayOf(1f)
    val sZ = floatArrayOf(1f)

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
            if (ImGui.beginTabBar("##tabs")) {
                if (ImGui.beginTabItem("Основное")) {
                    drawGeneral()

                    val size = ImGui.calcTextSize("Создать")
                    ImGui.setCursorPos(
                        ImGui.getWindowWidth() - size.x - ImGui.getStyle().windowPaddingX * 2,
                        ImGui.getWindowHeight() - size.y - ImGui.getStyle().windowPaddingY * 2
                    )
                    if (ImGui.button("Создать")) {
                        onClose()
                        NPCCreatorPacket(
                            npcName.get(),
                            npcModel.get(),
                            npcWorld.get(),
                            Vec3(npcX.get(), npcY.get(), npcZ.get()),
                            showName.get(),
                            animations.map { AnimationType.valueOf(it.key) to it.value }.toMap(),
                            textures.filter { it.value.isNotEmpty() },
                            tX[0], tY[0], tZ[0], rX[0], rY[0], rZ[0], sX[0], sY[0], sZ[0]
                        ).send()
                    }

                    ImGui.endTabItem()
                }

                if (ImGui.beginTabItem("Анимации")) {
                    drawAnimations()
                    ImGui.endTabItem()
                }

                if (ImGui.beginTabItem("Текстуры")) {
                    drawTextures()
                    ImGui.endTabItem()
                }

                if (ImGui.beginTabItem("Дополнительное")) {
                    drawTransforms()
                    ImGui.endTabItem()
                }
                ImGui.endTabBar()
            }
            ImGui.end()
        }
    }

    private fun drawGeneral() {
        ImGui.pushItemWidth(400f)
        ImGui.inputText("Имя персонажа", npcName)
        if (ImInputCombo.inputCombo("Модель персонажа", npcModel, state, *listModels().toTypedArray())) {
            model = GltfManager.getOrCreate(npcModel.get().rl)
        }

        ImGui.inputText("Мир", npcWorld)
        ImGui.popItemWidth()

        ImGui.pushItemWidth(120f)
        ImGui.inputDouble("x", npcX)
        ImGui.sameLine()
        ImGui.inputDouble("y", npcY)
        ImGui.sameLine()
        ImGui.inputDouble("z", npcZ)
        ImGui.checkbox("Показывать имя", showName)
        ImGui.popItemWidth()
    }


    private fun drawAnimations() {
        val animationNames = model.animationPlayer.nameToAnimationMap.keys.toTypedArray()
        val animationTypes = model.animationPlayer.typeToAnimationMap
        var animationToChange: Pair<String, String>? = null

        for (anim in animationTypes.map { it.key.toString() to it.value.name }
            .sortedBy { AnimationType.valueOf(it.first).ordinal }) {
            val index = ImInt(
                animationNames.indexOf(
                    if (anim.first !in animations.keys) anim.second else animations[anim.first]
                )
            )
            if (ImGui.combo(anim.first.lowercase().capitalize() + " анимация", index, animationNames)) {
                animationToChange = anim.first to animationNames[index.get()]
            }
        }

        animationToChange?.let { animations[it.first] = it.second }
    }

    private fun drawTextures() {
        val textureNames = model.modelTree.materials.map { it.texture.path.toString() }

        for (texture in textureNames) {
            ImGui.text(texture)
            ImGui.sameLine()
            val text = ImString()
            text.set(textures.computeIfAbsent(texture) { "" })
            ImGui.pushID(texture)
            if (ImGui.inputText("", text, ImGuiInputTextFlags.NoUndoRedo)) {
                textures[texture] = text.get()
            }
            ImGui.popID()
        }
    }

    private fun drawTransforms() {
        ImGui.text("Перемещение")
        ImGui.separator()
        ImGui.pushItemWidth(120f)

        ImGui.pushID("T")
        ImGui.dragFloat("X", tX, 0.1f, -10f, 10f); ImGui.sameLine()
        ImGui.dragFloat("Y", tY, 0.1f, -10f, 10f); ImGui.sameLine()
        ImGui.dragFloat("Z", tZ, 0.1f, -10f, 10f)
        ImGui.popID()

        ImGui.text("Поворот")
        ImGui.separator()

        ImGui.pushID("R")
        ImGui.dragFloat("X", rX, 1f, -360f, 360f); ImGui.sameLine()
        ImGui.dragFloat("Y", rY, 1f, -360f, 360f); ImGui.sameLine()
        ImGui.dragFloat("Z", rZ, 1f, -360f, 360f)
        ImGui.popID()

        ImGui.text("Масштаб")
        ImGui.separator()

        ImGui.pushID("S")
        ImGui.dragFloat("X", sX, 0.1f, 0.01f, 10f); ImGui.sameLine()
        ImGui.dragFloat("Y", sY, 0.1f, 0.01f, 10f); ImGui.sameLine()
        ImGui.dragFloat("Z", sZ, 0.1f, 0.01f, 10f)
        ImGui.popID()

        ImGui.popItemWidth()
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
    val animations: Map<AnimationType, String>,
    val textures: Map<String, String>,
    val tX: Float, val tY: Float, val tZ: Float,
    val rX: Float, val rY: Float, val rZ: Float,
    val sX: Float, val sY: Float, val sZ: Float,
) : HollowPacketV3<NPCCreatorPacket> {
    override fun handle(player: Player, data: NPCCreatorPacket) {
        val entity = NPCEntity(player.level).apply {
            setPos(pos)
            level.addFreshEntity(this)
        }

        entity[AnimatedEntityCapability::class].apply {
            model = this@NPCCreatorPacket.model
            animations.putAll(this@NPCCreatorPacket.animations)
            textures.putAll(this@NPCCreatorPacket.textures)
            transform = Transform(tX, tY, tZ, rX, rY, rZ, sX, sY, sZ)
        }
        entity.moveTo(pos.x, pos.y, pos.z, player.yHeadRot, player.xRot)

        entity.isCustomNameVisible = this@NPCCreatorPacket.showName && this@NPCCreatorPacket.name.isNotEmpty()
        entity.customName = this@NPCCreatorPacket.name.mcText
    }

}