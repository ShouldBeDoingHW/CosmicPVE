package com.cosmicpve.spacechest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.Objects;
import net.minecraft.sounds.SoundEvents;
import org.junit.jupiter.api.Test;

class SpaceChestPresentationTest {
    @Test void everyTierCreatesAnIndependentNonStackableChest() {
        for (SpaceChestTier tier : SpaceChestTier.values()) {
            var first = SpaceChests.create(tier);
            var second = SpaceChests.create(tier);
            assertEquals(1, first.getMaxStackSize());
            assertEquals(tier, first.get(ModDataComponents.SPACE_CHEST.get()).tier());
            assertEquals(2, SpaceChests.createMany(tier, 2).size());
            assertEquals(1, first.getCount());
            assertEquals(1, second.getCount());
        }
        assertEquals(1, ModItems.SPACE_CHEST.get().getDefaultMaxStackSize());
    }

    @Test void itemDefinitionUsesMinecraftSpecialChestRendererAndNormalTexture() throws Exception {
        try (var reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(
                "/assets/cosmicpve/items/space_chest.json")))) {
            var model = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("model");
            assertEquals("minecraft:special", model.get("type").getAsString());
            assertEquals("minecraft:item/chest", model.get("base").getAsString());
            assertEquals("minecraft:chest", model.getAsJsonObject("model").get("type").getAsString());
            assertEquals("minecraft:normal", model.getAsJsonObject("model").get("texture").getAsString());
        }
    }

    @Test void soundPolicyUsesCanonicalEventsAndDoesNotDuplicateClosedOrClaimedTransitions() {
        assertEquals(SoundEvents.ARMOR_EQUIP_NETHERITE.value(), SpaceChestSounds.initialSelection());
        assertEquals(SoundEvents.CHICKEN_EGG, SpaceChestSounds.missedRewardsCleared());
        assertEquals(SoundEvents.EXPERIENCE_ORB_PICKUP, SpaceChestSounds.selectedRewardRevealed());
        assertEquals(SoundEvents.CHEST_CLOSE, SpaceChestSounds.committedMenuClosed());
        assertFalse(SpaceChestSounds.playsCloseSound(SpaceChestPhase.SELECTING));
        assertTrue(SpaceChestSounds.playsCloseSound(SpaceChestPhase.COMMITTED));
    }
}
