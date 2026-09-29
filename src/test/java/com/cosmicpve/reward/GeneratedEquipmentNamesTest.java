package com.cosmicpve.reward;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.content.definition.reward.GeneratedEquipmentCategory;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class GeneratedEquipmentNamesTest {
    @Test void weaponSelectionUsesThreeThreeTwoTwoDistribution() {
        var counts = new int[4];
        var random = RandomSource.create(98271);
        var items = List.of(Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.BOW, Items.CROSSBOW);
        for (int draw = 0; draw < 100_000; draw++) {
            int index = items.indexOf(GeneratedEquipmentService.selectItem(GeneratedEquipmentCategory.RANDOM_WEAPON, random));
            assertTrue(index >= 0);
            counts[index]++;
        }
        assertArrayEquals(new int[]{30_000, 30_000, 20_000, 20_000},
                new int[]{roundThousand(counts[0]), roundThousand(counts[1]),
                        roundThousand(counts[2]), roundThousand(counts[3])});
    }

    @Test void everyNameUsesOnlyItsEquipmentClassNounsAndOneOptionalSuffix() {
        var items = List.of(Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS,
                Items.IRON_BOOTS, Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.BOW, Items.CROSSBOW);
        var armorNouns = items.subList(0, 4).stream().flatMap(item -> GeneratedEquipmentNames.nouns(item).stream()).toList();
        var weaponNouns = items.subList(4, 8).stream().flatMap(item -> GeneratedEquipmentNames.nouns(item).stream()).toList();
        var random = RandomSource.create(51);
        for (var item : items) {
            for (int draw = 0; draw < 100; draw++) {
                String name = GeneratedEquipmentNames.roll(item, random);
                assertFalse(name.isBlank() || name.contains("  "));
                assertTrue(GeneratedEquipmentNames.nouns(item).stream().anyMatch(noun -> name.contains(" " + noun)));
                assertTrue(name.split(" of ", -1).length <= 2);
                var excluded = items.indexOf(item) < 4 ? weaponNouns : armorNouns;
                assertTrue(excluded.stream().noneMatch(noun -> name.contains(" " + noun)));
            }
        }
    }

    private static int roundThousand(int value) { return ((value + 500) / 1000) * 1000; }
}
