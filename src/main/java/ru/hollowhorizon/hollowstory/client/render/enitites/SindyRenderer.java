package ru.hollowhorizon.hollowstory.client.render.enitites;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hollowstory.client.model.SindyModel;
import ru.hollowhorizon.hollowstory.client.model.ThomasModel;
import ru.hollowhorizon.hollowstory.common.entities.SindyNPC;
import ru.hollowhorizon.hollowstory.common.entities.ThomasNPC;

public class SindyRenderer extends MobRenderer<SindyNPC, SindyModel<SindyNPC>> {
    public SindyRenderer(EntityRendererManager entityRendererManager) {
        super(entityRendererManager, new SindyModel<>(), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(SindyNPC pEntity) {
        return new ResourceLocation("hollowstory:textures/entities/sindy.png");
    }
}
