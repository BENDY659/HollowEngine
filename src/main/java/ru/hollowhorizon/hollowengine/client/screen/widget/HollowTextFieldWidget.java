package ru.hollowhorizon.hollowengine.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class HollowTextFieldWidget extends EditBox {
    private final ResourceLocation texture;

    public HollowTextFieldWidget(Font fr, int x, int y, int w, int h, Component text, ResourceLocation texture, Consumer<String> consumer) {
        this(fr, x, y, w, h, text, texture);
        this.setResponder(consumer);
    }

    public HollowTextFieldWidget(Font fr, int x, int y, int w, int h, Component text, ResourceLocation texture) {
        super(fr, x, y, w - 10, h, text);
        setMaxLength(128);
        setValue(text.getString());
        this.texture = texture;
        setBordered(false);
    }

    @Override
    public void render(PoseStack stack, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        stack.pushPose();

        RenderSystem.setShaderTexture(0, texture);
        blit(stack, x - 5, y - this.height / 2 + 4, 0, this.isHoveredOrFocused() ? this.height : 0, this.width + 10, this.height, this.width + 10, this.height * 2);

        super.render(stack, p_230430_2_, p_230430_3_, p_230430_4_);
    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
    }

    @Override
    public int getWidth() {
        return super.getWidth() + 10;
    }
}
