package com.cosmicpve.equipment.enchantment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cosmicpve.data.component.CosmicEnchantmentBookData;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.junit.jupiter.api.Test;

class CosmicLoreTest {
    @Test void genericBookLoreKeepsSemanticOrderStylesAndSpecMetadata() {
        var data = new CosmicEnchantmentBookData(1, CosmicEnchantmentSpecs.EXECUTE.id(), 5, 83, 71);
        List<Component> lines = CosmicBookLore.lines(data, CosmicEnchantmentSpecs.EXECUTE);
        assertEquals(5, lines.size());
        assertEquals("tooltip.cosmicpve.book.success", key(lines.get(0)));
        assertEquals(ItemApplicationColors.SUCCESS, lines.get(0).getStyle().getColor().getValue());
        assertEquals("tooltip.cosmicpve.book.destroy", key(lines.get(1)));
        assertEquals(ItemApplicationColors.DESTROY, lines.get(1).getStyle().getColor().getValue());
        assertEquals("tooltip.cosmicpve.book.description", key(lines.get(2)));
        assertEquals(CosmicEnchantmentTier.ELITE.tooltipColor(), lines.get(2).getStyle().getColor().getValue());
        assertEquals("tooltip.cosmicpve.applicability.sword", key(lines.get(3)));
        assertEquals(ChatFormatting.GRAY.getColor(), lines.get(3).getStyle().getColor().getValue());
        assertEquals("tooltip.cosmicpve.book.instruction", key(lines.get(4)));
        assertEquals(ChatFormatting.GRAY.getColor(), lines.get(4).getStyle().getColor().getValue());
        assertEquals("enchantment.cosmicpve.execute.description", key(CosmicEnchantmentSpecs.EXECUTE.description()));
    }

    @Test void applicabilityMetadataCoversEveryCurrentEquipmentClass() {
        assertEquals("tooltip.cosmicpve.applicability.sword", key(CosmicEnchantmentSpecs.EXECUTE.applicability()));
        assertEquals("tooltip.cosmicpve.applicability.axe", key(CosmicEnchantmentSpecs.BLEED.applicability()));
        assertEquals("tooltip.cosmicpve.applicability.bow_or_crossbow", key(CosmicEnchantmentSpecs.VENOM.applicability()));
        assertEquals("tooltip.cosmicpve.applicability.chestplate", key(CosmicEnchantmentSpecs.AEGIS.applicability()));
        assertEquals("tooltip.cosmicpve.applicability.any_armor", key(CosmicEnchantmentSpecs.MOLTEN.applicability()));
        assertEquals("tooltip.cosmicpve.applicability.leggings", key(CosmicEnchantmentSpecs.NUTRITION.applicability()));
        assertEquals("tooltip.cosmicpve.applicability.boots_or_leggings", key(CosmicEnchantmentSpecs.LUCK.applicability()));
        assertTrue(CosmicEnchantmentSpecs.ALL.stream().allMatch(spec -> !spec.description().getString().isBlank()));
    }

    @Test void everyRealSpecificationHasLocalizedGenericDescriptionAndApplicability() throws Exception {
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(
                getClass().getResourceAsStream("/assets/cosmicpve/lang/en_us.json")), StandardCharsets.UTF_8)) {
            var language = JsonParser.parseReader(reader).getAsJsonObject();
            for (var spec : CosmicEnchantmentSpecs.ALL) {
                assertTrue(language.has("enchantment." + spec.id().getNamespace() + "."
                        + spec.id().getPath() + ".description"), spec.id().toString());
                assertTrue(language.has("tooltip.cosmicpve.applicability." + spec.equipmentApplicability()),
                        spec.equipmentApplicability());
            }
        }
    }

    private static String key(Component component) {
        return ((TranslatableContents) component.getContents()).getKey();
    }
}
