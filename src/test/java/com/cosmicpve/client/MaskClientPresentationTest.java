package com.cosmicpve.client;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.MaskLoadout;
import com.cosmicpve.equipment.mask.MaskProfiles;
import java.util.List;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.SkullBlock;
import org.junit.jupiter.api.Test;

class MaskClientPresentationTest {
    @Test void singleAndMultiMasksSuppressHelmetAndUsePlayerHeadRender() {
        for (int count=1;count<=5;count++) {
            var ids=java.util.stream.IntStream.range(0,count).mapToObj(i -> CosmicPVE.id("mask_"+i)).toList();
            var state=new AvatarRenderState(); state.headEquipment=new ItemStack(Items.DIAMOND_HELMET);
            MaskClientPresentation.apply(new MaskLoadout(ids,MaskProfiles.MULTI_TEXTURE),state);
            assertTrue(state.headEquipment.isEmpty()); assertEquals(SkullBlock.Types.PLAYER,state.wornHeadType);
            assertNotNull(state.wornHeadProfile);
        }
    }
}
