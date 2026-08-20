package com.cosmicpve.content.definition.armor;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.CosmicPVE;
import com.cosmicpve.equipment.armor.ArmorSetIds;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;

class BundledArmorSetDefinitionsTest {
    @Test void bundledArmorSetsUseApprovedSemantics() {
        var phantom = load("phantom");
        assertEquals(0xFF6969, phantom.presentationColor());
        assertEquals(.25, phantom.additiveOutgoingBonus());
        assertEquals(1.10, phantom.incomingMultiplier());
        assertEquals(1.25, phantom.procChanceMultipliers().get(CosmicPVE.id("mastery")));

        var yeti = load("yeti");
        assertEquals(0xA3FFF5, yeti.presentationColor());
        assertEquals(.10, yeti.additiveOutgoingBonus());
        assertEquals(.90, yeti.incomingMultiplier());
        assertTrue(yeti.immunities().containsAll(java.util.Set.of(CosmicPVE.id("freeze"), CosmicPVE.id("frozen"),
                CosmicPVE.id("permafrost"), CosmicPVE.id("ice_aspect"))));

        var ancient = load("ancient");
        assertEquals(0x050C59, ancient.presentationColor());
        assertEquals(.075, ancient.additiveOutgoingBonus());
        assertEquals(.925, ancient.incomingMultiplier());
    }

    @Test void ancientDefinitionWorksThroughGenericCrystalTransaction() {
        var ancient = load("ancient");
        var repository = new com.cosmicpve.content.CosmicContentRepository();
        repository.publish(com.cosmicpve.content.validation.ValidationResult.success(
                new com.cosmicpve.content.ContentSnapshot(0, java.util.Map.of(), java.util.Map.of(),
                        java.util.Map.of(ancient.id(), ancient))));
        var crystal = new net.minecraft.world.item.ItemStack(com.cosmicpve.registry.ModItems.ARMOR_SET_CRYSTAL.get());
        crystal.set(com.cosmicpve.registry.ModDataComponents.ARMOR_SET_CRYSTAL.get(),
                new com.cosmicpve.data.component.ArmorSetCrystalData(1,
                        com.cosmicpve.data.component.ArmorSetIdentity.from(ancient), 100));
        var chestplate = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE);
        var outcome = new com.cosmicpve.equipment.armor.ArmorCrystalApplicationService(
                repository, () -> { throw new AssertionError("100% crystal rolled"); }).apply(crystal, chestplate);
        assertEquals(com.cosmicpve.equipment.armor.ArmorCrystalApplicationService.Outcome.SUCCESS, outcome);
        assertEquals(ArmorSetIds.ANCIENT,
                chestplate.get(com.cosmicpve.registry.ModDataComponents.ARMOR_SET_ID.get()).setId());
    }

    private static ArmorSetDefinition load(String name) {
        String path = "/data/cosmicpve/cosmicpve/armor_sets/" + name + ".json";
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(
                BundledArmorSetDefinitionsTest.class.getResourceAsStream(path)))) {
            var data = ArmorSetDefinitionData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
            return data.resolve(CosmicPVE.id(name)).valueOrThrow();
        } catch (java.io.IOException exception) {
            throw new AssertionError(exception);
        }
    }
}
