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

package ru.hollowhorizon.hollowengine.client.screen.widget.button;

import com.mojang.blaze3d.vertex.PoseStack;
import kotlin.Unit;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.hollowhorizon.hc.client.screens.widget.button.BaseButton;
import ru.hollowhorizon.hollowengine.client.render.GUIHelper;

import javax.annotation.Nonnull;

public class IconHollowButton extends BaseButton {
    private final ResourceLocation icon;

    public IconHollowButton(int x, int y, int width, int height, Component text, Runnable onPress, ResourceLocation texture, ResourceLocation icon) {
        super(x, y, width, height, text, (data) -> {
            onPress.run();
            return Unit.INSTANCE;
        }, texture);
        this.icon = icon;
    }

    @Override
    public void render(@Nonnull PoseStack stack, int x, int y, float f) {
        super.render(stack, x, y, f);
        stack.pushPose();
        float color = isCursorAtButton(x, y) ? 0.7F : 1.0F;
        GUIHelper.drawIcon(stack, icon, this.x + getWidth() - getHeight(), this.y, getHeight(), getHeight(), 0.7F, color, color, color, 1.0F);
        stack.popPose();
    }
}
