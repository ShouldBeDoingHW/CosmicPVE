package com.cosmicpve.combat.legacy;

import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
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

    private LegacyCombatService() {}

    public static void register(IEventBus modBus) {
        modBus.addListener(LegacyCombatService::modifyDefaultComponents);
        NeoForge.EVENT_BUS.addListener(LegacyCombatService::modifyAttributes);
        NeoForge.EVENT_BUS.addListener(LegacyCombatService::disableSweep);
    }

    private static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        VANILLA_LEGACY_WEAPONS.forEach(item ->
                event.modify(item, components -> components.set(DataComponents.MINIMUM_ATTACK_CHARGE, 0.0F)));
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
}
