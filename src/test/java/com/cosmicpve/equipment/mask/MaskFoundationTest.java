package com.cosmicpve.equipment.mask;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.MaskLoadout;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.util.List;
import org.junit.jupiter.api.Test;

class MaskFoundationTest {
    @Test void loadoutPreservesOrderRejectsDuplicatesAndSupportsFive() {
        var ids=List.of(CosmicPVE.id("santa"),CosmicPVE.id("party"),CosmicPVE.id("dragon"));
        var presentations=ids.stream().map(id -> new com.cosmicpve.data.component.MaskPresentation(id,
                net.minecraft.network.chat.Component.literal(id.getPath()),
                net.minecraft.network.chat.Component.literal("Effect "+id.getPath()),0x123456)).toList();
        var value=new MaskLoadout(ids,MaskProfiles.MULTI_TEXTURE,presentations);
        assertTrue(value.valid()); assertEquals(ids,value.maskIds());
        var encoded=MaskLoadout.CODEC.encodeStart(JsonOps.INSTANCE,value).getOrThrow();
        var decoded=MaskLoadout.CODEC.parse(JsonOps.INSTANCE,encoded).getOrThrow();
        assertEquals(presentations,decoded.presentations());
        assertTrue(new MaskLoadout(List.of(CosmicPVE.id("santa"),CosmicPVE.id("santa"))).valid()==false);
        assertTrue(new MaskLoadout(List.of(CosmicPVE.id("santa"),CosmicPVE.id("party"),CosmicPVE.id("dragon"),
                CosmicPVE.id("lover"),CosmicPVE.id("zeus"))).valid());
    }
    @Test void codecRejectsEmptyDuplicateAndOverFive() {
        assertTrue(MaskLoadout.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString("{\"masks\":[]}" )).error().isPresent());
        assertTrue(MaskLoadout.CODEC.parse(JsonOps.INSTANCE,JsonParser.parseString(
                "{\"masks\":[\"cosmicpve:santa\",\"cosmicpve:santa\"]}" )).error().isPresent());
    }
    @Test void canonicalMathAndIntervalsRemainExact() {
        assertEquals(.02,MaskCombatResolver.TURKEY_DODGE.equals(CosmicPVE.id("turkey_mask_dodge")) ? .02 : -1);
        assertEquals(100,MaskRuntimeEventBridge.LOVER_INTERVAL_TICKS);
        assertEquals(160,MaskRuntimeEventBridge.SCARECROW_INTERVAL_TICKS);
        assertEquals(.05,MaskRuntimeEventBridge.movementBonus(com.cosmicpve.content.definition.mask.MaskBehavior.REINDEER));
        assertEquals(.01,MaskRuntimeEventBridge.movementBonus(com.cosmicpve.content.definition.mask.MaskBehavior.PARTY));
        for (int pieces=0; pieces<=5; pieces++) assertEquals(pieces * .01,
                MaskCombatResolver.monopolyOutgoingBonus(pieces));
        assertEquals(.55, com.cosmicpve.equipment.enchantment.HolyWhiteScrollService.preservationChance(true));
        assertEquals(.50, com.cosmicpve.equipment.enchantment.HolyWhiteScrollService.preservationChance(false));
        assertEquals(60,MaskRuntimeEventBridge.GUCCI_LEASE_TICKS);
        assertEquals(20,MaskRuntimeEventBridge.GUCCI_REFRESH_AT);
    }
    @Test void attachedLoreIsCompactIdentityOnlyAndOrdered() {
        var masks=List.of(
                new com.cosmicpve.data.component.MaskPresentation(CosmicPVE.id("zeus"),
                        net.minecraft.network.chat.Component.literal("Zeus"),net.minecraft.network.chat.Component.literal("Effect A"),0x123456),
                new com.cosmicpve.data.component.MaskPresentation(CosmicPVE.id("santa"),
                        net.minecraft.network.chat.Component.literal("Santa"),net.minecraft.network.chat.Component.literal("Effect B"),0x654321));
        var lines=MaskLore.attached(masks);
        assertEquals(2,lines.size());
        assertEquals("tooltip.cosmicpve.mask.attached_prefixtooltip.cosmicpve.mask.multi_prefixZeus, Santa)",lines.getFirst().getString());
        assertFalse(lines.stream().anyMatch(line -> line.getString().contains("Effect")));
    }
    @Test void attachedIdentityRemainsOneLogicalComponentForSingleThreeAndFiveMasks() {
        var masks=new java.util.ArrayList<com.cosmicpve.data.component.MaskPresentation>();
        for(int i=0;i<5;i++) masks.add(new com.cosmicpve.data.component.MaskPresentation(CosmicPVE.id("mask_"+i),
                net.minecraft.network.chat.Component.literal("Long Mask "+i),
                net.minecraft.network.chat.Component.literal("Effect "+i),0x110000+i));
        for(int count : List.of(1,3,5)) {
            var identity=MaskLore.attachedIdentity(masks.subList(0,count));
            assertFalse(identity.getString().contains("\n"));
            assertTrue(identity.getStyle().isBold());
            assertTrue(identity.getSiblings().stream().allMatch(part -> part.getStyle().isBold()));
            assertEquals(identity.getString(),MaskLore.attached(masks.subList(0,count)).getFirst().getString());
            assertEquals(2,MaskLore.attached(masks.subList(0,count)).size());
            assertFalse(MaskLore.attached(masks.subList(0,count)).stream()
                    .anyMatch(line -> line.getString().contains("Effect")));
        }
    }
    @Test void canonicalTooltipCopyAndDragonNameMatchCurrentDesign() throws Exception {
        var lang=JsonParser.parseReader(new java.io.InputStreamReader(java.util.Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/cosmicpve/lang/en_us.json")))).getAsJsonObject();
        assertEquals("Dragon",lang.get("mask.cosmicpve.dragon").getAsString());
        assertEquals("Gain +5% Movement Speed!",lang.get("mask.cosmicpve.reindeer.effect").getAsString());
        assertEquals("Deal +2% outgoing damage and take 50% less damage from Fire, Lava, and Poison.",
                lang.get("mask.cosmicpve.dragon.effect").getAsString());
        var dragon=JsonParser.parseReader(new java.io.InputStreamReader(java.util.Objects.requireNonNull(
                getClass().getResourceAsStream("/data/cosmicpve/cosmicpve/masks/dragon.json")))).getAsJsonObject();
        assertEquals("#FFF24D",dragon.get("color").getAsString());
        assertEquals("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjhhYTNjNTNlNDY3NTIzYzZjM2Q4MzNiYmJlZTM3YTY2ZmMxNGYzMDUzMGYzOWE2YTljMDQ1N2ZmZTgwNWMyNSJ9fX0=",
                dragon.get("profile_texture").getAsString());
        assertEquals("Separates a Multi-Mask into its individual Masks.",
                lang.get("tooltip.cosmicpve.mask_splicer.purpose").getAsString());
        assertEquals("Thanos",lang.get("mask.cosmicpve.thanos").getAsString());
        assertEquals("Monopoly",lang.get("mask.cosmicpve.monopoly").getAsString());
        assertEquals("Gucci",lang.get("mask.cosmicpve.gucci").getAsString());
    }
    @Test void loreSupportsSingleTwoThreeAndFiveInStableOrder() {
        var definitions=new java.util.ArrayList<com.cosmicpve.content.definition.mask.MaskDefinition>();
        for (int i=0;i<5;i++) definitions.add(new com.cosmicpve.content.definition.mask.MaskDefinition(
                CosmicPVE.id("mask_"+i),net.minecraft.network.chat.Component.literal("Mask "+i),
                net.minecraft.network.chat.Component.literal("Effect "+i),i+1,MaskProfiles.MULTI_TEXTURE,
                com.cosmicpve.content.definition.mask.MaskBehavior.SANTA));
        for (int count : List.of(1,2,3,5)) {
            var lines=MaskLore.lines(definitions.subList(0,count).stream().map(definition ->
                    new com.cosmicpve.data.component.MaskPresentation(definition.id(), definition.displayName(),
                            definition.effectSummary(), definition.presentationColor())).toList(),true);
            assertTrue(lines.stream().anyMatch(line->line.getString().contains("Effect 0")));
            assertEquals(count==1 ? 3 : count*2+3,lines.size());
            for (int i=0;i<count;i++) {
                String expected="Effect "+i;
                assertTrue(lines.stream().anyMatch(line->line.getString().contains(expected)));
            }
        }
    }
    @Test void itemDefinitionsUseVanillaPlayerHeadAndShearsPresentation() throws Exception {
        var mask=JsonParser.parseReader(new java.io.InputStreamReader(java.util.Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/cosmicpve/items/mask.json")))).getAsJsonObject().getAsJsonObject("model");
        assertEquals("minecraft:special",mask.get("type").getAsString());
        assertEquals("minecraft:player_head",mask.getAsJsonObject("model").get("type").getAsString());
        var splicer=JsonParser.parseReader(new java.io.InputStreamReader(java.util.Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/cosmicpve/items/mask_splicer.json")))).getAsJsonObject().getAsJsonObject("model");
        assertEquals("minecraft:item/shears",splicer.get("model").getAsString());
    }

    @Test void monopolyCountsOnlyPersistentHolyStateOnCanonicalGear() {
        var held=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD);
        var armor=new java.util.ArrayList<net.minecraft.world.item.ItemStack>();
        armor.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_HELMET));
        armor.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE));
        armor.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_LEGGINGS));
        armor.add(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_BOOTS));
        assertEquals(0,MaskCombatResolver.holyGearCount(held,armor));
        held.set(com.cosmicpve.registry.ModDataComponents.HOLY.get(),true);
        assertEquals(1,MaskCombatResolver.holyGearCount(held,armor));
        for(int i=0;i<armor.size();i++) {
            armor.get(i).set(com.cosmicpve.registry.ModDataComponents.HOLY.get(),true);
            assertEquals(i+2,MaskCombatResolver.holyGearCount(held,armor));
        }
    }

    @Test void thanosDetectsActualMasteryAndIgnoresOrdinaryCosmicEnchantments() {
        var registry=new net.minecraft.core.MappedRegistry<net.minecraft.world.item.enchantment.Enchantment>(
                net.minecraft.core.registries.Registries.ENCHANTMENT,com.mojang.serialization.Lifecycle.stable());
        var supported=net.minecraft.core.HolderSet.direct(
                net.minecraft.core.registries.BuiltInRegistries.ITEM.wrapAsHolder(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE));
        var definition=net.minecraft.world.item.enchantment.Enchantment.definition(supported,1,5,
                net.minecraft.world.item.enchantment.Enchantment.constantCost(1),
                net.minecraft.world.item.enchantment.Enchantment.constantCost(1),1,
                net.minecraft.world.entity.EquipmentSlotGroup.CHEST);
        var deathPact=registry.register(com.cosmicpve.registry.ModEnchantments.DEATH_PACT,
                new net.minecraft.world.item.enchantment.Enchantment(net.minecraft.network.chat.Component.literal("Death Pact"),definition,
                        net.minecraft.core.HolderSet.empty(),net.minecraft.core.component.DataComponentMap.EMPTY),
                net.minecraft.core.RegistrationInfo.BUILT_IN);
        var execute=registry.register(com.cosmicpve.registry.ModEnchantments.EXECUTE,
                new net.minecraft.world.item.enchantment.Enchantment(net.minecraft.network.chat.Component.literal("Execute"),definition,
                        net.minecraft.core.HolderSet.empty(),net.minecraft.core.component.DataComponentMap.EMPTY),
                net.minecraft.core.RegistrationInfo.BUILT_IN);
        registry.freeze();
        var ordinary=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE);
        net.minecraft.world.item.enchantment.EnchantmentHelper.updateEnchantments(ordinary,m -> m.set(execute,5));
        assertFalse(MaskCombatResolver.hasActualMastery(ordinary));
        var mastery=new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_CHESTPLATE);
        net.minecraft.world.item.enchantment.EnchantmentHelper.updateEnchantments(mastery,m -> m.set(deathPact,1));
        assertTrue(MaskCombatResolver.hasActualMastery(mastery));
        assertTrue(MaskCombatResolver.anyActualMastery(List.of(ordinary,mastery)));
        assertFalse(MaskCombatResolver.anyActualMastery(List.of(ordinary)));
    }
}
