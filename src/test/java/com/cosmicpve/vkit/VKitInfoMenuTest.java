package com.cosmicpve.vkit;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class VKitInfoMenuTest {
    @Test void oneRowLayoutUsesExactOrderAndAlternatingSlots() {
        assertEquals(9, VKitInfoMenu.SLOT_COUNT);
        assertArrayEquals(new int[] {1, 3, 5, 7}, VKitInfoMenu.KIT_SLOTS);
        assertEquals(List.of(VKitDefinition.PHOENIX, VKitDefinition.OGRE,
                VKitDefinition.SLAYER, VKitDefinition.JUDGEMENT), VKitInfoMenu.DISPLAY_ORDER);
        for (int slot = 0; slot < 9; slot++) assertEquals(slot % 2 == 1
                ? VKitInfoMenu.SlotRole.KIT : VKitInfoMenu.SlotRole.FILLER, VKitInfoMenu.role(slot));
        assertEquals("V-KITS", VKitInfoMenu.TITLE.getString());
        assertTrue(VKitInfoMenu.TITLE.getStyle().isBold());
        assertEquals(0xFFAA00, VKitInfoMenu.TITLE.getStyle().getColor().getValue());
    }

    @Test void iconsUseExactBasesColorsGlintAndNames() {
        assertIcon(VKitDefinition.PHOENIX, Items.BLAZE_POWDER, 7);
        assertIcon(VKitDefinition.OGRE, Items.SLIME_BALL, 10);
        assertIcon(VKitDefinition.SLAYER, Items.ECHO_SHARD, 0);
        assertIcon(VKitDefinition.JUDGEMENT, Items.HEAVY_CORE, 1);
    }

    @Test void unlockedAndLockedLoreUseCanonicalRewardNameHelper() {
        var phoenix = VKitInfoMenu.icon(VKitDefinition.PHOENIX, Items.BLAZE_POWDER, 7);
        var phoenixLore = phoenix.get(DataComponents.LORE).lines();
        assertEquals("LEVEL: VII", phoenixLore.get(0).getString());
        assertEquals("POSSIBLE REWARDS", phoenixLore.get(2).getString());
        assertEquals("Sandals of the Phoenix (VII)", phoenixLore.get(3).getString());
        assertEquals("Sikanda (VII)", phoenixLore.get(4).getString());
        assertRewardStyle(phoenixLore.get(3), VKitDefinition.PHOENIX.color());
        assertRewardStyle(phoenixLore.get(4), VKitDefinition.PHOENIX.color());

        var slayer = VKitInfoMenu.icon(VKitDefinition.SLAYER, Items.ECHO_SHARD, 0);
        var slayerLore = slayer.get(DataComponents.LORE).lines();
        assertEquals("LEVEL: LOCKED", slayerLore.get(0).getString());
        assertEquals("Shroud of War", slayerLore.get(3).getString());
        assertEquals("Glitched Bow", slayerLore.get(4).getString());
        assertFalse(slayersHaveFakeLevel(slayerLore));
    }

    private static boolean slayersHaveFakeLevel(List<net.minecraft.network.chat.Component> lore) {
        return lore.stream().anyMatch(line -> line.getString().matches(".*\\([IVX]+\\)$"));
    }

    private static void assertIcon(VKitDefinition definition, net.minecraft.world.item.Item item, int level) {
        var icon = VKitInfoMenu.icon(definition, item, level);
        assertTrue(icon.is(item));
        assertTrue(icon.hasFoil());
        assertEquals(definition.displayName() + " V-Kit", icon.getHoverName().getString());
        assertEquals(definition.color(), icon.getHoverName().getStyle().getColor().getValue());
        assertTrue(icon.getHoverName().getStyle().isBold());
        assertTrue(icon.getHoverName().getStyle().isUnderlined());
        assertFalse(icon.getHoverName().getStyle().isItalic());
        assertNull(icon.get(com.cosmicpve.registry.ModDataComponents.VKIT_EQUIPMENT.get()));
        assertNull(icon.get(com.cosmicpve.registry.ModDataComponents.VKIT_CRYSTAL.get()));
    }

    private static void assertRewardStyle(net.minecraft.network.chat.Component component, int color) {
        assertEquals(color, component.getStyle().getColor().getValue());
        assertTrue(component.getStyle().isBold());
        assertTrue(component.getStyle().isItalic());
    }
}
