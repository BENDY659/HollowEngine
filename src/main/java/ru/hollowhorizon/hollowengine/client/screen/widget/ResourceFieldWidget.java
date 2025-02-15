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
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.hollowhorizon.hollowengine.client.screen.widget.button.IconHollowButton;

import java.util.function.Consumer;

import static ru.hollowhorizon.hollowengine.HollowEngine.MODID;

public class ResourceFieldWidget extends HollowTextFieldWidget {

    private final IconHollowButton button;

    public ResourceFieldWidget(Font fr, int x, int y, int w, int h, ResourceLocation texture, Consumer<String> stringConsumer) {
        this(fr, x, y, w, h, texture);
        this.setResponder(stringConsumer);
    }

    public ResourceFieldWidget(Font fr, int x, int y, int w, int h, ResourceLocation texture) {
        super(fr, x, y, w, h, Component.literal(""), texture);
        this.button = new IconHollowButton(this.x + this.width - this.height, this.y, this.height, this.height, Component.empty(), () -> {
//            HollowJavaUtils.chooseFile(
//                    fileChooser -> fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Model File", "*.smd")),
//                    file -> {
//                        SEND_MODEL_FILE.sendToServer(file);
//                        this.setValue("hollow-story/models/" + file.getName());
//                    }
//
//            );
        }, new ResourceLocation(MODID, "textures/gui/text_field_mini.png"), new ResourceLocation(MODID, "textures/gui/folder.png"));
    }

    @Override
    public void render(PoseStack stack, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(stack, p_230430_2_, p_230430_3_, p_230430_4_);
        this.button.render(stack, p_230430_2_, p_230430_3_, p_230430_4_);
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        if (this.button.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_)) return true;
        return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
    }

    @Override
    public boolean mouseReleased(double p_231048_1_, double p_231048_3_, int p_231048_5_) {
        if (this.button.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_)) return true;
        return super.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_);
    }
}