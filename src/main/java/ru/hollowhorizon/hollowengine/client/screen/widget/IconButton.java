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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static net.minecraft.client.gui.components.Button.NO_TOOLTIP;
import static ru.hollowhorizon.hollowengine.HollowEngine.MODID;

public class IconButton extends HollowButton {
    public static final ResourceLocation BLANK_BUTTON_TEXTURE =
            new ResourceLocation(MODID, "textures/gui/blank_button.png");
    protected static final TextureManager TEXTURE_MANAGER = Minecraft.getInstance().getTextureManager();
    protected final ResourceLocation resourceLocation;
    protected final int xTexStart;
    protected final int yTexStart;
    protected final int yDiffText;
    protected final int textureWidth;
    protected final int textureHeight;

    public IconButton(
            int x,
            int y,
            int width,
            int height,
            ResourceLocation resourceLocation,
            int xTexStart,
            int yTexStart,
            int textureHeight,
            int textureWidth,
            int yDiffText,
            Button.OnPress press) {
        this(
                x,
                y,
                width,
                height,
                resourceLocation,
                xTexStart,
                yTexStart,
                textureHeight,
                textureWidth,
                yDiffText,
                press,
                NO_TOOLTIP,
                Component.empty());
    }

    public IconButton(
            int x,
            int y,
            int width,
            int height,
            ResourceLocation resourceLocation,
            int xTexStart,
            int yTexStart,
            int textureHeight,
            int textureWidth,
            int yDiffText,
            Button.OnPress press,
            Button.OnTooltip tooltip,
            Component title) {
        super(x, y, width, height, title, press, tooltip);
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.yDiffText = yDiffText;
        this.resourceLocation = resourceLocation;
    }

    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        final int yTex = yTexStart + (yDiffText * this.getYImage(this.isHoveredOrFocused()));
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        blit(poseStack, x, y, xTexStart, yTex, width, height, textureWidth, textureHeight);
        if (this.isHoveredOrFocused()) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }
        RenderSystem.disableBlend();
    }

    protected int getYImage(boolean isHovered) {
        if (!active) {
            return 2;
        } else if (isHovered) {
            return 1;
        }
        return 0;
    }
}
