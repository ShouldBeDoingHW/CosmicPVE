package com.cosmicpve.client.entity;

import com.cosmicpve.entity.spacepirate.SpacePirateDefinition;
import com.cosmicpve.entity.spacepirate.SpacePirateVariant2;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

public final class SpacePirateVariant2Renderer extends HumanoidMobRenderer<SpacePirateVariant2,
        SkeletonRenderState, SkeletonModel<SkeletonRenderState>> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png");

    public SpacePirateVariant2Renderer(EntityRendererProvider.Context context) {
        super(context, new SkeletonModel<>(context.bakeLayer(ModelLayers.WITHER_SKELETON)), 0.5F);
        addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ModelLayers.WITHER_SKELETON_ARMOR, context.getModelSet(), SkeletonModel::new),
                context.getEquipmentRenderer()));
    }

    @Override public Identifier getTextureLocation(SkeletonRenderState state) { return TEXTURE; }
    @Override public SkeletonRenderState createRenderState() { return new SkeletonRenderState(); }
    @Override public void extractRenderState(SpacePirateVariant2 entity, SkeletonRenderState state, float tick) {
        super.extractRenderState(entity, state, tick);
        state.isAggressive = entity.isAggressive();
        state.isShaking = false;
        state.isHoldingBow = false;
    }
    @Override protected void scale(SkeletonRenderState state, PoseStack pose) {
        pose.scale(SpacePirateDefinition.SCALE, SpacePirateDefinition.SCALE, SpacePirateDefinition.SCALE);
    }
}
