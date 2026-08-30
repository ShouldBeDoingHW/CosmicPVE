package com.cosmicpve.combat.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.junit.jupiter.api.Test;

class WeaponDamageNormalizationTest {
    @Test
    void everyVanillaSwordUsesItsMatchingAxesActualBaseDamage() {
        for (Pair pair : pairs()) {
            ItemAttributeModifiers axe = pair.axe().components().getOrDefault(
                    DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            double axeDamage = axe.compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND);
            double normalizedSwordDamage = LegacyCombatService.normalizedSwordAttributes(pair.sword(), pair.axe())
                    .compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND);
            assertEquals(axeDamage, normalizedSwordDamage, 0.00001, pair.sword().toString());
        }
    }

    @Test
    void normalizedTotalsMatchPinnedVanillaAxeValues() {
        double[] expected = {7.0, 9.0, 9.0, 7.0, 9.0, 9.0, 10.0};
        for (int index = 0; index < pairs().size(); index++) {
            Pair pair = pairs().get(index);
            double actual = LegacyCombatService.normalizedSwordAttributes(pair.sword(), pair.axe())
                    .compute(Attributes.ATTACK_DAMAGE, 1.0, EquipmentSlot.MAINHAND);
            assertEquals(expected[index], actual, 0.00001);
        }
    }

    private static List<Pair> pairs() {
        return List.of(
                new Pair(Items.WOODEN_SWORD, Items.WOODEN_AXE),
                new Pair(Items.COPPER_SWORD, Items.COPPER_AXE),
                new Pair(Items.STONE_SWORD, Items.STONE_AXE),
                new Pair(Items.GOLDEN_SWORD, Items.GOLDEN_AXE),
                new Pair(Items.IRON_SWORD, Items.IRON_AXE),
                new Pair(Items.DIAMOND_SWORD, Items.DIAMOND_AXE),
                new Pair(Items.NETHERITE_SWORD, Items.NETHERITE_AXE));
    }

    private record Pair(Item sword, Item axe) {}
}
