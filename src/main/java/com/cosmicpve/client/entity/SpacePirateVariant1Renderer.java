package com.cosmicpve.client.entity;

import com.cosmicpve.entity.spacepirate.SpacePirateDefinition;
import com.cosmicpve.entity.spacepirate.SpacePirateVariant1;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.piglin.ZombifiedPiglinModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;
import net.minecraft.resources.Identifier;

public final class SpacePirateVariant1Renderer extends HumanoidMobRenderer<SpacePirateVariant1,
        ZombifiedPiglinRenderState, ZombifiedPiglinModel> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/piglin/zombified_piglin.png");

    public SpacePirateVariant1Renderer(EntityRendererProvider.Context context) {
        super(context, new ZombifiedPiglinModel(context.bakeLayer(ModelLayers.ZOMBIFIED_PIGLIN)), 0.5F);
        addLayer(new HumanoidArmorLayer<>(this,
                ArmorModelSet.bake(ModelLayers.ZOMBIFIED_PIGLIN_ARMOR, context.getModelSet(), ZombifiedPiglinModel::new),
                context.getEquipmentRenderer()));
    }

    @Override public Identifier getTextureLocation(ZombifiedPiglinRenderState state) { return TEXTURE; }
    @Override public ZombifiedPiglinRenderState createRenderState() { return new ZombifiedPiglinRenderState(); }
    @Override public void extractRenderState(SpacePirateVariant1 entity, ZombifiedPiglinRenderState state, float tick) {
        super.extractRenderState(entity, state, tick);
        state.isAggressive = entity.isAggressive();
    }
    @Override protected void scale(ZombifiedPiglinRenderState state, PoseStack pose) {
        pose.scale(SpacePirateDefinition.SCALE, SpacePirateDefinition.SCALE, SpacePirateDefinition.SCALE);
    }
}
