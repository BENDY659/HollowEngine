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

package ru.hollowhorizon.hollowengine.client

import com.mojang.blaze3d.platform.InputConstants
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.item.Item
import net.minecraftforge.client.event.InputEvent
import net.minecraftforge.client.event.RegisterKeyMappingsEvent
import net.minecraftforge.client.event.RenderGuiOverlayEvent
import net.minecraftforge.client.event.RenderPlayerEvent
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import org.lwjgl.glfw.GLFW
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.client.utils.mcTranslate
import ru.hollowhorizon.hc.client.utils.open
import ru.hollowhorizon.hollowengine.HollowEngine
import ru.hollowhorizon.hollowengine.client.render.PlayerRenderer
import ru.hollowhorizon.hollowengine.client.screen.ProgressManagerScreen
import ru.hollowhorizon.hollowengine.client.screen.overlays.MouseOverlay
import ru.hollowhorizon.hollowengine.client.screen.overlays.RecordingDriver
import ru.hollowhorizon.hollowengine.client.screen.recording.ModifyRecordingScreen
import ru.hollowhorizon.hollowengine.client.screen.recording.StartRecordingScreen
import ru.hollowhorizon.hollowengine.common.network.KeybindPacket
import ru.hollowhorizon.hollowengine.common.network.MouseButton
import ru.hollowhorizon.hollowengine.common.network.MouseClickedPacket
import ru.hollowhorizon.hollowengine.common.util.Keybind
import ru.hollowhorizon.hollowengine.cutscenes.replay.PauseRecordingPacket
import thedarkcolour.kotlinforforge.forge.MOD_BUS

object ClientEvents {
    const val HE_CATEGORY = "key.categories.hollowengine.keys"
    val OPEN_EVENT_LIST = KeyMapping(keyBindName("event_list"), GLFW.GLFW_KEY_GRAVE_ACCENT, HE_CATEGORY)
    val TOGGLE_RECORDING = KeyMapping(keyBindName("toggle_recording"), GLFW.GLFW_KEY_V, HE_CATEGORY)
    val canceledButtons = hashSetOf<MouseButton>()
    private val customTooltips = HashMap<Item, MutableList<Component>>()

    private fun keyBindName(name: String) = "key.${HollowEngine.MODID}.$name"

    fun addTooltip(item: Item, tooltip: Component) {
        customTooltips.computeIfAbsent(item) { ArrayList() }.add(tooltip)
    }


    fun resetClientScripting() {
        customTooltips.clear()
    }

    @JvmStatic
    fun renderOverlay(event: RenderGuiOverlayEvent.Post) {
        if (event.overlay != VanillaGuiOverlay.HOTBAR.type()) return

        val window = event.window
        val width = window.guiScaledWidth
        val height = window.guiScaledHeight
        MouseOverlay.draw(event.poseStack, width / 2, height / 2 + 16, event.partialTick)
        RecordingDriver.draw(event.poseStack, 10, 10, event.partialTick)
    }

    @JvmStatic
    fun onTooltipRender(event: ItemTooltipEvent) {
        val item = event.itemStack.item

        if (item in customTooltips) event.toolTip.addAll(customTooltips[item] ?: emptyList())
    }

    @JvmStatic
    fun onClicked(event: InputEvent.MouseButton.Pre) {
        if (event.action != 1) return

        if (event.button > 2) return
        val button = MouseButton.from(event.button)
        if (canceledButtons.isNotEmpty()) MouseClickedPacket(button).send()
        if (canceledButtons.removeIf { it.ordinal == button.ordinal }) event.isCanceled = true
    }

    @JvmStatic
    fun onKeyPressed(event: InputEvent.Key) {
        val key = InputConstants.getKey(
            event.key,
            event.scanCode
        )

//        if (event.key == GLFW.GLFW_KEY_M) {
//            Minecraft.getInstance().setScreen(ExampleGui())
//        }

        if (Minecraft.getInstance().screen != null) return

        if (OPEN_EVENT_LIST.isActiveAndMatches(key)) {
            Minecraft.getInstance().setScreen(ProgressManagerScreen())
        }


        if (TOGGLE_RECORDING.isActiveAndMatches(key) && event.action == 0) {
            val player = Minecraft.getInstance().player ?: return
            if (!player.hasPermissions(2)) player.sendSystemMessage("hollowengine.no_permissions".mcTranslate)
            else {
                if (RecordingDriver.enable || player[AnimatedEntityCapability::class].model != "%NO_MODEL%") {
                    RecordingDriver.enable = false
                    PauseRecordingPacket(false, null).send()
                    ModifyRecordingScreen().open()
                } else Minecraft.getInstance().setScreen(StartRecordingScreen())
            }
        }

        if (event.action == 0) KeybindPacket(Keybind.fromCode(event.key)).send()
    }

    @JvmStatic
    fun renderPlayer(event: RenderPlayerEvent.Pre) {
        PlayerRenderer.render(event)
    }

    fun initKeys() {
        MOD_BUS.addListener { event: RegisterKeyMappingsEvent ->
            event.register(OPEN_EVENT_LIST)
            event.register(TOGGLE_RECORDING)
        }
    }
}