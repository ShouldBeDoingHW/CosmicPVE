package com.cosmicpve.equipment.armor;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.component.ArmorSetIdentity;
import com.cosmicpve.equipment.EquipmentTooltipService;
import com.cosmicpve.registry.ModDataComponents;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class ArmorSetPresentationResolverTest {
    @Test void omniUsesSharedTintAndTooltipPresentationWithoutBecomingASetIdentity() {
        ItemStack omni = new ItemStack(Items.IRON_HELMET);
        omni.set(ModDataComponents.OMNI_ARMOR.get(), true);
        var presentation = ArmorSetPresentationResolver.resolve(omni).orElseThrow();
        assertTrue(presentation.omni());
        assertEquals("Omni", presentation.displayName().getString());
        assertEquals(0x061630, presentation.color());
        assertEquals(0x061630, ArmorSetPresentationResolver.color(omni).orElseThrow());
        assertNull(omni.get(ModDataComponents.ARMOR_SET_ID.get()));
        var lines = EquipmentTooltipService.armorSetPresentationLines(omni);
        assertEquals(2, lines.size());
        assertTrue(lines.getFirst().getString().isEmpty());
        assertIdentityArgument(lines.get(1), "Omni");
        assertEquals(0x061630, lines.get(1).getStyle().getColor().getValue());
    }

    @Test void genuineSetRetainsItsExistingPresentationAndBonusLines() {
        ItemStack ranger = new ItemStack(Items.IRON_HELMET);
        var identity = new ArmorSetIdentity(1, ArmorSetIds.RANGER, Component.literal("Ranger"), 0x4BA36E,
                List.of(Component.literal("Ranger bonus")));
        ranger.set(ModDataComponents.ARMOR_SET_ID.get(), identity);
        var presentation = ArmorSetPresentationResolver.resolve(ranger).orElseThrow();
        assertFalse(presentation.omni());
        assertEquals(identity.color(), presentation.color());
        assertEquals(identity.fullSetBonus(), presentation.fullSetBonus());
        var lines = EquipmentTooltipService.armorSetPresentationLines(ranger);
        assertEquals(4, lines.size());
        assertIdentityArgument(lines.get(1), "Ranger");
        assertEquals(0x4BA36E, lines.get(1).getStyle().getColor().getValue());
        assertEquals("Ranger bonus", lines.get(3).getString());
    }

    @Test void presentationDoesNotChangeAcceptedOmniGameplayResolution() {
        var genuine = new ArmorSetResolver.PieceIdentity(Optional.of(ArmorSetIds.RANGER), false);
        var omni = new ArmorSetResolver.PieceIdentity(Optional.empty(), true);
        assertEquals(Optional.of(ArmorSetIds.RANGER),
                ArmorSetResolver.resolvePieceIdentities(List.of(genuine, genuine, omni, omni)));
        assertTrue(ArmorSetResolver.resolvePieceIdentities(List.of(genuine, omni, omni, omni)).isEmpty());
        assertTrue(ArmorSetResolver.resolvePieceIdentities(List.of(omni, omni, omni, omni)).isEmpty());
    }

    private static void assertIdentityArgument(Component line, String expected) {
        var contents = assertInstanceOf(net.minecraft.network.chat.contents.TranslatableContents.class, line.getContents());
        assertEquals("tooltip.cosmicpve.armor_set.identity", contents.getKey());
        assertEquals(expected, assertInstanceOf(Component.class, contents.getArgs()[0]).getString());
    }
}
