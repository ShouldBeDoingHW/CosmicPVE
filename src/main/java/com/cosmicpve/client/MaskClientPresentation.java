package com.cosmicpve.client;

import com.cosmicpve.equipment.mask.MaskProfiles;
import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SkullBlock;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import com.google.common.reflect.TypeToken;

/** Uses the vanilla player-head render layer over the underlying helmet armor model. */
public final class MaskClientPresentation {
    private MaskClientPresentation() {}
    public static void register(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(new TypeToken<AvatarRenderer<?>>() {},
                (LivingEntity avatar, AvatarRenderState state) -> {
            var loadout = avatar.getItemBySlot(EquipmentSlot.HEAD).get(ModDataComponents.MASK_LOADOUT.get());
            if (loadout == null || !loadout.valid() || loadout.renderTexture().isEmpty()) return;
            apply(loadout, state);
                });
    }
    static void apply(com.cosmicpve.data.component.MaskLoadout loadout, AvatarRenderState state) {
        state.headEquipment = ItemStack.EMPTY;
        state.wornHeadType = SkullBlock.Types.PLAYER;
        state.wornHeadProfile = MaskProfiles.profile(loadout.renderTexture().orElseThrow());
    }
}
