package com.cosmicpve.equipment.armor;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.content.definition.armor.ArmorSetDefinitionData;
import com.cosmicpve.data.component.ArmorSetIdentity;
import com.cosmicpve.equipment.enchantment.OrbPresentationColors;
import com.cosmicpve.registry.ModDataComponents;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArmorCrystalPolishTest {
    @Test void everyProductionCrystalUsesOrbRateColorsAndBoldBonusWithoutMutatingData() throws Exception {
        for (String name : java.util.List.of("phantom","yeti","ancient","yjiki","dimensional_traveler","engineer","ranger","dragonslayer")) {
            try (var reader = new java.io.InputStreamReader(getClass().getResourceAsStream(
                    "/data/cosmicpve/cosmicpve/armor_sets/" + name + ".json"))) {
                var definition = ArmorSetDefinitionData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader))
                        .getOrThrow().resolve(CosmicPVE.id(name)).valueOrThrow();
                var crystal = ArmorSetCrystals.create(definition, 37);
                var before = crystal.copy();
                var data = crystal.get(ModDataComponents.ARMOR_SET_CRYSTAL.get());
                var lore = ArmorSetCrystalItem.lore(data);
                assertEquals(0x55FF55, OrbPresentationColors.SUCCESS);
                assertEquals(0xFF5555, OrbPresentationColors.DESTROY);
                assertEquals(OrbPresentationColors.SUCCESS,lore.get(0).getStyle().getColor().getValue());
                assertEquals(OrbPresentationColors.DESTROY,lore.get(1).getStyle().getColor().getValue());
                for(int i=0;i<3;i++)assertFalse(lore.get(i).getStyle().isBold());
                for(int i=4;i<lore.size();i++) {
                    assertTrue(lore.get(i).getStyle().isBold());
                    assertEquals(data.identity().color(),lore.get(i).getStyle().getColor().getValue());
                    assertEquals(data.identity().fullSetBonus().get(i-4).getString(),lore.get(i).getString());
                }
                assertEquals(37,data.successRate());
                assertTrue(net.minecraft.world.item.ItemStack.matches(before,crystal));
                if(name.equals("yjiki")) {
                    assertEquals(0xFFFFFF,definition.presentationColor());
                    assertEquals(0xFFFFFF,crystal.getHoverName().getStyle().getColor().getValue());
                }
            }
        }
    }

    @Test void legacyYjikiSnapshotsUseWhiteAndNestedBonusFragmentsAreBold() {
        var bonus = Component.literal("Parent").append(Component.literal("child")
                .withStyle(s -> s.withBold(false).withColor(0x123456)));
        var identity = new ArmorSetIdentity(1,ArmorSetIds.YJIKI,Component.literal("Yjiki"),0xAA00AA,java.util.List.of(bonus));
        assertEquals(0xFFFFFF,identity.color());
        var data = new com.cosmicpve.data.component.ArmorSetCrystalData(1,identity,45);
        var line = ArmorSetCrystalItem.lore(data).get(4);
        assertTrue(line.getSiblings().getFirst().getStyle().isBold());
        assertEquals(0x123456,line.getSiblings().getFirst().getStyle().getColor().getValue());
        assertFalse(bonus.getSiblings().getFirst().getStyle().isBold());
    }
}
