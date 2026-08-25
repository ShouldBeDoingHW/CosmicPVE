package com.cosmicpve.equipment.mask;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.data.component.MaskLoadout;
import com.cosmicpve.data.component.MaskPresentation;
import com.cosmicpve.equipment.EquipmentTooltipService;
import com.cosmicpve.registry.ModDataComponents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.junit.jupiter.api.Test;

class MaskEquipmentTooltipTest {
    @Test void protectedMaskedHelmetShowsCompactIdentityImmediatelyBeforeRemovalAndCapacity() {
        var helmet=new ItemStack(Items.DIAMOND_HELMET);
        helmet.set(ModDataComponents.CUSTOM_ENCHANT_META.get(),new CustomEnchantMetadata(1,5,0,true,false));
        var presentations=List.of(presentation("zeus",0x11AAFF,"Hidden Zeus effect"),
                presentation("santa",0xFF2222,"Hidden Santa effect"));
        helmet.set(ModDataComponents.MASK_LOADOUT.get(),new MaskLoadout(
                presentations.stream().map(MaskPresentation::id).toList(),MaskProfiles.MULTI_TEXTURE,presentations));
        var lines=new ArrayList<Component>(); lines.add(Component.literal("Helmet"));
        new EquipmentTooltipService().onTooltip(new ItemTooltipEvent(helmet,null,lines,TooltipFlag.NORMAL,Item.TooltipContext.EMPTY));
        int protectedLine=indexContaining(lines,"tooltip.cosmicpve.protected");
        assertTrue(lines.get(protectedLine+1).getString().startsWith("tooltip.cosmicpve.mask.attached_prefix"));
        assertEquals("tooltip.cosmicpve.mask.remove",lines.get(protectedLine+2).getString());
        assertTrue(lines.get(protectedLine+3).getString().contains("Enchantment Slots"));
        assertFalse(lines.stream().anyMatch(line -> line.getString().contains("Hidden")));
        var siblings=lines.get(protectedLine+1).getSiblings();
        assertTrue(siblings.stream().anyMatch(component -> "Zeus".equals(component.getString())
                && component.getStyle().getColor().getValue()==0x11AAFF));
        assertTrue(siblings.stream().anyMatch(component -> "Santa".equals(component.getString())
                && component.getStyle().getColor().getValue()==0xFF2222));
    }
    private static int indexContaining(List<Component> lines,String value) {
        for (int i=0;i<lines.size();i++) if (lines.get(i).getString().contains(value)) return i;
        return -1;
    }
    private static MaskPresentation presentation(String id,int color,String effect) {
        return new MaskPresentation(CosmicPVE.id(id),Component.literal(Character.toUpperCase(id.charAt(0))+id.substring(1)),
                Component.literal(effect),color);
    }
}
