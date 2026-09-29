package com.cosmicpve.client;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.equipment.accessory.AccessoryResolver;
import com.cosmicpve.equipment.accessory.BeltDefinition;
import com.cosmicpve.equipment.accessory.BeltItemFactory;
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

/** Body-local waist overlay for authored belt models. */
public final class BeltClientPresentation {
    // LivingEntityRenderer has already flipped X/Y for HumanoidModel coordinates:
    // body Y=0 is the shoulders and Y=12 pixels is the waist (positive down-body).
    static final double BODY_TO_WAIST = 12.0 / 16.0;
    static final ContextKey<ItemStackRenderState> BELT_STATE = new ContextKey<>(CosmicPVE.id("belt_render_state"));
    private static final AccessoryResolver ACCESSORIES = new AccessoryResolver();
    private BeltClientPresentation() {}
    public static void registerState(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<AvatarRenderer<?>>() {}, (LivingEntity avatar, AvatarRenderState state) ->
                ACCESSORIES.equippedBelt(avatar).filter(value -> value != BeltDefinition.CINDERWOLF).ifPresent(definition -> {
                    var itemState = new ItemStackRenderState();
                    Minecraft.getInstance().getItemModelResolver().updateForLiving(
                            itemState, BeltItemFactory.create(definition), ItemDisplayContext.FIXED, avatar);
                    state.setRenderData(BELT_STATE, itemState);
                }));
    }
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) event.getPlayerRenderer(skin).addLayer(new BeltLayer(event.getPlayerRenderer(skin)));
    }
    static final class BeltLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
        BeltLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) { super(parent); }
        @Override public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light,
                AvatarRenderState state, float yRot, float xRot) {
            var itemState = state.getRenderData(BELT_STATE);
            if (itemState == null || itemState.isEmpty() || state.isInvisible) return;
            poseStack.pushPose();
            getParentModel().body.translateAndRotate(poseStack);
            poseStack.translate(0.0, BODY_TO_WAIST, 0.0);
            itemState.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}
