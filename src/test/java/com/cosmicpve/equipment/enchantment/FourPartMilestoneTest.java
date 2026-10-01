package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.CosmicPVE;
import com.cosmicpve.combat.enchantment.BlackoutBehavior;
import com.cosmicpve.equipment.accessory.AmuletCombatService;
import com.cosmicpve.equipment.accessory.AmuletDefinition;
import com.cosmicpve.equipment.skin.WeaponSkinDefinitions;
import com.cosmicpve.registry.ModEnchantments;
import com.google.gson.JsonParser;
import java.util.List;
import javax.imageio.ImageIO;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class FourPartMilestoneTest {
    @Test void superbreakerMetadataDurationAndItemCooldownLength() {
        assertEquals(CosmicEnchantmentTier.ELITE, CosmicEnchantmentSpecs.SUPERBREAKER.tier());
        assertEquals("pickaxe", CosmicEnchantmentSpecs.SUPERBREAKER.equipmentApplicability());
        assertEquals(10, CosmicEnchantmentSpecs.SUPERBREAKER.maxLevel());
        assertEquals(220, SuperbreakerService.durationTicks(1));
        assertEquals(400, SuperbreakerService.durationTicks(10));
        assertEquals(4, SuperbreakerService.HASTE_AMPLIFIER);
        assertEquals(2400, SuperbreakerService.COOLDOWN_TICKS);
    }

    @Test void highlightUsesMasteryAndExactSixteenCubedBounds() {
        assertEquals(CosmicEnchantmentTier.MASTERY, CosmicEnchantmentSpecs.HIGHLIGHT.tier());
        assertEquals("pickaxe", CosmicEnchantmentSpecs.HIGHLIGHT.equipmentApplicability());
        assertEquals(1, CosmicEnchantmentSpecs.HIGHLIGHT.maxLevel());
        assertEquals(0.03, HighlightService.BASE_CHANCE);
        assertEquals(300, HighlightService.DURATION_TICKS);
        try (var reader = new java.io.InputStreamReader(java.util.Objects.requireNonNull(getClass()
                .getResourceAsStream("/assets/cosmicpve/lang/en_us.json")))) {
            assertEquals("Breaking an ore may highlight nearby ores of the same type for 15s.",
                    JsonParser.parseReader(reader).getAsJsonObject()
                            .get("enchantment.cosmicpve.highlight.description").getAsString());
        } catch (java.io.IOException exception) {
            throw new java.io.UncheckedIOException(exception);
        }
        BlockPos center = new BlockPos(40, 80, -20);
        var all = HighlightService.scanMatching(center, ignored -> true);
        assertEquals(4096, all.size());
        assertTrue(all.contains(center.offset(-8, -8, -8)));
        assertTrue(all.contains(center.offset(7, 7, 7)));
        assertFalse(all.contains(center.offset(8, 0, 0)));
        assertFalse(all.contains(center.offset(0, -9, 0)));
        assertEquals(List.of(center.offset(2, 1, -3)), HighlightService.scanMatching(center,
                pos -> pos.equals(center.offset(2, 1, -3))));
    }

    @Test void luauHealthThresholdAndTridentVirtualBlackoutAreCanonical() {
        assertEquals(0x26EDAD, AmuletDefinition.LUAU_LEI.color());
        assertEquals("Amulet: Luau Lei", AmuletDefinition.LUAU_LEI.displayName());
        assertEquals(0.15, AmuletCombatService.LUAU_LEI_BONUS);
        assertTrue(AmuletCombatService.aboveLuauThreshold(16.01F, 20));
        assertFalse(AmuletCombatService.aboveLuauThreshold(16, 20));
        assertFalse(AmuletCombatService.aboveLuauThreshold(15, 20));
        assertFalse(AmuletCombatService.aboveLuauThreshold(8, 10));
        var skin = WeaponSkinDefinitions.find(WeaponSkinDefinitions.TRIDENT_OF_THE_DEEP).orElseThrow();
        assertEquals(0x10B29C, skin.nameColor());
        assertEquals(6, skin.virtualEnchantments().getFirst().level());
        assertEquals(ModEnchantments.BLACKOUT.identifier(), skin.virtualEnchantments().getFirst().enchantmentId());
        assertEquals(4, CosmicEnchantmentSpecs.BLACKOUT.maxLevel());
        assertEquals(0.12, BlackoutBehavior.chance(6));
        assertEquals(120, BlackoutBehavior.durationTicks(6));
        var effective = new EffectiveEnchantmentsResolver().resolveSources(
                List.of(new ActualEnchantmentGrant(ModEnchantments.BLACKOUT.identifier(), 4, CosmicPVE.id("real"))),
                skin.virtualEnchantments());
        assertEquals(1, effective.entries().size());
        assertEquals(6, effective.level(ModEnchantments.BLACKOUT.identifier()));
        assertTrue(effective.get(ModEnchantments.BLACKOUT.identifier()).orElseThrow().hasVirtualSource());
    }

    @Test void authoredLuauAndFlatTridentAssetsResolveAndDecode() throws Exception {
        try (var stream = getClass().getResourceAsStream("/assets/cosmicpve/textures/item/trident_of_the_deep.png")) {
            assertNotNull(stream);
            var image = ImageIO.read(stream);
            assertNotNull(image);
            assertEquals(32, image.getWidth());
            assertEquals(32, image.getHeight());
        }
        assertNotNull(getClass().getResource("/assets/cosmicpve/models/item/amulet/luau_lei.obj"));
        try (var stream = getClass().getResourceAsStream("/assets/cosmicpve/textures/item/amulet/luau_lei.png")) {
            assertNotNull(stream);
            var image = ImageIO.read(stream);
            assertNotNull(image);
            assertEquals(16, image.getWidth());
            assertEquals(16, image.getHeight());
        }
        try (var stream = getClass().getResourceAsStream("/assets/cosmicpve/models/item/luau_lei_amulet.json")) {
            assertNotNull(stream);
            var model = JsonParser.parseReader(new java.io.InputStreamReader(stream)).getAsJsonObject();
            assertEquals("cosmicpve:models/item/amulet/luau_lei.obj", model.get("model").getAsString());
            assertEquals("cosmicpve:item/amulet/luau_lei",
                    model.getAsJsonObject("textures").get("texture0").getAsString());
        }
        try (var stream = getClass().getResourceAsStream("/assets/cosmicpve/models/item/trident_of_the_deep.json")) {
            assertNotNull(stream);
            var model = JsonParser.parseReader(new java.io.InputStreamReader(stream)).getAsJsonObject();
            assertEquals("cosmicpve:item/trident_of_the_deep",
                    model.getAsJsonObject("textures").get("layer0").getAsString());
        }
    }
}
