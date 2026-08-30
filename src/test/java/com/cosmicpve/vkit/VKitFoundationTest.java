package com.cosmicpve.vkit;

import static org.junit.jupiter.api.Assertions.*;

import com.cosmicpve.data.attachment.VKitProgressionData;
import com.cosmicpve.data.component.VKitCrystalData;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class VKitFoundationTest {
    @Test
    void canonicalDefinitionsUseSettledIdsColorsAndEquipment() {
        assertDefinition(VKitDefinition.PHOENIX, "cosmicpve:phoenix", 0x3F0761,
                Items.IRON_BOOTS, Items.DIAMOND_SWORD, "Sandals of the Phoenix", "Sikanda");
        assertDefinition(VKitDefinition.OGRE, "cosmicpve:ogre", 0x18660A,
                Items.IRON_CHESTPLATE, Items.CROSSBOW, "Fat Tummy", "The Gutbuster");
        assertDefinition(VKitDefinition.JUDGEMENT, "cosmicpve:judgement", 0x663434,
                Items.IRON_LEGGINGS, Items.DIAMOND_AXE, "Trousers of Retribution", "The Banhammer");
        assertDefinition(VKitDefinition.SLAYER, "cosmicpve:slayer", 0x8C0B0B,
                Items.IRON_HELMET, Items.BOW, "Shroud of War", "Glitched Bow");
    }

    @Test
    void exactPointBudgetsAreSpentWithoutDuplicates() {
        var generator = new VKitEquipmentGenerator();
        int[] expected = {5, 6, 7, 9, 10, 12, 13, 14, 16, 18};
        for (int level = 1; level <= 10; level++) {
            assertEquals(expected[level - 1], VKitEquipmentGenerator.pointBudget(level));
            for (VKitDefinition definition : VKitDefinition.ALL) {
                for (var reward : java.util.List.of(definition.armor(), definition.weapon())) {
                    var pool = reward.pool(level);
                    for (int seed = 0; seed < 20; seed++) {
                        Map<com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpec, Integer> allocation =
                                generator.allocate(pool, expected[level - 1], RandomSource.create(seed));
                        assertEquals(expected[level - 1], allocation.values().stream().mapToInt(Integer::intValue).sum());
                        assertEquals(allocation.size(), new HashSet<>(allocation.keySet()).size());
                        assertTrue(allocation.size() <= 5);
                        allocation.forEach((spec, value) -> {
                            assertTrue(pool.contains(spec));
                            assertTrue(value >= 1 && value <= spec.maxLevel());
                        });
                    }
                }
            }
        }
    }

    @Test
    void masteryPoolAdditionsBeginAtLevelEight() {
        for (VKitDefinition definition : VKitDefinition.ALL) {
            for (var reward : java.util.List.of(definition.armor(), definition.weapon())) {
                assertTrue(reward.levelEightAdditions().stream().noneMatch(reward.pool(7)::contains));
                assertTrue(reward.pool(8).containsAll(reward.levelEightAdditions()));
            }
        }
    }

    @Test
    void everyEquipmentPoolExactlyMatchesTheCanonicalDesign() {
        assertPool(VKitDefinition.PHOENIX.weapon(), "rage", "doublestrike", "execute", "greatsword",
                "divine_immolation");
        assertPool(VKitDefinition.SLAYER.weapon(), "virus", "sniper", "lightning", "eagle_eye", "soul_siphon");
        assertPool(VKitDefinition.OGRE.weapon(), "virus", "eagle_eye", "lightning", "venom", "snare");
        assertPool(VKitDefinition.JUDGEMENT.weapon(), "rage", "devour", "pummel", "insanity", "soul_tether");
        assertPool(VKitDefinition.SLAYER.armor(), "molten", "armored", "voodoo", "angelic", "mortal_coil");
        assertPool(VKitDefinition.OGRE.armor(), "aegis", "armored", "angelic", "leadership");
        assertPool(VKitDefinition.JUDGEMENT.armor(), "plague_carrier", "armored", "cactus", "self_destruct", "molten");
        assertPool(VKitDefinition.PHOENIX.armor(), "ender_walker", "gears", "dodge", "luck", "phoenix");
    }

    @Test
    void deterministicAllocationFillsSelectedEnchantmentsToMaxAndOnlyFinalMayBePartial() {
        var pool = VKitDefinition.PHOENIX.weapon().basePool();
        var allocation = new VKitEquipmentGenerator().allocate(pool, 10, ignored -> 0);
        assertEquals(pool.get(0).maxLevel(), allocation.get(pool.get(0)));
        assertEquals(pool.get(1).maxLevel(), allocation.get(pool.get(1)));
        assertEquals(1, allocation.get(pool.get(2)));
        assertFalse(allocation.containsKey(pool.get(3)));
        assertEquals(10, allocation.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void crystalProgressionAdvancesThroughTenAndThenRepeatsTen() {
        assertEquals(1, VKitProgressionService.nextRollLevel(0));
        assertEquals(2, VKitProgressionService.nextRollLevel(1));
        assertEquals(10, VKitProgressionService.nextRollLevel(9));
        assertEquals(10, VKitProgressionService.nextRollLevel(10));
    }

    @Test
    void equipmentRollUsesOneExactBooleanBranch() {
        var generator = new VKitEquipmentGenerator();
        assertEquals(VKitEquipmentType.ARMOR, generator.chooseType(() -> true));
        assertEquals(VKitEquipmentType.WEAPON, generator.chooseType(() -> false));
    }

    @Test
    void progressionCodecIsVersionedStableAndDefaultsMissingKitsToZero() {
        var original = new VKitProgressionData(VKitProgressionData.CURRENT_DATA_VERSION,
                Map.of(VKitDefinition.PHOENIX.id(), 7, VKitDefinition.SLAYER.id(), 10));
        var json = VKitProgressionData.CODEC.codec().encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        var decoded = VKitProgressionData.CODEC.codec().parse(JsonOps.INSTANCE, json).getOrThrow();
        assertEquals(7, decoded.level(VKitDefinition.PHOENIX.id()));
        assertEquals(10, decoded.level(VKitDefinition.SLAYER.id()));
        assertEquals(0, decoded.level(VKitDefinition.OGRE.id()));
        assertThrows(IllegalStateException.class, () -> VKitProgressionData.CODEC.codec().parse(
                JsonOps.INSTANCE, JsonParser.parseString("{\"levels\":{\"cosmicpve:phoenix\":11}}"))
                .getOrThrow());
    }

    @Test
    void registeredCrystalsCarryTypedIdentityGlintAndCanonicalPresentation() {
        assertCrystal(new ItemStack(ModItems.PHOENIX_VKIT_CRYSTAL.get()), VKitDefinition.PHOENIX);
        assertCrystal(new ItemStack(ModItems.OGRE_VKIT_CRYSTAL.get()), VKitDefinition.OGRE);
        assertCrystal(new ItemStack(ModItems.JUDGEMENT_VKIT_CRYSTAL.get()), VKitDefinition.JUDGEMENT);
        assertCrystal(new ItemStack(ModItems.SLAYER_VKIT_CRYSTAL.get()), VKitDefinition.SLAYER);
    }

    @Test
    void everyCrystalVariantIsNonStackable() {
        assertEquals(1, ModItems.PHOENIX_VKIT_CRYSTAL.get().getDefaultMaxStackSize());
        assertEquals(1, ModItems.OGRE_VKIT_CRYSTAL.get().getDefaultMaxStackSize());
        assertEquals(1, ModItems.JUDGEMENT_VKIT_CRYSTAL.get().getDefaultMaxStackSize());
        assertEquals(1, ModItems.SLAYER_VKIT_CRYSTAL.get().getDefaultMaxStackSize());
    }

    @Test
    void singleCrystalRedemptionCommitsLockedMidAndLevelTenExactlyOnce() {
        assertRedemption(0, 1);
        assertRedemption(4, 5);
        assertRedemption(10, 10);
    }

    @Test
    void finalLegacyStackUnitCannotBeOverwrittenByTheUseResult() {
        ItemStack crystal = new ItemStack(ModItems.PHOENIX_VKIT_CRYSTAL.get());
        crystal.setCount(2); // Synthetic pre-hotfix development stack.
        ItemStack firstReward = new ItemStack(Items.IRON_BOOTS);
        ItemStack finalReward = new ItemStack(Items.DIAMOND_SWORD);
        AtomicInteger progressionUpdates = new AtomicInteger();
        AtomicInteger deliveries = new AtomicInteger();
        AtomicInteger feedback = new AtomicInteger();

        var firstResult = VKitCrystalItem.commitRedemption(crystal, 1, firstReward,
                ignored -> progressionUpdates.incrementAndGet(),
                delivered -> {
                    assertSame(firstReward, delivered);
                    deliveries.incrementAndGet();
                }, feedback::incrementAndGet);
        assertEquals(1, crystal.getCount());
        assertSame(crystal, firstResult.heldItemTransformedTo(),
                "a synthetic legacy stack keeps its remaining crystal in the used hand");

        var finalResult = VKitCrystalItem.commitRedemption(crystal, 2, finalReward,
                ignored -> progressionUpdates.incrementAndGet(),
                delivered -> deliveries.incrementAndGet(), feedback::incrementAndGet);

        assertTrue(crystal.isEmpty());
        assertSame(finalReward, finalResult.heldItemTransformedTo(),
                "the generated reward must authoritatively replace the final crystal in hand");
        assertEquals(2, progressionUpdates.get());
        assertEquals(1, deliveries.get(), "only a redemption leaving crystals in hand uses inventory delivery");
        assertEquals(2, feedback.get());
    }

    @Test
    void successfulRedemptionUsesExactlyOnePlayerLevelUpCue() {
        assertSame(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, VKitCrystalFeedback.successSound());
    }

    @Test
    void flavorUsesKitColorItalicAndUnderlinesTheFullKitIdentity() {
        for (VKitDefinition definition : VKitDefinition.ALL) {
            var flavor = VKitCrystalItem.flavor(definition);
            assertEquals(definition.color(), flavor.getStyle().getColor().getValue());
            assertTrue(flavor.getStyle().isItalic());
            var kitIdentity = flavor.getSiblings().getFirst();
            assertEquals(definition.displayName() + " Vkit", kitIdentity.getString());
            assertEquals(definition.color(), kitIdentity.getStyle().getColor().getValue());
            assertTrue(kitIdentity.getStyle().isItalic());
            assertTrue(kitIdentity.getStyle().isUnderlined());
        }
    }

    @Test
    void crystalModelsUseTheCanonicalVanillaDyes() throws Exception {
        Path root = Path.of(System.getProperty("cosmicpve.projectDir"),
                "src/main/resources/assets/cosmicpve/items");
        assertModel(root, "phoenix_vkit_crystal.json", "minecraft:item/purple_dye");
        assertModel(root, "ogre_vkit_crystal.json", "minecraft:item/green_dye");
        assertModel(root, "judgement_vkit_crystal.json", "minecraft:item/black_dye");
        assertModel(root, "slayer_vkit_crystal.json", "minecraft:item/red_dye");
    }

    private static void assertDefinition(VKitDefinition definition, String id, int color,
            net.minecraft.world.item.Item armor, net.minecraft.world.item.Item weapon,
            String armorName, String weaponName) {
        assertEquals(id, definition.id().toString());
        assertEquals(color, definition.color());
        assertEquals(armor, definition.armor().item());
        assertEquals(weapon, definition.weapon().item());
        assertEquals(armorName, definition.armor().displayName());
        assertEquals(weaponName, definition.weapon().displayName());
    }

    private static void assertRedemption(int currentLevel, int expectedLevel) {
        ItemStack crystal = new ItemStack(ModItems.PHOENIX_VKIT_CRYSTAL.get());
        ItemStack reward = new ItemStack(Items.DIAMOND_SWORD);
        AtomicInteger committedLevel = new AtomicInteger(-1);
        AtomicInteger deliveries = new AtomicInteger();
        AtomicInteger feedback = new AtomicInteger();
        int rollLevel = VKitProgressionService.nextRollLevel(currentLevel);

        var result = VKitCrystalItem.commitRedemption(crystal, rollLevel, reward, committedLevel::set,
                delivered -> {
                    deliveries.incrementAndGet();
                }, feedback::incrementAndGet);

        assertEquals(expectedLevel, committedLevel.get());
        assertTrue(crystal.isEmpty());
        assertEquals(0, deliveries.get(), "a final crystal returns its reward as the authoritative hand replacement");
        assertEquals(1, feedback.get());
        assertSame(reward, result.heldItemTransformedTo());
    }

    private static void assertCrystal(ItemStack stack, VKitDefinition definition) {
        VKitCrystalData data = stack.get(ModDataComponents.VKIT_CRYSTAL.get());
        assertNotNull(data);
        assertEquals(definition.id(), data.kitId());
        assertTrue(stack.hasFoil());
        assertTrue(stack.getHoverName().getStyle().isBold());
        assertEquals(0xFFFFFF, stack.getHoverName().getStyle().getColor().getValue());
        assertEquals(definition.color(), stack.getHoverName().getSiblings().getFirst().getStyle().getColor().getValue());
        assertTrue(stack.getHoverName().getSiblings().getFirst().getStyle().isItalic());
        assertTrue(stack.getHoverName().getSiblings().getFirst().getStyle().isUnderlined());
    }

    private static void assertModel(Path root, String file, String expected) throws Exception {
        var json = JsonParser.parseString(Files.readString(root.resolve(file))).getAsJsonObject();
        assertEquals(expected, json.getAsJsonObject("model").get("model").getAsString());
    }

    private static void assertPool(VKitDefinition.EquipmentReward reward, String... expected) {
        var actual = reward.pool(8).stream().map(spec -> spec.id().getPath()).collect(java.util.stream.Collectors.toSet());
        assertEquals(java.util.Set.of(expected), actual);
        assertEquals(expected.length - reward.levelEightAdditions().size(), reward.basePool().size());
    }
}
