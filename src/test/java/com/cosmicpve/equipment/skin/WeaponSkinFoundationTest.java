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
                WeaponSkinDefinitions.SEASONS_BEATINGS, WeaponSkinDefinitions.STORMBRINGER), WeaponSkinDefinitions.ids());
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
    }

    @Test void canonicalLoreUsesPerSkinNameColorsAndSharedYellowGrayPresentation() {
        assertSkinLore(WeaponSkinDefinitions.BOOSTED_CHAINSAW, 0xCCA00A,
                WeaponSkinDefinition.WeaponKind.AXE, 1);
        assertSkinLore(WeaponSkinDefinitions.MAUIS_HOOK, 0x404242,
                WeaponSkinDefinition.WeaponKind.SWORD, 2);
        assertSkinLore(WeaponSkinDefinitions.STORMBRINGER, 0x224B57,
                WeaponSkinDefinition.WeaponKind.AXE, 2);
        assertSkinLore(WeaponSkinDefinitions.SEASONS_BEATINGS, 0x1B943A,
                WeaponSkinDefinition.WeaponKind.SWORD, 2);
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
        assertEquals(3, grants.getFirst().level());
        assertEquals(WeaponSkinDefinitions.BOOSTED_CHAINSAW, grants.getFirst().sourceId());
        assertFalse(EnchantmentHelper.hasAnyEnchantments(axe));
        var effective = new EffectiveEnchantmentsResolver().resolveSources(
                List.of(new ActualEnchantmentGrant(ModEnchantments.DOUBLESTRIKE.identifier(), 2,
                        CosmicPVE.id("actual_item"))), grants);
        assertEquals(3, effective.level(ModEnchantments.DOUBLESTRIKE.identifier()));
        assertTrue(effective.get(ModEnchantments.DOUBLESTRIKE.identifier()).orElseThrow().provenance().stream()
                .anyMatch(value -> value.kind() == EnchantmentSourceKind.VIRTUAL
                        && value.sourceId().equals(WeaponSkinDefinitions.BOOSTED_CHAINSAW)));
        service.remove(axe, axe, true);
        assertTrue(resolver.virtualEnchantments(axe).isEmpty());
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
        for (String name : List.of("boosted_chainsaw", "mauis_hook", "seasons_beatings", "stormbringer")) {
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

    private static void assertSkinLore(
            Identifier id, int color, WeaponSkinDefinition.WeaponKind kind, int effectLines) {
        var definition = WeaponSkinDefinitions.find(id).orElseThrow();
        assertEquals(color, definition.nameColor());
        assertEquals(kind, definition.weaponKind());
        var lines = WeaponSkinLore.applicationItem(definition);
        assertEquals(effectLines + 3, lines.size());
        for (int index = 0; index < effectLines; index++) {
            assertEquals(net.minecraft.ChatFormatting.YELLOW.getColor(),
                    lines.get(index).getStyle().getColor().getValue());
        }
        for (int index = effectLines; index < lines.size(); index++) {
            assertEquals(net.minecraft.ChatFormatting.GRAY.getColor(),
                    lines.get(index).getStyle().getColor().getValue());
        }
        assertEquals(color, WeaponSkinLore.active(definition).getStyle().getColor().getValue());
        assertTrue(WeaponSkinLore.active(definition).getStyle().isBold());
        assertEquals(effectLines, definition.effectDescription().size());
    }
}
