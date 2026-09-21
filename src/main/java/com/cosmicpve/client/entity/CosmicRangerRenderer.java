package com.cosmicpve.client.entity;

import com.cosmicpve.entity.woodlands.CosmicRangerEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.BoggedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;

public final class CosmicRangerRenderer extends BoggedRenderer {
    public CosmicRangerRenderer(EntityRendererProvider.Context context){super(context);}
    @Override protected void scale(BoggedRenderState state,PoseStack pose){
        super.scale(state,pose);pose.scale(CosmicRangerEntity.SCALE,CosmicRangerEntity.SCALE,CosmicRangerEntity.SCALE);
    }
}
