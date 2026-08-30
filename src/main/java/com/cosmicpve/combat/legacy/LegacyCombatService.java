package com.cosmicpve.combat.legacy;

import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.player.SweepAttackEvent;

/** Global legacy-combat rules for the vanilla swords and axes supported by this prototype. */
public final class LegacyCombatService {
    public static final double TARGET_TOTAL_ATTACK_SPEED = 20.0;
    private static final double PLAYER_BASE_ATTACK_SPEED = 4.0;
    private static final Set<Item> VANILLA_LEGACY_WEAPONS = Set.of(
            Items.WOODEN_SWORD, Items.COPPER_SWORD, Items.STONE_SWORD, Items.GOLDEN_SWORD,
            Items.IRON_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD,
            Items.WOODEN_AXE, Items.COPPER_AXE, Items.STONE_AXE, Items.GOLDEN_AXE,
            Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE);
    private static final List<WeaponPair> NORMALIZED_MELEE_PAIRS = List.of(
            new WeaponPair(Items.WOODEN_SWORD, Items.WOODEN_AXE),
            new WeaponPair(Items.COPPER_SWORD, Items.COPPER_AXE),
            new WeaponPair(Items.STONE_SWORD, Items.STONE_AXE),
            new WeaponPair(Items.GOLDEN_SWORD, Items.GOLDEN_AXE),
            new WeaponPair(Items.IRON_SWORD, Items.IRON_AXE),
            new WeaponPair(Items.DIAMOND_SWORD, Items.DIAMOND_AXE),
            new WeaponPair(Items.NETHERITE_SWORD, Items.NETHERITE_AXE));

    private LegacyCombatService() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(LegacyCombatService::modifyDefaultComponents);
        NeoForge.EVENT_BUS.addListener(LegacyCombatService::modifyAttributes);
        NeoForge.EVENT_BUS.addListener(LegacyCombatService::disableSweep);
    }

    private static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        VANILLA_LEGACY_WEAPONS.forEach(item ->
                event.modify(item, components -> components.set(DataComponents.MINIMUM_ATTACK_CHARGE, 0.0F)));
        NORMALIZED_MELEE_PAIRS.forEach(pair -> event.modify(pair.sword(), components -> components.set(
                DataComponents.ATTRIBUTE_MODIFIERS,
                normalizedSwordAttributes(pair.sword(), pair.axe()))));
    }

    /** Gives a vanilla sword the matching vanilla axe's actual base attack damage. */
    public static ItemAttributeModifiers normalizedSwordAttributes(Item sword, Item axe) {
        ItemAttributeModifiers swordAttributes = sword.components().getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers axeAttributes = axe.components().getOrDefault(
                DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        var axeDamage = axeAttributes.modifiers().stream()
                .filter(entry -> entry.attribute() == Attributes.ATTACK_DAMAGE)
                .filter(entry -> entry.modifier().is(Item.BASE_ATTACK_DAMAGE_ID))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Axe has no base attack-damage modifier: " + axe));
        return swordAttributes.withModifierAdded(
                Attributes.ATTACK_DAMAGE, axeDamage.modifier(), axeDamage.slot());
    }

    private static void modifyAttributes(ItemAttributeModifierEvent event) {
        if (!VANILLA_LEGACY_WEAPONS.contains(event.getItemStack().getItem())) {
            return;
        }
        event.replaceModifier(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(
                        Item.BASE_ATTACK_SPEED_ID,
                        TARGET_TOTAL_ATTACK_SPEED - PLAYER_BASE_ATTACK_SPEED,
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
    }

    private static void disableSweep(SweepAttackEvent event) {
        event.setSweeping(false);
    }

    private record WeaponPair(Item sword, Item axe) {}
}
