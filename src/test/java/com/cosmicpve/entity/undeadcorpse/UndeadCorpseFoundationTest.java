package com.cosmicpve.entity.undeadcorpse;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayDeque;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.junit.jupiter.api.Test;

class UndeadCorpseFoundationTest {
    @Test void canonicalAttributesAndScaleAreStable() {
        var attributes=UndeadCorpseEntity.createAttributes().build();
        assertEquals(15.0D,attributes.getBaseValue(Attributes.MAX_HEALTH));
        assertEquals(0.4D,attributes.getBaseValue(Attributes.MOVEMENT_SPEED));
        assertEquals(0.0D,attributes.getBaseValue(Attributes.ATTACK_DAMAGE));
        assertEquals(1.0D,attributes.getBaseValue(Attributes.ARMOR));
        assertEquals(0.0D,attributes.getBaseValue(Attributes.ARMOR_TOUGHNESS));
        assertEquals(0.9F,UndeadCorpseEntity.RENDER_SCALE);
    }

    @Test void axeEnchantmentsUseThreeIndependentThirtyPercentRolls() {
        var values=new ArrayDeque<Float>(java.util.List.of(0.29F,0.30F,0.01F));
        var result=UndeadCorpseEquipmentService.rollEnchantments(values::removeFirst);
        assertTrue(result.sharpness()); assertFalse(result.bleed()); assertTrue(result.rage());
        assertEquals(3,UndeadCorpseEquipmentService.BASE_ENCHANT_LEVEL);
        assertEquals(0.30F,UndeadCorpseEquipmentService.ENCHANT_CHANCE);
    }
}
