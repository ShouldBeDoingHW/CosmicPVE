package com.cosmicpve.equipment.mask;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.ContentSnapshot;
import com.cosmicpve.content.CosmicContentRepository;
import com.cosmicpve.content.definition.mask.MaskBehavior;
import com.cosmicpve.content.definition.mask.MaskDefinition;
import com.cosmicpve.content.validation.ValidationResult;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.registry.ModDataComponents;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class MaskApplicationServiceTest {
    @Test void singleAndFiveMaskRoundTripsPreserveExactHelmetData() {
        var content=content(); var service=new MaskApplicationService(content);
        for (var ids : List.of(List.of(CosmicPVE.id("santa")), List.of(CosmicPVE.id("santa"),CosmicPVE.id("party"),
                CosmicPVE.id("dragon"),CosmicPVE.id("lover"),CosmicPVE.id("zeus")))) {
            ItemStack mask=MaskItemFactory.create(ids,content);
            ItemStack helmet=new ItemStack(Items.DIAMOND_HELMET);
            helmet.setDamageValue(73); helmet.set(DataComponents.CUSTOM_NAME,Component.literal("Preserved Helmet"));
            var meta=new CustomEnchantMetadata(1,5,2,true,true);
            helmet.set(ModDataComponents.CUSTOM_ENCHANT_META.get(),meta);
            assertEquals(MaskApplicationService.ApplyOutcome.SUCCESS,service.apply(mask,helmet,mask,helmet));
            assertTrue(mask.isEmpty()); assertEquals(ids,helmet.get(ModDataComponents.MASK_LOADOUT.get()).maskIds());
            assertEquals(73,helmet.getDamageValue()); assertEquals("Preserved Helmet",helmet.getHoverName().getString());
            assertEquals(meta,helmet.get(ModDataComponents.CUSTOM_ENCHANT_META.get()));
            var removed=service.remove(helmet,helmet,true);
            assertEquals(MaskApplicationService.RemoveOutcome.SUCCESS,removed.outcome());
            assertEquals(ids,removed.returnedMask().get(ModDataComponents.MASK_ITEM.get()).maskIds());
            assertFalse(helmet.has(ModDataComponents.MASK_LOADOUT.get())); assertEquals(73,helmet.getDamageValue());
            assertEquals(meta,helmet.get(ModDataComponents.CUSTOM_ENCHANT_META.get()));
        }
    }

    @Test void staleAlreadyMaskedAndNonHelmetApplicationsAreAtomic() {
        var content=content(); var service=new MaskApplicationService(content);
        ItemStack mask=MaskItemFactory.create(List.of(CosmicPVE.id("santa")),content);
        ItemStack sword=new ItemStack(Items.DIAMOND_SWORD);
        assertEquals(MaskApplicationService.ApplyOutcome.NOT_HELMET,service.apply(mask,sword,mask,sword));
        assertEquals(1,mask.getCount());
        ItemStack helmet=new ItemStack(Items.IRON_HELMET);
        assertEquals(MaskApplicationService.ApplyOutcome.STALE,service.apply(mask,helmet,mask.copy(),helmet));
        assertEquals(MaskApplicationService.ApplyOutcome.SUCCESS,service.apply(mask,helmet,mask,helmet));
        ItemStack second=MaskItemFactory.create(List.of(CosmicPVE.id("party")),content);
        assertEquals(MaskApplicationService.ApplyOutcome.ALREADY_MASKED,service.apply(second,helmet,second,helmet));
        assertEquals(1,second.getCount());
    }

    private static CosmicContentRepository content() {
        var definitions=new LinkedHashMap<net.minecraft.resources.Identifier,MaskDefinition>();
        int color=0;
        for (String id : List.of("santa","party","dragon","lover","zeus")) {
            String texture=id.equals("santa") ? MaskProfiles.MULTI_TEXTURE : MaskProfiles.MULTI_TEXTURE;
            definitions.put(CosmicPVE.id(id),new MaskDefinition(CosmicPVE.id(id),Component.literal(id),
                    Component.literal("effect"),++color,texture,MaskBehavior.valueOf(id.toUpperCase(java.util.Locale.ROOT))));
        }
        var repository=new CosmicContentRepository();
        assertTrue(repository.publish(ValidationResult.success(new ContentSnapshot(0,Map.of(),Map.of(),Map.of(),Map.of(),Map.of(),definitions))));
        return repository;
    }
}
