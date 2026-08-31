package com.cosmicpve.reward.lootbox;

import static org.junit.jupiter.api.Assertions.*;
import com.cosmicpve.combat.api.AttackCategory;
import com.cosmicpve.data.component.SignatureWeaponIdentity;
import com.cosmicpve.equipment.armor.ArmorSetIds;
import com.cosmicpve.registry.ModEnchantments;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class Step8ELootboxTest {
    @Test void signaturePoolIsExactAndUniform() {
        assertEquals(6, SignatureWeaponDefinition.ALL.size());
        assertEquals(Set.of("Phantom Scythe", "Yeti Maul", "Yjiki Claw", "Ranger's Bow",
                "Engineer's Hammer", "Traveler's Space Blaster"), SignatureWeaponDefinition.ALL.stream()
                .map(SignatureWeaponDefinition::displayName).collect(Collectors.toSet()));
        assertEquals(6, SignatureWeaponDefinition.ALL.stream().map(SignatureWeaponDefinition::id).distinct().count());
    }

    @Test void signatureBonusRequiresCorrectCategoryAndResolvedMatchingSet() {
        var melee = new SignatureWeaponIdentity(1, SignatureWeaponDefinition.PHANTOM_SCYTHE.id(),
                ArmorSetIds.PHANTOM, SignatureWeaponIdentity.Kind.MELEE);
        assertTrue(SignatureWeaponCombatService.matches(melee, AttackCategory.MELEE, Optional.of(ArmorSetIds.PHANTOM)));
        assertFalse(SignatureWeaponCombatService.matches(melee, AttackCategory.PROJECTILE, Optional.of(ArmorSetIds.PHANTOM)));
        assertFalse(SignatureWeaponCombatService.matches(melee, AttackCategory.MELEE, Optional.of(ArmorSetIds.YETI)));
        assertFalse(SignatureWeaponCombatService.matches(melee, AttackCategory.MELEE, Optional.empty()));
        var ranged = new SignatureWeaponIdentity(1, SignatureWeaponDefinition.RANGERS_BOW.id(),
                ArmorSetIds.RANGER, SignatureWeaponIdentity.Kind.RANGED);
        assertTrue(SignatureWeaponCombatService.matches(ranged, AttackCategory.PROJECTILE, Optional.of(ArmorSetIds.RANGER)));
        assertFalse(SignatureWeaponCombatService.matches(ranged, AttackCategory.MELEE, Optional.of(ArmorSetIds.RANGER)));
        assertEquals(1.0, SignatureWeaponCombatService.MATCHING_SET_BONUS);
    }

    @Test void cosmicTablePoolAndRatesAreCanonical() {
        assertEquals(Set.of(ModEnchantments.ARMORED, ModEnchantments.ANGELIC, ModEnchantments.RAGE,
                ModEnchantments.OBLITERATE, ModEnchantments.LEADERSHIP, ModEnchantments.SOUL_SIPHON,
                ModEnchantments.LUCK), Set.copyOf(CosmicEnchantmentTableRewards.POOL));
        assertEquals(List.of(50, 75, 100), CosmicEnchantmentTableRewards.ORDINARY_SUCCESS);
        assertEquals(List.of(25, 50), CosmicEnchantmentTableRewards.MASTERY_SUCCESS);
    }

    @Test void adminPoolIsExactlyFourEqualTwentyFiveWeights() {
        assertEquals(4, AdminAbuseRewards.ALL.size());
        assertTrue(AdminAbuseRewards.ALL.stream().allMatch(value -> value.weight() == 25));
        assertEquals(100, AdminAbuseRewards.ALL.stream().mapToInt(AdminAbuseRewards.Outcome::weight).sum());
        var reached = new java.util.HashSet<AdminAbuseRewards.Outcome>();
        for (long seed = 0; seed < 1000; seed++) reached.add(AdminAbuseRewards.select(RandomSource.create(seed)));
        assertEquals(Set.copyOf(AdminAbuseRewards.ALL), reached);
    }

    @Test void adminNamesAndLootboxPresentationUseCanonicalStyles() {
        assertSingleStyle(AdminAbuseRewardFactory.name(AdminAbuseRewards.Outcome.GHOSTLY_VEIL),
                "Ghostly Veil", 0x345FA8);
        assertSingleStyle(AdminAbuseRewardFactory.name(AdminAbuseRewards.Outcome.COVERT_CLOAK),
                "Covert Cloak", 0x345FA8);
        assertSegments(AdminAbuseRewardFactory.name(AdminAbuseRewards.Outcome.NANKADA), "Nankada",
                List.of(0xB8B8B8, 0xFFE578, 0xB8B8B8));
        assertSegments(AdminAbuseRewardFactory.name(AdminAbuseRewards.Outcome.ASHOKA), "Ashoka",
                List.of(0xFFFFFF, 0x397AB8, 0xFFFFFF));
        var name = AnimatedLootboxItem.adminName();
        assertEquals("Admin Abuse", name.getString());
        var letters = name.getSiblings().stream().filter(part -> !part.getString().isBlank()).toList();
        assertEquals(List.of(0xD41432, 0x8F1022, 0xD41432, 0x8F1022, 0xD41432,
                        0x8F1022, 0xD41432, 0x8F1022, 0xD41432, 0x8F1022),
                letters.stream().map(Step8ELootboxTest::color).toList());
        assertTrue(name.getSiblings().stream().allMatch(part -> part.getStyle().isBold()
                && part.getStyle().isItalic() && part.getStyle().isStrikethrough()));
    }

    @Test void lootboxNamesAndSecretCacheFlavorUseFinalPresentation() {
        var table = AnimatedLootboxItem.displayName(AnimatedLootboxItem.Kind.COSMIC_ENCHANTMENT_TABLE);
        assertEquals("Cosmic Enchantment Table", table.getString());
        assertEquals(0x32045C, color(table));
        assertTrue(table.getStyle().isBold());
        var lore = AnimatedLootboxItem.secretWeaponCacheLore();
        assertEquals(1, lore.size());
        assertEquals("The most devastating weapons known to life, all inside a single box...", lore.getFirst().getString());
        assertTrue(lore.getFirst().getStyle().isItalic());
        assertEquals(net.minecraft.ChatFormatting.YELLOW.getColor(), color(lore.getFirst()));
    }

    @Test void itemDefinitionsReferenceCanonicalVanillaModels() {
        assertSpecialChest("secret_weapon_cache", "minecraft:item/ender_chest", "minecraft:ender");
        assertModel("cosmic_enchantment_table", "minecraft:block/enchanting_table");
        assertModel("admin_abuse", "minecraft:block/end_portal_frame");
    }

    private static void assertSpecialChest(String name, String expectedBase, String expectedTexture) {
        var model = itemDefinition(name).getAsJsonObject("model");
        assertEquals("minecraft:special", model.get("type").getAsString());
        assertEquals(expectedBase, model.get("base").getAsString());
        assertEquals("minecraft:chest", model.getAsJsonObject("model").get("type").getAsString());
        assertEquals(expectedTexture, model.getAsJsonObject("model").get("texture").getAsString());
    }

    private static void assertModel(String name, String expected) {
        assertEquals(expected, itemDefinition(name).getAsJsonObject("model").get("model").getAsString());
    }

    private static com.google.gson.JsonObject itemDefinition(String name) {
        String path = "/assets/cosmicpve/items/" + name + ".json";
        try (var reader = new InputStreamReader(java.util.Objects.requireNonNull(
                Step8ELootboxTest.class.getResourceAsStream(path)))) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (java.io.IOException exception) { throw new AssertionError(exception); }
    }

    private static void assertSingleStyle(net.minecraft.network.chat.Component name, String text, int color) {
        assertEquals(text, name.getString());
        assertEquals(color, color(name));
        assertTrue(name.getStyle().isBold() && name.getStyle().isItalic() && name.getStyle().isStrikethrough());
    }

    private static void assertSegments(net.minecraft.network.chat.Component name, String text, List<Integer> colors) {
        assertEquals(text, name.getString());
        var parts = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        parts.add(name); parts.addAll(name.getSiblings());
        assertEquals(colors, parts.stream().map(Step8ELootboxTest::color).toList());
        assertTrue(parts.stream().allMatch(part -> part.getStyle().isBold()
                && part.getStyle().isItalic() && part.getStyle().isStrikethrough()));
    }

    private static int color(net.minecraft.network.chat.Component component) {
        return java.util.Objects.requireNonNull(component.getStyle().getColor()).getValue();
    }
}
