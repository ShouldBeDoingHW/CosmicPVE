package com.cosmicpve.client.entity;

import com.cosmicpve.entity.undeadcorpse.UndeadCorpseEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;

public final class UndeadCorpseRenderer extends AbstractZombieRenderer<UndeadCorpseEntity, ZombieRenderState,
        ZombieModel<ZombieRenderState>> {
    public UndeadCorpseRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_BABY)),
                ArmorModelSet.bake(ModelLayers.ZOMBIE_ARMOR, context.getModelSet(), ZombieModel::new),
                ArmorModelSet.bake(ModelLayers.ZOMBIE_BABY_ARMOR, context.getModelSet(), ZombieModel::new));
    }
    @Override public ZombieRenderState createRenderState() { return new ZombieRenderState(); }
    @Override protected void scale(ZombieRenderState state, PoseStack pose) {
        pose.scale(UndeadCorpseEntity.RENDER_SCALE, UndeadCorpseEntity.RENDER_SCALE, UndeadCorpseEntity.RENDER_SCALE);
    }
}
