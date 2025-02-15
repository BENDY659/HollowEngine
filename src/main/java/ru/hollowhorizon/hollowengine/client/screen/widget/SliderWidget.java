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

package ru.hollowhorizon.hollowengine.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget;

import java.util.function.Consumer;

import static ru.hollowhorizon.hollowengine.HollowEngine.MODID;

public class SliderWidget extends HollowWidget {
    public static final ResourceLocation SLIDER_BASE = new ResourceLocation(MODID, "textures/gui/slider_base.png");
    private final Consumer<Boolean> consumer;
    private boolean value;
    private boolean processAnim;
    private int processCounter;

    public SliderWidget(int x, int y, int w, int h, Consumer<Boolean> consumer) {
        super(x, y, w, h, Component.empty());
        this.consumer = consumer;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float p_230430_4_) {
        this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        TextureManager manager = Minecraft.getInstance().textureManager;

        manager.bindForSetup(SLIDER_BASE);
        blit(stack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height * 3);

        stack.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, processCounter / 10F);

        manager.bindForSetup(SLIDER_BASE);
        blit(stack, this.x, this.y, 0, this.height * 2, this.width, this.height, this.width, this.height * 3);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1F);
        RenderSystem.disableBlend();
        stack.popPose();

        manager.bindForSetup(SLIDER_BASE);
        blit(stack, this.x + (int) (0.6667F * this.width * processCounter / 10F), this.y, 0, this.height, this.width, this.height, this.width, this.height * 3);

        if (this.processAnim) {
            if (this.value) {
                if (processCounter < 10) processCounter += 1;
                else this.processAnim = false;
            } else {
                if (processCounter > 0) processCounter -= 1;
                else this.processAnim = false;
            }
        }
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int button) {
        if (this.isHovered && button == 0) {
            this.value = !this.value;
            this.consumer.accept(this.value);
            this.processAnim = true;
            //Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(HSSounds.SLIDER_BUTTON, 1.0F));
            return true;
        }
        return false;
    }
}
