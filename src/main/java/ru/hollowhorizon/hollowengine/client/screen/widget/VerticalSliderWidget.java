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

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import ru.hollowhorizon.hc.client.utils.ScissorUtil;

import java.util.function.Consumer;

import static ru.hollowhorizon.hollowengine.client.screen.widget.ModelPreviewWidget.BORDER_WIDTH;

public class VerticalSliderWidget extends AbstractWidget {
    private final int maxHeight;
    private int yHeight;
    private boolean isClicked;
    private Consumer<Float> consumer;

    public VerticalSliderWidget(int x, int y, int w, int h) {
        super(x, y, w, h, Component.literal(""));

        this.maxHeight = this.height - 30;
        yHeight = this.y + 30;
    }

    public int clamp(int value) {
        return Mth.clamp(value, this.y + 15, this.y + this.height - 15);
    }

    @Override
    public void render(PoseStack stack, int p_230430_2_, int mouseY, float p_230430_4_) {
        if (isClicked) {
            yHeight = clamp(mouseY);
            this.consumer.accept(getScroll());
        }

        fill(stack, x, yHeight - 15, x + width, yHeight + 15, 0xFFFFFFFF);

        fill(stack, x, y, x + width, y + height, 0x66FFFFFF);

        ScissorUtil.INSTANCE.push(
                x + BORDER_WIDTH,
                y + BORDER_WIDTH,
                width - BORDER_WIDTH * 2,
                height - BORDER_WIDTH * 2);

        fill(stack, x, y, x + width, y + height, 0x33FFFFFF);

        ScissorUtil.INSTANCE.pop();
    }

    public float getScroll() {
        return (this.yHeight - this.y - 15) / (this.maxHeight + 0F);
    }

    public void setScroll(float modifier) {
        this.yHeight = clamp(this.y + (int) (this.maxHeight * modifier) + 15);
    }

    public void onValueChange(Consumer<Float> consumer) {
        this.consumer = consumer;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        System.out.println("cli: " + button);
        if (button == 0 && isHovered(mouseX, mouseY)) {
            isClicked = true;
            return true;
        }
        return false;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX > this.x && mouseX <= this.x + this.width && mouseY > this.y && mouseY < this.y + this.height;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        System.out.println("rel: " + button);
        if (button == 0) {
            isClicked = false;
            return true;
        }
        return false;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
