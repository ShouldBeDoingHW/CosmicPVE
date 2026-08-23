package com.cosmicpve.entity.spacepirate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import org.junit.jupiter.api.Test;

class SpacePirateEquipmentServiceTest {
    private final SpacePirateEquipmentService service = new SpacePirateEquipmentService();

    @Test void variantOneRollsEachArmorPieceIndependentlyAndUsesFiftyPercentPummel() {
        var rolls = new ScriptedRolls(
                new boolean[] {true, false, true, false, true},
                new int[] {0, 1, 2, 3});
        var plan = service.generate(SpacePirateVariant.VARIANT_1, rolls);
        assertEquals(4, plan.armor().size());
        assertEquals(SpacePirateEquipmentPlan.ArmorMaterial.DIAMOND, plan.armor().get(0).material());
        assertEquals(SpacePirateEquipmentPlan.ArmorMaterial.IRON, plan.armor().get(1).material());
        assertEquals(1, plan.armor().get(0).protectionLevel());
        assertEquals(4, plan.armor().get(3).protectionLevel());
        assertTrue(plan.pummel());
        assertEquals(8, SpacePirateEquipmentService.VARIANT_1_INSANITY_LEVEL);
        assertEquals(3, SpacePirateEquipmentService.VARIANT_1_PUMMEL_LEVEL);
        rolls.assertExhausted();
    }

    @Test void variantTwoExecuteChanceRollsLevelOnlyOnSuccess() {
        var success = new ScriptedRolls(new boolean[] {false, false, false, false},
                new int[] {0, 0, 0, 0, 2, 0, 4});
        var plan = service.generate(SpacePirateVariant.VARIANT_2, success);
        assertEquals(3, plan.poisonLevel());
        assertEquals(5, plan.executeLevel());
        assertFalse(plan.pummel());
        success.assertExhausted();

        var failure = new ScriptedRolls(new boolean[] {true, true, true, true},
                new int[] {3, 3, 3, 3, 0, 1});
        var noExecute = service.generate(SpacePirateVariant.VARIANT_2, failure);
        assertEquals(1, noExecute.poisonLevel());
        assertEquals(0, noExecute.executeLevel());
        failure.assertExhausted();
    }

    @Test void generatedEquipmentCanNeverDrop() {
        assertEquals(0.0F, SpacePirateEquipmentService.GENERATED_EQUIPMENT_DROP_CHANCE);
    }

    private static final class ScriptedRolls implements SpacePirateEquipmentService.Rolls {
        private final ArrayDeque<Boolean> booleans = new ArrayDeque<>();
        private final ArrayDeque<Integer> integers = new ArrayDeque<>();
        ScriptedRolls(boolean[] booleans, int[] integers) {
            for (boolean value : booleans) this.booleans.add(value);
            for (int value : integers) this.integers.add(value);
        }
        @Override public boolean nextBoolean() { return booleans.remove(); }
        @Override public int nextInt(int bound) {
            int value = integers.remove();
            if (value < 0 || value >= bound) throw new AssertionError(value + " is outside bound " + bound);
            return value;
        }
        void assertExhausted() {
            assertTrue(booleans.isEmpty(), "unused boolean rolls");
            assertTrue(integers.isEmpty(), "unused integer rolls");
        }
    }
}
