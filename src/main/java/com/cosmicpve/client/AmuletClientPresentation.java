package com.cosmicpve.client;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.equipment.accessory.AccessoryResolver;
import com.cosmicpve.equipment.accessory.AmuletItemFactory;
import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;

/** Dedicated torso-following player layer; it overlays rather than replacing chest armor. */
public final class AmuletClientPresentation {
    static final ContextKey<ItemStackRenderState> AMULET_STATE = new ContextKey<>(CosmicPVE.id("amulet_render_state"));
    private static final AccessoryResolver ACCESSORIES = new AccessoryResolver();
    private AmuletClientPresentation() {}

    public static void registerState(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<AvatarRenderer<?>>() {},
                (LivingEntity avatar, AvatarRenderState state) -> ACCESSORIES.equippedAmulet(avatar).ifPresent(definition -> {
                    var itemState = new ItemStackRenderState();
                    Minecraft.getInstance().getItemModelResolver().updateForLiving(
                            itemState, AmuletItemFactory.create(definition), ItemDisplayContext.FIXED, avatar);
                    state.setRenderData(AMULET_STATE, itemState);
                }));
    }

    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) event.getPlayerRenderer(skin).addLayer(
                new AmuletLayer(event.getPlayerRenderer(skin)));
    }

    static final class AmuletLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
        AmuletLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) { super(parent); }
        @Override public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                AvatarRenderState state, float yRot, float xRot) {
            var itemState = state.getRenderData(AMULET_STATE);
            if (itemState == null || itemState.isEmpty() || state.isInvisible) return;
            poseStack.pushPose();
            getParentModel().body.translateAndRotate(poseStack);
            itemState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
