package com.cosmicpve.client.entity;

import com.cosmicpve.entity.woodlands.ForestFanaticEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.skeleton.BoggedModel;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;
import net.minecraft.resources.Identifier;

public final class ForestFanaticRenderer extends AbstractSkeletonRenderer<ForestFanaticEntity, BoggedRenderState> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/skeleton/bogged.png");
    private static final Identifier OVERLAY = Identifier.withDefaultNamespace("textures/entity/skeleton/bogged_overlay.png");
    public ForestFanaticRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.BOGGED_ARMOR, new BoggedModel(context.bakeLayer(ModelLayers.BOGGED)));
        addLayer(new SkeletonClothingLayer<>(this, context.getModelSet(), ModelLayers.BOGGED_OUTER_LAYER, OVERLAY));
    }
    @Override public Identifier getTextureLocation(BoggedRenderState state) { return TEXTURE; }
    @Override public BoggedRenderState createRenderState() { return new BoggedRenderState(); }
    @Override public void extractRenderState(ForestFanaticEntity entity, BoggedRenderState state, float tick) {
        super.extractRenderState(entity, state, tick); state.isSheared = false;
    }
    @Override protected void scale(BoggedRenderState state, PoseStack pose) {
        pose.scale(ForestFanaticEntity.RENDER_SCALE, ForestFanaticEntity.RENDER_SCALE, ForestFanaticEntity.RENDER_SCALE);
    }
}
