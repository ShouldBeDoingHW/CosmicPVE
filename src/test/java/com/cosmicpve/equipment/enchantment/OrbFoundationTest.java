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
        assertEquals(0x55FFFF, EnchantmentOrbItem.ARMOR_NAME_COLOR);
        assertEquals(0xFFAA00, EnchantmentOrbItem.WEAPON_NAME_COLOR);
    }

    @Test void orbPresentationUsesExactFamilyNamesColorsRatesAndMechanicLore() {
        var factory = new EnchantingRewardItemFactory();
        var armor = factory.orb(OrbType.ARMOR, 73, 41);
        var weapon = factory.orb(OrbType.WEAPON, 64, 92);
        assertEquals("Armor Enchantment Orb", armor.getHoverName().getString());
        assertEquals(0x55FFFF, armor.getHoverName().getStyle().getColor().getValue());
        assertTrue(armor.getHoverName().getStyle().isBold());
        assertEquals("Weapon Enchantment Orb", weapon.getHoverName().getString());
        assertEquals(0xFFAA00, weapon.getHoverName().getStyle().getColor().getValue());
        assertTrue(weapon.getHoverName().getStyle().isBold());
        var armorLore = EnchantmentOrbItem.lore(armor, armor.get(com.cosmicpve.registry.ModDataComponents.ENCHANTMENT_ORB.get()));
        var weaponLore = EnchantmentOrbItem.lore(weapon, weapon.get(com.cosmicpve.registry.ModDataComponents.ENCHANTMENT_ORB.get()));
        assertEquals("Expand the weave. Make room for one more enchantment.", armorLore.getFirst().getString());
        assertTrue(armorLore.getFirst().getStyle().isItalic());
        assertEquals("SUCCESS: 73%", armorLore.get(2).getString());
        assertTrue(armorLore.get(2).getStyle().isBold());
        assertEquals("DESTROY: 41%", armorLore.get(3).getString());
        assertEquals("Armor capacity: 5 → 8 max", armorLore.get(5).getString());
        assertEquals("Carve another channel for power.", weaponLore.getFirst().getString());
        assertEquals("SUCCESS: 64%", weaponLore.get(2).getString());
        assertEquals("DESTROY: 92%", weaponLore.get(3).getString());
        assertEquals("Weapon capacity: 5 → 10 max", weaponLore.get(5).getString());
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
