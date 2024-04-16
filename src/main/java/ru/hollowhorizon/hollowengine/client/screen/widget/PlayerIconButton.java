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

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class PlayerIconButton extends IconButton {
    protected final ResourceLocation skinLocation;

    public PlayerIconButton(
            int x, int y, GameProfile profile, OnPress press, OnTooltip tooltip, Component title) {
        super(x, y, 20, 20, BLANK_BUTTON_TEXTURE, 0, 0, 64, 32, 20, press, tooltip, title);
        final SkinManager skinManager = Minecraft.getInstance().getSkinManager();
        final Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map =
                skinManager.getInsecureSkinInformation(profile);
        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
            skinLocation = skinManager.registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
        } else {
            skinLocation = AbstractClientPlayer.getSkinLocation(profile.getName());
            AbstractClientPlayer.registerSkinTexture(skinLocation, profile.getName());
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static void blitFace(PoseStack poseStack, int x, int y, int size) {
        blit(poseStack, x, y, size, size, 8.0F, 8.0F, 8, 8, 64, 64);
        blit(poseStack, x, y, size, size, 40.0F, 8.0F, 8, 8, 64, 64);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        final int yTex = yTexStart + (yDiffText * this.getYImage(this.isHoveredOrFocused()));
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        blit(poseStack, x, y, xTexStart, yTex, width, height, textureWidth, textureHeight);
        RenderSystem.setShaderTexture(0, skinLocation);
        RenderSystem.setShaderColor(0.3F, 0.3F, 0.3F, alpha);
        blitFace(poseStack, x + 4, y + 4, 13);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        blitFace(poseStack, x + 3, y + 3, 13);
        if (this.isHoveredOrFocused()) {
            this.renderToolTip(poseStack, mouseX, mouseY);
        }
    }
}