package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.data.component.EnchantmentOrbData;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import org.junit.jupiter.api.Test;

class OrbFoundationTest {
    @Test void orbDataRequiresInclusiveOneToOneHundredRates() {
        assertEquals(1, new EnchantmentOrbData(1, 1, 1).successRate());
        assertEquals(100, new EnchantmentOrbData(1, 100, 100).destroyRate());
        assertThrows(IllegalArgumentException.class, () -> new EnchantmentOrbData(1, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new EnchantmentOrbData(1, 1, 101));
    }

    @Test void armorAndWeaponCapsHaveExactUpgradeBounds() {
        assertEquals(8, OrbType.ARMOR.maximumCapacity());
        assertEquals(10, OrbType.WEAPON.maximumCapacity());
        assertTrue(OrbType.ARMOR.canUpgradeBonus(2));
        assertFalse(OrbType.ARMOR.canUpgradeBonus(3));
        assertTrue(OrbType.WEAPON.canUpgradeBonus(4));
        assertFalse(OrbType.WEAPON.canUpgradeBonus(5));
    }

    @Test void orbItemsAreNonStackingAndNeverForceGlint() {
        assertEquals(1, EnchantmentOrbItem.MAX_STACK_SIZE);
        assertFalse(EnchantmentOrbItem.FORCE_GLINT);
        assertEquals(0x55FF55,EnchantmentOrbItem.NAME_COLOR);
    }

    @Test void bothOrbModelsUseTheVanillaEyeOfEnderTexture() throws Exception {
        assertEquals("minecraft:item/ender_eye", texture("armor_enchantment_orb"));
        assertEquals("minecraft:item/ender_eye", texture("weapon_enchantment_orb"));
    }

    private String texture(String name) throws Exception {
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(getClass().getResourceAsStream(
                "/assets/cosmicpve/models/item/" + name + ".json")))) {
            return JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("textures").get("layer0").getAsString();
        }
    }
}
