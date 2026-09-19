package com.cosmicpve.entity.spacepirate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import com.cosmicpve.CosmicPVE;
import com.cosmicpve.registry.ModEntities;
import org.junit.jupiter.api.Test;

class SpacePirateFoundationTest {
    @Test void bothStableEntityIdsAreRegisteredWithScaledDimensions() {
        assertEquals(CosmicPVE.id("space_pirate_variant_1"), ModEntities.SPACE_PIRATE_VARIANT_1.getId());
        assertEquals(CosmicPVE.id("space_pirate_variant_2"), ModEntities.SPACE_PIRATE_VARIANT_2.getId());
        var first = ModEntities.SPACE_PIRATE_VARIANT_1.get();
        var second = ModEntities.SPACE_PIRATE_VARIANT_2.get();
        assertEquals(SpacePirateDefinition.VARIANT_1.width(), first.getDimensions().width());
        assertEquals(SpacePirateDefinition.VARIANT_1.height(), first.getDimensions().height());
        assertEquals(SpacePirateDefinition.VARIANT_2.width(), second.getDimensions().width());
        assertEquals(SpacePirateDefinition.VARIANT_2.height(), second.getDimensions().height());
    }

    @Test void variantsUseCanonicalHealthScaleHitboxesAndVanillaPresentations() {
        assertEquals(1.35F, SpacePirateDefinition.SCALE);
        assertEquals(25.0F, SpacePirateDefinition.VARIANT_1.health());
        assertEquals(0.6F * 1.35F, SpacePirateDefinition.VARIANT_1.width());
        assertEquals(1.95F * 1.35F, SpacePirateDefinition.VARIANT_1.height());
        assertEquals(SpacePirateDefinition.Presentation.ZOMBIFIED_PIGLIN,
                SpacePirateDefinition.VARIANT_1.presentation());
        assertEquals(35.0F, SpacePirateDefinition.VARIANT_2.health());
        assertEquals(0.7F * 1.35F, SpacePirateDefinition.VARIANT_2.width());
        assertEquals(2.4F * 1.35F, SpacePirateDefinition.VARIANT_2.height());
        assertEquals(SpacePirateDefinition.Presentation.WITHER_SKELETON,
                SpacePirateDefinition.VARIANT_2.presentation());
    }

    @Test void commonEntityIsAPlainMonsterWithoutPiglinOrWitherInheritance() {
        assertEquals(Monster.class, SpacePirateEntity.class.getSuperclass());
    }

    @Test void attributesKeepWeaponDamageAuthoritativeAndAvoidHiddenArmor() {
        var variantOne = SpacePirateEntity.createAttributes(25.0F).build();
        assertEquals(25.0, variantOne.getValue(Attributes.MAX_HEALTH));
        assertEquals(1.5, variantOne.getValue(Attributes.ATTACK_DAMAGE));
        assertEquals(0.0, variantOne.getValue(Attributes.ARMOR));
        assertEquals(0.0, variantOne.getValue(Attributes.ARMOR_TOUGHNESS));
        var variantTwo = SpacePirateEntity.createAttributes(35.0F).build();
        assertEquals(35.0, variantTwo.getValue(Attributes.MAX_HEALTH));
        assertEquals(0.35, SpacePirateDefinition.MOVEMENT_SPEED, 1.0E-12);
        assertEquals(SpacePirateDefinition.MOVEMENT_SPEED,
                variantOne.getValue(Attributes.MOVEMENT_SPEED), 1.0E-12);
        assertEquals(SpacePirateDefinition.MOVEMENT_SPEED,
                variantTwo.getValue(Attributes.MOVEMENT_SPEED), 1.0E-12);
    }
}
