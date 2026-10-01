package com.cosmicpve.equipment.skin;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.data.component.CustomEnchantMetadata;
import com.cosmicpve.data.component.WeaponSkinIdentity;
import com.cosmicpve.data.component.WeaponSkinItemData;
import com.cosmicpve.equipment.enchantment.ActualEnchantmentGrant;
import com.cosmicpve.equipment.enchantment.EffectiveEnchantmentsResolver;
import com.cosmicpve.equipment.enchantment.EnchantmentSourceKind;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModEnchantments;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import javax.imageio.ImageIO;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.junit.jupiter.api.Test;

class WeaponSkinFoundationTest {
    private final WeaponSkinApplicationService service = new WeaponSkinApplicationService();
    private final WeaponSkinResolver resolver = new WeaponSkinResolver();

    @Test void definitionsAreStableDiscoverableAndApplicableToMultipleVanillaTiers() {
        assertEquals(List.of(WeaponSkinDefinitions.BOOSTED_CHAINSAW, WeaponSkinDefinitions.MAUIS_HOOK,
                WeaponSkinDefinitions.GRIM_AXE, WeaponSkinDefinitions.SEASONS_BEATINGS,
                WeaponSkinDefinitions.SPINAL_TAP, WeaponSkinDefinitions.STORMBRINGER,
                WeaponSkinDefinitions.THE_CARVER, WeaponSkinDefinitions.WHISK_TAKER,
                WeaponSkinDefinitions.FIREWORK_ROCKET, WeaponSkinDefinitions.TRIDENT_OF_THE_DEEP).stream()
                .sorted(java.util.Comparator.comparing(Identifier::toString)).toList(), WeaponSkinDefinitions.ids());
        assertTrue(WeaponSkinDefinitions.find(WeaponSkinDefinitions.BOOSTED_CHAINSAW).orElseThrow()
                .accepts(new ItemStack(Items.WOODEN_AXE)));
        assertTrue(WeaponSkinDefinitions.find(WeaponSkinDefinitions.STORMBRINGER).orElseThrow()
                .accepts(new ItemStack(Items.NETHERITE_AXE)));
        assertTrue(WeaponSkinDefinitions.find(WeaponSkinDefinitions.MAUIS_HOOK).orElseThrow()
                .accepts(new ItemStack(Items.IRON_SWORD)));
        assertFalse(WeaponSkinDefinitions.find(WeaponSkinDefinitions.MAUIS_HOOK).orElseThrow()
                .accepts(new ItemStack(Items.DIAMOND_AXE)));
        assertTrue(WeaponSkinDefinitions.find(WeaponSkinDefinitions.SEASONS_BEATINGS).orElseThrow()
                .accepts(new ItemStack(Items.DIAMOND_SWORD)));
        assertFalse(WeaponSkinDefinitions.find(WeaponSkinDefinitions.SEASONS_BEATINGS).orElseThrow()
                .accepts(new ItemStack(Items.DIAMOND_AXE)));
        assertTrue(WeaponSkinDefinitions.find(WeaponSkinDefinitions.WHISK_TAKER).orElseThrow()
                .accepts(new ItemStack(Items.GOLDEN_AXE)));
        assertTrue(WeaponSkinDefinitions.find(WeaponSkinDefinitions.GRIM_AXE).orElseThrow()
                .accepts(new ItemStack(Items.NETHERITE_AXE)));
        assertFalse(WeaponSkinDefinitions.find(WeaponSkinDefinitions.GRIM_AXE).orElseThrow()
                .accepts(new ItemStack(Items.WOODEN_SWORD)));
        assertTrue(WeaponSkinDefinitions.find(WeaponSkinDefinitions.SPINAL_TAP).orElseThrow()
                .accepts(new ItemStack(Items.WOODEN_SWORD)));
        assertTrue(WeaponSkinDefinitions.find(WeaponSkinDefinitions.THE_CARVER).orElseThrow()
                .accepts(new ItemStack(Items.NETHERITE_SWORD)));
    }

    @Test void canonicalLoreUsesPerSkinNameColorsAndSharedRedGrayPresentation() {
        assertSkinLore(WeaponSkinDefinitions.BOOSTED_CHAINSAW, 0xCCA00A,
                WeaponSkinDefinition.WeaponKind.AXE, 1);
        assertSkinLore(WeaponSkinDefinitions.MAUIS_HOOK, 0x404242,
                WeaponSkinDefinition.WeaponKind.SWORD, 2);
        assertSkinLore(WeaponSkinDefinitions.STORMBRINGER, 0x224B57,
                WeaponSkinDefinition.WeaponKind.AXE, 2);
        assertSkinLore(WeaponSkinDefinitions.SEASONS_BEATINGS, 0x1B943A,
                WeaponSkinDefinition.WeaponKind.SWORD, 2);
        assertSkinLore(WeaponSkinDefinitions.GRIM_AXE, 0x4C09B8, WeaponSkinDefinition.WeaponKind.AXE, 1);
        assertSkinLore(WeaponSkinDefinitions.WHISK_TAKER, 0xB08E00, WeaponSkinDefinition.WeaponKind.AXE, 2);
        assertSkinLore(WeaponSkinDefinitions.SPINAL_TAP, 0x00F02C, WeaponSkinDefinition.WeaponKind.SWORD, 1);
        assertSkinLore(WeaponSkinDefinitions.THE_CARVER, 0xDBD70B, WeaponSkinDefinition.WeaponKind.SWORD, 2);
    }

    @Test void revisedGrimAxeAndCarverDescriptionsRemainSeparate() throws Exception {
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                "/assets/cosmicpve/lang/en_us.json")))) {
            var language = JsonParser.parseReader(reader).getAsJsonObject();
            assertEquals("5% chance to suppress enemy unique and elite enchantments.",
                    language.get("weapon_skin.cosmicpve.grim_axe.effect").getAsString());
            assertEquals("Devour IV", language.get("weapon_skin.cosmicpve.the_carver.effect.devour").getAsString());
            assertEquals("Deal +3.3% damage to enemies at a lower health percentage.",
                    language.get("weapon_skin.cosmicpve.the_carver.effect.damage").getAsString());
        }
        assertEquals(2, WeaponSkinDefinitions.find(WeaponSkinDefinitions.THE_CARVER)
                .orElseThrow().effectDescription().size(), "Carver's other effect remains on its own lore line");
    }

    @Test void typedDataCodecsRejectInvalidVersions() {
        assertEquals(WeaponSkinDefinitions.MAUIS_HOOK,
                new WeaponSkinItemData(1, WeaponSkinDefinitions.MAUIS_HOOK).skinId());
        assertThrows(IllegalArgumentException.class,
                () -> new WeaponSkinIdentity(0, WeaponSkinDefinitions.MAUIS_HOOK, java.util.Optional.empty()));
        assertTrue(WeaponSkinItemData.CODEC.parse(JsonOps.INSTANCE,
                JsonParser.parseString("{\"data_version\":0,\"skin_id\":\"cosmicpve:mauis_hook\"}"))
                .error().isPresent());
    }

    @Test void applyMutatesOriginalStackOnceAndPreservesUnrelatedComponents() {
        var skin = WeaponSkinItemFactory.create(WeaponSkinDefinitions.MAUIS_HOOK);
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.setDamageValue(37);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("Kept Name"));
        var metadata = new CustomEnchantMetadata(1, 5, 3, true, true);
        sword.set(ModDataComponents.CUSTOM_ENCHANT_META.get(), metadata);
        Identifier originalModel = sword.get(DataComponents.ITEM_MODEL);

        assertEquals(WeaponSkinApplicationService.ApplyOutcome.SUCCESS,
                service.apply(skin, sword, skin, sword));

        assertTrue(skin.isEmpty());
        assertEquals(37, sword.getDamageValue());
        assertEquals("Kept Name", sword.getHoverName().getString());
        assertEquals(metadata, sword.get(ModDataComponents.CUSTOM_ENCHANT_META.get()));
        assertEquals(WeaponSkinDefinitions.MAUIS_HOOK,
                sword.get(ModDataComponents.WEAPON_SKIN.get()).skinId());
        assertEquals(WeaponSkinDefinitions.MAUIS_HOOK, sword.get(DataComponents.ITEM_MODEL));
        assertEquals(originalModel,
                sword.get(ModDataComponents.WEAPON_SKIN.get()).previousItemModel().orElseThrow());
    }

    @Test void incompatibleSecondAndStaleApplicationsAreAtomic() {
        var chainsaw = WeaponSkinItemFactory.create(WeaponSkinDefinitions.BOOSTED_CHAINSAW);
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        assertEquals(WeaponSkinApplicationService.ApplyOutcome.REJECTED_TARGET,
                service.apply(chainsaw, sword, chainsaw, sword));
        assertEquals(1, chainsaw.getCount());
        assertFalse(sword.has(ModDataComponents.WEAPON_SKIN.get()));

        var axe = new ItemStack(Items.DIAMOND_AXE);
        assertEquals(WeaponSkinApplicationService.ApplyOutcome.REJECTED_STALE,
                service.apply(chainsaw, axe, chainsaw.copy(), axe));
        assertEquals(1, chainsaw.getCount());
        assertEquals(WeaponSkinApplicationService.ApplyOutcome.SUCCESS,
                service.apply(chainsaw, axe, chainsaw, axe));
        var storm = WeaponSkinItemFactory.create(WeaponSkinDefinitions.STORMBRINGER);
        assertEquals(WeaponSkinApplicationService.ApplyOutcome.REJECTED_ALREADY_SKINNED,
                service.apply(storm, axe, storm, axe));
        assertEquals(1, storm.getCount());
    }

    @Test void emptyCursorRemovalReturnsSkinAndRestoresExactModelWithoutLoss() {
        var skin = WeaponSkinItemFactory.create(WeaponSkinDefinitions.STORMBRINGER);
        var axe = new ItemStack(Items.IRON_AXE);
        Identifier originalModel = axe.get(DataComponents.ITEM_MODEL);
        service.apply(skin, axe, skin, axe);

        assertEquals(WeaponSkinApplicationService.RemoveOutcome.REJECTED_OUTPUT,
                service.remove(axe, axe, false).outcome());
        assertTrue(axe.has(ModDataComponents.WEAPON_SKIN.get()));
        assertEquals(WeaponSkinApplicationService.RemoveOutcome.REJECTED_STALE,
                service.remove(axe, axe.copy(), true).outcome());

        var result = service.remove(axe, axe, true);
        assertEquals(WeaponSkinApplicationService.RemoveOutcome.SUCCESS, result.outcome());
        assertFalse(axe.has(ModDataComponents.WEAPON_SKIN.get()));
        assertEquals(originalModel, axe.get(DataComponents.ITEM_MODEL));
        assertEquals(WeaponSkinDefinitions.STORMBRINGER,
                result.returnedSkin().get(ModDataComponents.WEAPON_SKIN_ITEM.get()).skinId());
        assertEquals(WeaponSkinDefinitions.STORMBRINGER,
                result.returnedSkin().get(DataComponents.ITEM_MODEL));
    }

    @Test void boostedChainsawIsVirtualOnlyUsesMaximumAndDisappearsOnRemoval() {
        var skin = WeaponSkinItemFactory.create(WeaponSkinDefinitions.BOOSTED_CHAINSAW);
        var axe = new ItemStack(Items.DIAMOND_AXE);
        service.apply(skin, axe, skin, axe);
        var grants = resolver.virtualEnchantments(axe);
        assertEquals(1, grants.size());
        assertEquals(5, grants.getFirst().level());
        assertEquals(WeaponSkinDefinitions.BOOSTED_CHAINSAW, grants.getFirst().sourceId());
        assertFalse(EnchantmentHelper.hasAnyEnchantments(axe));
        var effective = new EffectiveEnchantmentsResolver().resolveSources(
                List.of(new ActualEnchantmentGrant(ModEnchantments.DOUBLESTRIKE.identifier(), 2,
                        CosmicPVE.id("actual_item"))), grants);
        assertEquals(5, effective.level(ModEnchantments.DOUBLESTRIKE.identifier()));
        assertEquals(0.05, com.cosmicpve.combat.enchantment.DoublestrikeBehavior.chance(
                effective.level(ModEnchantments.DOUBLESTRIKE.identifier())), 1.0E-12);
        assertTrue(effective.get(ModEnchantments.DOUBLESTRIKE.identifier()).orElseThrow().provenance().stream()
                .anyMatch(value -> value.kind() == EnchantmentSourceKind.VIRTUAL
                        && value.sourceId().equals(WeaponSkinDefinitions.BOOSTED_CHAINSAW)));
        service.remove(axe, axe, true);
        assertTrue(resolver.virtualEnchantments(axe).isEmpty());
    }

    @Test void boostedChainsawLoreNamesItsVirtualLevel() throws Exception {
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                "/assets/cosmicpve/lang/en_us.json")))) {
            var language = JsonParser.parseReader(reader).getAsJsonObject();
            assertEquals("Grants Doublestrike V.",
                    language.get("weapon_skin.cosmicpve.boosted_chainsaw.effect").getAsString());
        }
    }

    @Test void canonicalBehaviorConstantsAndLeatherSoundPitchesRemainExact() {
        assertEquals(0.04, WeaponSkinCombatResolver.MAUI_OUTGOING);
        assertEquals(0.10, WeaponSkinCombatResolver.MAUI_STEAL_CHANCE);
        assertEquals(0.03, WeaponSkinCombatResolver.STORM_CHANCE);
        assertEquals(2.0, WeaponSkinCombatResolver.STORM_TRUE_DAMAGE);
        assertEquals(30, WeaponSkinCombatResolver.STORM_SLOWNESS_TICKS);
        assertEquals(1, WeaponSkinCombatResolver.STORM_SLOWNESS_AMPLIFIER);
        assertEquals(0.98, WeaponSkinCombatResolver.STORM_INCOMING_MULTIPLIER);
        assertEquals(0.10, WeaponSkinCombatResolver.SEASONS_OUTGOING);
        assertEquals(0.95, WeaponSkinCombatResolver.SEASONS_INCOMING_MULTIPLIER);
        assertEquals(1.0F, WeaponSkinFeedback.ATTACH_PITCH);
        assertTrue(WeaponSkinFeedback.REMOVE_PITCH < WeaponSkinFeedback.ATTACH_PITCH);
        var packet = WeaponSkinCombatResolver.stormPacket();
        assertEquals(2.0, packet.amount());
        assertTrue(packet.bypassesArmor());
        assertTrue(packet.bypassesCustomReduction());
        assertFalse(packet.bypassesAbsorption());
    }

    @Test void mauisHookUsesTheSharedAdditiveOutgoingBucketAndRemovalClearsIt() {
        var skin = WeaponSkinItemFactory.create(WeaponSkinDefinitions.MAUIS_HOOK);
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        service.apply(skin, sword, skin, sword);
        var behavior = new WeaponSkinCombatResolver(resolver, null, null);
        var context = new com.cosmicpve.combat.api.CombatContext(
                null, null, null, null, Optional.empty(), null,
                com.cosmicpve.combat.api.AttackCategory.MELEE,
                com.cosmicpve.combat.api.DamageChannel.ORDINARY, Set.of(),
                new com.cosmicpve.combat.api.WeaponSnapshot(sword),
                com.cosmicpve.equipment.enchantment.EffectiveEnchantments.EMPTY,
                1L, OptionalLong.empty(), com.cosmicpve.combat.api.RecursionPolicy.NORMAL, Set.of());
        var contribution = behavior.resolve(context);
        assertEquals(1, contribution.size());
        assertEquals(WeaponSkinDefinitions.MAUIS_HOOK, contribution.getFirst().sourceId());
        assertEquals(0.04, contribution.getFirst().bonus());

        service.remove(sword, sword, true);
        var removedContext = new com.cosmicpve.combat.api.CombatContext(
                null, null, null, null, Optional.empty(), null,
                com.cosmicpve.combat.api.AttackCategory.MELEE,
                com.cosmicpve.combat.api.DamageChannel.ORDINARY, Set.of(),
                new com.cosmicpve.combat.api.WeaponSnapshot(sword),
                com.cosmicpve.equipment.enchantment.EffectiveEnchantments.EMPTY,
                2L, OptionalLong.empty(), com.cosmicpve.combat.api.RecursionPolicy.NORMAL, Set.of());
        assertTrue(behavior.resolve(removedContext).isEmpty());
    }

    @Test void suppliedTexturesAreUsableTransparentThirtyTwoPixelResources() throws Exception {
        for (String name : List.of("boosted_chainsaw", "mauis_hook", "seasons_beatings", "stormbringer",
                "grim_axe", "whisk_taker", "the_carver", "spinal_tap")) {
            BufferedImage image = ImageIO.read(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                    "/assets/cosmicpve/textures/item/" + name + ".png")));
            assertEquals(32, image.getWidth());
            assertEquals(32, image.getHeight());
            assertTrue(image.getColorModel().hasAlpha());
            boolean transparent=false;
            for(int y=0;y<image.getHeight()&&!transparent;y++) for(int x=0;x<image.getWidth();x++)
                if(((image.getRGB(x,y)>>>24)&0xFF)<255) { transparent=true; break; }
            assertTrue(transparent,name+" must retain transparent background pixels");
            assertEquals("cosmicpve:item/" + name, itemModel(name));
        }
    }

    @Test void newSkinsPreserveUnderlyingAttackAttributes() {
        assertAttributesPreserved(WeaponSkinDefinitions.WHISK_TAKER, Items.DIAMOND_AXE);
        assertAttributesPreserved(WeaponSkinDefinitions.GRIM_AXE, Items.NETHERITE_AXE);
        assertAttributesPreserved(WeaponSkinDefinitions.SPINAL_TAP, Items.DIAMOND_SWORD);
        assertAttributesPreserved(WeaponSkinDefinitions.THE_CARVER, Items.IRON_SWORD);
    }

    @Test void carverDevourIsVirtualHighestLevelAndDisappearsOnRemoval() {
        var skin = WeaponSkinItemFactory.create(WeaponSkinDefinitions.THE_CARVER);
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        service.apply(skin, sword, skin, sword);
        var grants = resolver.virtualEnchantments(sword);
        assertEquals(1, grants.size());
        assertEquals(ModEnchantments.DEVOUR.identifier(), grants.getFirst().enchantmentId());
        assertEquals(4, grants.getFirst().level());
        assertFalse(EnchantmentHelper.hasAnyEnchantments(sword));
        var effective = new EffectiveEnchantmentsResolver().resolveSources(List.of(
                new ActualEnchantmentGrant(ModEnchantments.DEVOUR.identifier(), 2, CosmicPVE.id("actual_item"))), grants);
        assertEquals(4, effective.level(ModEnchantments.DEVOUR.identifier()));
        assertEquals(1, effective.entries().size());
        service.remove(sword, sword, true);
        assertTrue(resolver.virtualEnchantments(sword).isEmpty());
    }

    @Test void newPassiveThresholdsAndBucketsAreExact() {
        assertTrue(WeaponSkinCombatResolver.strictlyUnderHealthThreshold(39.999, 100, .40));
        assertFalse(WeaponSkinCombatResolver.strictlyUnderHealthThreshold(40, 100, .40));
        assertTrue(WeaponSkinCombatResolver.lowerHealthPercentage(9, 20, 5, 10));
        assertFalse(WeaponSkinCombatResolver.lowerHealthPercentage(5, 10, 10, 20));
        assertEquals(.05, WeaponSkinCombatResolver.WHISK_OUTGOING);
        assertEquals(0.0, WeaponSkinCombatResolver.whiskOutgoingBonus(0));
        assertEquals(.05, WeaponSkinCombatResolver.whiskOutgoingBonus(1));
        assertEquals(.05, WeaponSkinCombatResolver.whiskOutgoingBonus(10));
        assertEquals(.05, WeaponSkinCombatResolver.GRIM_CHANCE);
        assertEquals(40, WeaponSkinCombatResolver.GRIM_SUPPRESSION_TICKS);
        assertEquals(.033, WeaponSkinCombatResolver.CARVER_OUTGOING);
        assertEquals(1.10, com.cosmicpve.combat.enchantment.LuckBehavior.feedingFrenzyMultiplier(10), 1e-12);
        assertEquals(1.10, com.cosmicpve.combat.enchantment.LuckBehavior.feedingFrenzyMultiplier(99), 1e-12);
    }

    @Test void blackScrollPresentationIsInkSacOnly() throws Exception {
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                "/assets/cosmicpve/models/item/black_scroll.json")))) {
            assertEquals("minecraft:item/ink_sac", JsonParser.parseReader(reader).getAsJsonObject()
                    .getAsJsonObject("textures").get("layer0").getAsString());
        }
    }

    private String itemModel(String name) throws Exception {
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                "/assets/cosmicpve/items/" + name + ".json")))) {
            return JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("model")
                    .get("model").getAsString();
        }
    }

    private void assertAttributesPreserved(Identifier skinId, net.minecraft.world.item.Item item) {
        var weapon = new ItemStack(item);
        var before = weapon.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
        double damage = before.compute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                1.0, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        double speed = before.compute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                4.0, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        var skin = WeaponSkinItemFactory.create(skinId);
        assertEquals(WeaponSkinApplicationService.ApplyOutcome.SUCCESS,
                service.apply(skin, weapon, skin, weapon));
        var after = weapon.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
        assertEquals(before, after);
        assertEquals(damage, after.compute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE,
                1.0, net.minecraft.world.entity.EquipmentSlot.MAINHAND));
        assertEquals(speed, after.compute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED,
                4.0, net.minecraft.world.entity.EquipmentSlot.MAINHAND));
    }

    private static void assertSkinLore(
            Identifier id, int color, WeaponSkinDefinition.WeaponKind kind, int effectLines) {
        var definition = WeaponSkinDefinitions.find(id).orElseThrow();
        assertEquals(color, definition.nameColor());
        assertEquals(kind, definition.weaponKind());
        var lines = WeaponSkinLore.applicationItem(definition);
        var title = WeaponSkinItemFactory.create(id).getHoverName();
        assertEquals("Item Skin (" + definition.displayName().getString() + ")", title.getString());
        assertEquals(color, title.getSiblings().getFirst().getStyle().getColor().getValue());
        assertEquals(effectLines + 6, lines.size());
        for (int index = 0; index < effectLines; index++) {
            assertEquals(0xFF5555,
                    lines.get(index).getStyle().getColor().getValue());
        }
        assertEquals("", lines.get(effectLines).getString());
        assertEquals("Attach this skin to any " + kind.name(), lines.get(effectLines + 1).getString());
        var applicabilityClass = lines.get(effectLines + 1).getSiblings().getFirst();
        assertEquals(0xFFFFFF, applicabilityClass.getStyle().getColor().getValue());
        assertTrue(applicabilityClass.getStyle().isUnderlined());
        assertTrue(applicabilityClass.getStyle().isItalic());
        assertEquals("to over-ride its visual appearance.", lines.get(effectLines + 2).getString());
        assertEquals("", lines.get(effectLines + 3).getString());
        for (int index = effectLines + 4; index < lines.size(); index++) {
            assertEquals(net.minecraft.ChatFormatting.GRAY.getColor(),
                    lines.get(index).getStyle().getColor().getValue());
        }
        assertEquals("Drag n' Drop onto item to attach.", lines.get(effectLines + 4).getString());
        assertEquals("Right-Click item to detach skin.", lines.get(effectLines + 5).getString());
        assertEquals(color, WeaponSkinLore.active(definition).getStyle().getColor().getValue());
        assertTrue(WeaponSkinLore.active(definition).getStyle().isBold());
        assertEquals(effectLines, definition.effectDescription().size());
    }
}
