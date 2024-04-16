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

package ru.hollowhorizon.hollowengine.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import ru.hollowhorizon.hc.client.screens.HollowScreen;
import ru.hollowhorizon.hc.client.utils.ScissorUtil;
import ru.hollowhorizon.hollowengine.client.screen.widget.ModelEditWidget;
import ru.hollowhorizon.hollowengine.client.screen.widget.ModelPreviewWidget;

public class SMDEditModelScreen extends HollowScreen {
    private final NPCModelChoicerScreen lastScreen;
    private final NPCCreationScreen npcScreen;
    private ModelPreviewWidget preview;

    protected SMDEditModelScreen(NPCModelChoicerScreen lastScreen, NPCCreationScreen screen) {
        super(Component.literal("EDIT_MODEL_SCREEN"));
        this.lastScreen = lastScreen;
        this.npcScreen = screen;

        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
    }

    @Override
    protected void init() {

        this.addRenderableWidget(new ModelEditWidget(0, 0, this.width / 2, this.height, this.lastScreen, this.npcScreen));
        //this.preview = new ModelPreviewWidget(this.npcScreen, this.width / 2, 0, this.width / 2, this.height, new NPCEntity(new NPCSettings(), Minecraft.getInstance().level), this::renderWidgetTooltip);
        this.addRenderableWidget(this.preview);
    }


    public void renderWidgetTooltip(AbstractWidget widget, PoseStack stack, int mouseX, int mouseY) {
        this.renderTooltip(stack, widget.getMessage(), mouseX, mouseY);

    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(stack);

        preview.render(stack, mouseX, mouseY, partialTicks);
        final float fullscreenness = preview.getFullscreenness();
        if (fullscreenness < 1.0F) {
            final boolean shouldScissor = fullscreenness > 0.0F;
            if (shouldScissor) {
                ScissorUtil.INSTANCE.push(0, 0, width - preview.getWidth(), height);
            }

            super.render(stack, mouseX, mouseY, partialTicks);

            if (shouldScissor) {
                ScissorUtil.INSTANCE.pop();
            }
        }
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        if (this.preview.getFullscreenness() > 0.9F) {
            return this.preview.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
        }
        return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
    }

    @Override
    public boolean mouseReleased(double p_231048_1_, double p_231048_3_, int p_231048_5_) {
        return super.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_);
    }

    @Override
    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
    }

    @Override
    public boolean keyReleased(int p_223281_1_, int p_223281_2_, int p_223281_3_) {
        this.children().forEach(iGuiEventListener -> iGuiEventListener.keyReleased(p_223281_1_, p_223281_2_, p_223281_3_));
        return super.keyReleased(p_223281_1_, p_223281_2_, p_223281_3_);
    }

    @Override
    public void removed() {
        super.removed();
        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(false);
    }
}
