package com.cosmicpve.equipment;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService;
import com.cosmicpve.equipment.enchantment.CosmicEnchantmentTier;
import com.cosmicpve.equipment.enchantment.TransmogApplicationService;
import com.cosmicpve.equipment.enchantment.TransmogTooltipOrdering;
import com.cosmicpve.equipment.enchantment.ItemApplicationColors;
import java.util.ArrayList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class EquipmentTooltipService {
    public void onTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        var identity = stack.get(ModDataComponents.ARMOR_SET_ID.get());
        if (identity != null) {
            event.getToolTip().add(Component.empty());
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.armor_set.identity", identity.displayName())
                    .withColor(identity.color()));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.armor_set.full_bonus")
                    .withColor(identity.color()));
            identity.fullSetBonus().forEach(line -> event.getToolTip().add(line.copy().withColor(identity.color())));
        }
        var crystal = stack.get(ModDataComponents.ARMOR_SET_CRYSTAL.get());
        if (crystal != null) {
            int color = crystal.identity().color();
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.crystal.success", crystal.successRate()));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.crystal.destroy", 100));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.crystal.instruction"));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.armor_set.full_bonus").withColor(color));
            crystal.identity().fullSetBonus().forEach(line -> event.getToolTip().add(line.copy().withColor(color)));
        }
        var book = stack.get(ModDataComponents.COSMIC_ENCHANT_BOOK.get());
        if (book != null) {
            var spec = CosmicEnchantmentSpecs.find(book.enchantmentId());
            spec.ifPresent(value -> {
                int color = value.tier().tooltipColor();
                event.getToolTip().add(Component.translatable("tooltip.cosmicpve.book.rarity",
                        Component.translatable("cosmic_tier.cosmicpve." + value.tier().name().toLowerCase())).withColor(color));
                event.getToolTip().add(Component.translatable("tooltip.cosmicpve.book.success", book.successRate()).withColor(ItemApplicationColors.SUCCESS));
                event.getToolTip().add(Component.translatable("tooltip.cosmicpve.book.destroy", book.destroyRate()).withColor(ItemApplicationColors.DESTROY));
                event.getToolTip().add(Component.translatable("tooltip.cosmicpve.book.instruction"));
            });
        }
        var metadata = stack.get(ModDataComponents.CUSTOM_ENCHANT_META.get());
        if (metadata != null && metadata.whiteScrollProtected()) {
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.protected")
                    .withColor(0xFFFFFF).withStyle(net.minecraft.ChatFormatting.BOLD));
        }
        if (stack.is(com.cosmicpve.registry.ModItems.WHITE_SCROLL.get())) {
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.white_scroll.purpose"));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.white_scroll.instruction"));
        }
        if (stack.is(com.cosmicpve.registry.ModItems.TRANSMOG_SCROLL.get())) {
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.transmog.purpose"));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.transmog.instruction"));
        }
        var orb = stack.get(ModDataComponents.ENCHANTMENT_ORB.get());
        if (orb != null) {
            boolean armor = stack.is(com.cosmicpve.registry.ModItems.ARMOR_ENCHANTMENT_ORB.get());
            event.getToolTip().add(Component.translatable(armor
                    ? "tooltip.cosmicpve.orb.armor_purpose" : "tooltip.cosmicpve.orb.weapon_purpose"));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.orb.success", orb.successRate()).withColor(ItemApplicationColors.SUCCESS));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.orb.destroy", orb.destroyRate()).withColor(ItemApplicationColors.DESTROY));
            event.getToolTip().add(Component.translatable(armor
                    ? "tooltip.cosmicpve.orb.armor_instruction" : "tooltip.cosmicpve.orb.weapon_instruction"));
        }
        var blackScroll = stack.get(ModDataComponents.BLACK_SCROLL.get());
        if (blackScroll != null) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.cosmicpve.black_scroll.rate", blackScroll.returnedSuccessRate())
                    .withColor(ItemApplicationColors.SUCCESS));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.black_scroll.purpose"));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.black_scroll.instruction"));
        }
        var skinItem = stack.get(ModDataComponents.WEAPON_SKIN_ITEM.get());
        if (skinItem != null) {
            com.cosmicpve.equipment.skin.WeaponSkinDefinitions.find(skinItem.skinId()).ifPresent(definition -> {
                event.getToolTip().add(Component.translatable("tooltip.cosmicpve.skin.applies_to",
                        Component.translatable("weapon_kind.cosmicpve." + definition.weaponKind().name().toLowerCase())));
                event.getToolTip().add(Component.translatable("tooltip.cosmicpve.skin.instruction"));
            });
        }
        var activeSkin = stack.get(ModDataComponents.WEAPON_SKIN.get());
        if (activeSkin != null) {
            com.cosmicpve.equipment.skin.WeaponSkinDefinitions.find(activeSkin.skinId()).ifPresent(definition ->
                    event.getToolTip().add(Component.translatable(
                            "tooltip.cosmicpve.skin.active", definition.displayName()).withColor(0x55FFFF)));
        }
        var capacity = new CustomEnchantCapacityService();
        int used = capacity.used(stack);
        if (used > 0 || CustomEnchantCapacityService.isArmor(stack) || CustomEnchantCapacityService.isWeapon(stack))
            event.getToolTip().add(Component.translatable(
                "tooltip.cosmicpve.enchant_capacity", used, capacity.capacity(stack)));
        sortTransmogEnchantments(event);
        recolorCosmicEnchantments(event);
    }

    static void sortTransmogEnchantments(ItemTooltipEvent event) {
        if (!new TransmogApplicationService().isApplied(event.getItemStack())) return;
        var lines = event.getToolTip();
        var rendered = new ArrayList<RenderedEnchant>();
        var claimed = new java.util.HashSet<Integer>();
        for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(event.getItemStack()).entrySet()) {
            var id = entry.getKey().unwrapKey().map(key -> key.identifier()).orElse(null);
            var spec = id == null ? java.util.Optional.<com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpec>empty()
                    : CosmicEnchantmentSpecs.find(id);
            String name = Enchantment.getFullname(entry.getKey(), entry.getIntValue()).getString();
            for (int i = 0; i < lines.size(); i++) {
                if (!claimed.contains(i) && lines.get(i).getString().equals(name)) {
                    claimed.add(i);
                    rendered.add(new RenderedEnchant(i, lines.get(i), new TransmogTooltipOrdering.Key(
                            spec.isPresent(), spec.map(value -> value.tier()).orElse(null),
                            entry.getIntValue(), id, i)));
                    break;
                }
            }
        }
        if (rendered.size() < 2) return;
        int insertion = rendered.stream().mapToInt(RenderedEnchant::lineIndex).min().orElseThrow();
        rendered.stream().map(RenderedEnchant::lineIndex).sorted(java.util.Comparator.reverseOrder())
                .forEach(index -> lines.remove((int) index));
        rendered.sort(java.util.Comparator.comparing(RenderedEnchant::key, TransmogTooltipOrdering.COMPARATOR));
        for (var value : rendered) lines.add(insertion++, value.component());
    }

    private record RenderedEnchant(int lineIndex, Component component, TransmogTooltipOrdering.Key key) {}

    static void recolorCosmicEnchantments(ItemTooltipEvent event) {
        var lines = event.getToolTip();
        for (var entry : EnchantmentHelper.getEnchantmentsForCrafting(event.getItemStack()).entrySet()) {
            var id = entry.getKey().unwrapKey().map(key -> key.identifier()).orElse(null);
            var spec = id == null ? java.util.Optional.<com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpec>empty()
                    : CosmicEnchantmentSpecs.find(id);
            if (spec.isEmpty()) continue;
            Component vanilla = Enchantment.getFullname(entry.getKey(), entry.getIntValue());
            for (int i = 0; i < lines.size(); i++) {
                if (lines.get(i).getString().equals(vanilla.getString())) {
                    lines.set(i, vanilla.copy().withColor(spec.orElseThrow().tier().tooltipColor()));
                    break;
                }
            }
        }
    }
}
