package ru.hollowhorizon.hollowengine.mixins;

import kotlin.Unit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hollowengine.client.gui.ExampleGui;
import ru.hollowhorizon.hollowengine.client.gui.scripting.CodeEditorGui;
import ru.hollowhorizon.hollowengine.client.screen.widget.ScaleableButton;

@Mixin(PauseScreen.class)
public class PauseScreenMixin extends Screen {
    protected PauseScreenMixin(Component pTitle) {
        super(pTitle);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        addRenderableWidget(new ScaleableButton(5, 5, 20, 20, "hollowengine:textures/gui/hollowengine.png", "HollowEngine: Scripting", button -> {
            Minecraft.getInstance().setScreen(new CodeEditorGui());
            return Unit.INSTANCE;
        }));
    }
}
