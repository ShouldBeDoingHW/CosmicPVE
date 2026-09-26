package com.cosmicpve.client.entity;

import com.cosmicpve.CosmicPVE;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.resources.Identifier;

public final class CinderWolfRenderer extends WolfRenderer {
    public static final Identifier TEXTURE = CosmicPVE.id("textures/entity/cinder_wolf.png");
    private final float scale;
    public CinderWolfRenderer(EntityRendererProvider.Context context, float scale) {
        super(context); this.scale = scale;
    }
    @Override public Identifier getTextureLocation(WolfRenderState state) { return TEXTURE; }
    @Override protected void scale(WolfRenderState state, PoseStack pose) {
        super.scale(state, pose);
        pose.scale(scale, scale, scale);
    }
}
