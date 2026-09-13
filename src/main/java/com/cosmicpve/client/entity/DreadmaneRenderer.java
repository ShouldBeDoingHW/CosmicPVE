package com.cosmicpve.client.entity;

import com.cosmicpve.entity.woodlands.DreadmaneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.resources.Identifier;

public final class DreadmaneRenderer extends AbstractHorseRenderer<DreadmaneEntity, EquineRenderState, HorseModel> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/horse/horse_zombie.png");
    public DreadmaneRenderer(EntityRendererProvider.Context context) {
        super(context, new HorseModel(context.bakeLayer(ModelLayers.ZOMBIE_HORSE)),
                new HorseModel(context.bakeLayer(ModelLayers.ZOMBIE_HORSE_BABY)));
    }
    @Override public Identifier getTextureLocation(EquineRenderState state) { return TEXTURE; }
    @Override public EquineRenderState createRenderState() { return new EquineRenderState(); }
    @Override protected void scale(EquineRenderState state, PoseStack pose) {
        pose.scale(DreadmaneEntity.RENDER_SCALE, DreadmaneEntity.RENDER_SCALE, DreadmaneEntity.RENDER_SCALE);
    }
}
