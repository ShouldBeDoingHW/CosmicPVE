package com.cosmicpve.equipment;

import com.cosmicpve.equipment.enchantment.CosmicEnchantmentSpecs;
import com.cosmicpve.equipment.enchantment.CosmicBookLore;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.equipment.enchantment.CustomEnchantCapacityService;
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
        if (stack.is(com.cosmicpve.registry.ModItems.SPACE_CHEST.get())
                && stack.has(ModDataComponents.SPACE_CHEST.get())) {
            event.getToolTip().addAll(com.cosmicpve.spacechest.SpaceChestItem.lore());
        }
        if (stack.is(com.cosmicpve.registry.ModItems.HEROIC_CRYSTAL.get())) {
            event.getToolTip().addAll(com.cosmicpve.equipment.heroic.HeroicCrystalItem.lore());
        }
        if (stack.has(ModDataComponents.HEROIC.get()))
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.heroic").withColor(0xAA55FF));
        event.getToolTip().addAll(armorSetPresentationLines(stack));
        if (stack.is(com.cosmicpve.registry.ModItems.SECRET_WEAPON_CACHE.get()))
            event.getToolTip().addAll(com.cosmicpve.reward.lootbox.AnimatedLootboxItem.secretWeaponCacheLore());
        if (stack.is(com.cosmicpve.registry.ModItems.COSMIC_ENCHANTMENT_TABLE.get()))
            event.getToolTip().add(Component.literal("Reach into the cosmos and see what answers.")
                    .withStyle(net.minecraft.ChatFormatting.YELLOW, net.minecraft.ChatFormatting.ITALIC));
        if (stack.is(com.cosmicpve.registry.ModItems.HEROIC_COSMIC_ENCHANTMENT_TABLE.get()))
            event.getToolTip().add(Component.literal("Only the strongest powers answer this call.")
                    .withStyle(net.minecraft.ChatFormatting.YELLOW, net.minecraft.ChatFormatting.ITALIC));
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
            CosmicEnchantmentSpecs.find(book.enchantmentId())
                    .ifPresent(spec -> event.getToolTip().addAll(CosmicBookLore.lines(book, spec)));
        }
        var dust = stack.get(ModDataComponents.COSMIC_DUST.get());
        if (dust != null) {
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.cosmic_dust.effect")
                    .withColor(dust.tier().tooltipColor()));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.cosmic_dust.instruction")
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
        }
        if (stack.is(com.cosmicpve.registry.ModItems.UNEXAMINED_ENCHANTMENT_BOOK.get())
                && stack.has(ModDataComponents.UNEXAMINED_BOOK.get())) {
            event.getToolTip().addAll(
                    com.cosmicpve.equipment.enchantment.UnexaminedEnchantmentBookItem.lore());
        }
        var metadata = stack.get(ModDataComponents.CUSTOM_ENCHANT_META.get());
        if (metadata != null && metadata.whiteScrollProtected()) {
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.protected")
                    .withColor(0xFFFFFF).withStyle(net.minecraft.ChatFormatting.BOLD));
        }
        if (com.cosmicpve.equipment.enchantment.HolyWhiteScrollService.isHoly(stack)) {
            event.getToolTip().add(holyMarker());
        }
        if (stack.is(com.cosmicpve.registry.ModItems.WHITE_SCROLL.get())) {
            event.getToolTip().addAll(com.cosmicpve.equipment.enchantment.WhiteScrollItem.lore());
        }
        if (stack.is(com.cosmicpve.registry.ModItems.HOLY_WHITE_SCROLL.get())) {
            event.getToolTip().addAll(com.cosmicpve.equipment.enchantment.HolyWhiteScrollItem.lore());
        }
        if (stack.is(com.cosmicpve.registry.ModItems.SPACE_DUST_BUNDLE.get())) {
            event.getToolTip().addAll(com.cosmicpve.reward.lootbox.SpaceDustBundleItem.lore());
        }
        if (stack.is(com.cosmicpve.registry.ModItems.TRANSMOG_SCROLL.get())) {
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.transmog.purpose"));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.transmog.instruction"));
        }
        if (stack.is(com.cosmicpve.registry.ModItems.REPAIR_SCROLL.get())) {
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.repair_scroll.purpose")
                    .withStyle(net.minecraft.ChatFormatting.YELLOW));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.repair_scroll.instruction")
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
        }
        var banknote = stack.get(ModDataComponents.BANKNOTE.get());
        if (banknote != null) event.getToolTip().add(Component.translatable("tooltip.cosmicpve.banknote.value",
                com.cosmicpve.economy.MoneyAmount.format(banknote.valueCents())).withStyle(net.minecraft.ChatFormatting.YELLOW));
        var orb = stack.get(ModDataComponents.ENCHANTMENT_ORB.get());
        if (orb != null) {
            event.getToolTip().addAll(com.cosmicpve.equipment.enchantment.EnchantmentOrbItem.lore(stack, orb));
        }
        var blackScroll = stack.get(ModDataComponents.BLACK_SCROLL.get());
        if (blackScroll != null) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.cosmicpve.black_scroll.rate", blackScroll.returnedSuccessRate())
                    .withColor(ItemApplicationColors.SUCCESS));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.black_scroll.purpose"));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.black_scroll.instruction"));
        }
        var enchantedBlackScroll = stack.get(ModDataComponents.ENCHANTED_BLACK_SCROLL.get());
        if (enchantedBlackScroll != null) {
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.enchanted_black_scroll.rate",
                    enchantedBlackScroll.returnedSuccessRate()).withColor(ItemApplicationColors.SUCCESS));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.enchanted_black_scroll.purpose"));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.enchanted_black_scroll.instruction"));
        }
        var storedXp = stack.get(ModDataComponents.STORED_XP_BOTTLE.get());
        if (storedXp != null) event.getToolTip().add(Component.translatable(
                "tooltip.cosmicpve.salvaged_xp_bottle", String.format(java.util.Locale.ROOT, "%,d", storedXp.storedXp()))
                .withStyle(net.minecraft.ChatFormatting.YELLOW));
        var skinItem = stack.get(ModDataComponents.WEAPON_SKIN_ITEM.get());
        if (skinItem != null) {
            com.cosmicpve.equipment.skin.WeaponSkinDefinitions.find(skinItem.skinId()).ifPresent(definition ->
                    event.getToolTip().addAll(com.cosmicpve.equipment.skin.WeaponSkinLore.applicationItem(definition)));
        }
        var activeSkin = stack.get(ModDataComponents.WEAPON_SKIN.get());
        if (activeSkin != null) {
            com.cosmicpve.equipment.skin.WeaponSkinDefinitions.find(activeSkin.skinId()).ifPresent(definition ->
                    event.getToolTip().add(com.cosmicpve.equipment.skin.WeaponSkinLore.active(definition)));
        }
        var signature = stack.get(ModDataComponents.SIGNATURE_WEAPON.get());
        if (signature != null) {
            com.cosmicpve.content.CosmicContent.repository().findArmorSetDefinition(signature.matchingArmorSetId())
                    .ifPresent(set -> {
                        String setName = set.displayName().getString();
                        String text = signature.kind() == com.cosmicpve.data.component.SignatureWeaponIdentity.Kind.MELEE
                                ? "Gives +1 base damage when wielded in tandem with a full " + setName + " armor set!"
                                : "Gives +1 projectile damage when wielded in tandem with a full " + setName + " armor set!";
                        event.getToolTip().add(Component.literal(text).withStyle(style ->
                                style.withColor(set.presentationColor()).withItalic(true)));
                    });
        }
        var adminReward = stack.get(ModDataComponents.ADMIN_ABUSE_REWARD.get());
        if (adminReward != null) com.cosmicpve.reward.lootbox.AdminAbuseRewards.ALL.stream()
                .filter(outcome -> outcome.id().equals(adminReward.rewardId())).findFirst()
                .ifPresent(outcome -> event.getToolTip().add(outcome.flavor()));
        var maskItem = stack.get(ModDataComponents.MASK_ITEM.get());
        if (maskItem != null && maskItem.valid()) {
            if (maskItem.presentations().size() == maskItem.maskIds().size()) event.getToolTip().addAll(
                    com.cosmicpve.equipment.mask.MaskLore.lines(maskItem.presentations(), true));
        }
        var activeMasks = stack.get(ModDataComponents.MASK_LOADOUT.get());
        if (activeMasks != null && activeMasks.valid()) {
            if (activeMasks.presentations().size() == activeMasks.maskIds().size()) event.getToolTip().addAll(
                    com.cosmicpve.equipment.mask.MaskLore.attached(activeMasks.presentations()));
        }
        if (stack.is(com.cosmicpve.registry.ModItems.MASK_SPLICER.get())) {
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.mask_splicer.purpose").withStyle(net.minecraft.ChatFormatting.YELLOW));
            event.getToolTip().add(Component.translatable("tooltip.cosmicpve.mask_splicer.instruction").withStyle(net.minecraft.ChatFormatting.GRAY));
        }
        var capacity = new CustomEnchantCapacityService();
        int used = capacity.used(stack);
        if (used > 0 || CustomEnchantCapacityService.isArmor(stack) || CustomEnchantCapacityService.isWeapon(stack))
            event.getToolTip().add(capacityLine(capacity.capacity(stack)));
        sortTransmogEnchantments(event);
        recolorCosmicEnchantments(event);
    }

    public static Component holyMarker() {
        return Component.literal("HOLY")
                .withStyle(style -> style.withColor(0xC4394A).withUnderlined(true).withBold(true));
    }

    public static java.util.List<Component> armorSetPresentationLines(net.minecraft.world.item.ItemStack stack) {
        var resolved = com.cosmicpve.equipment.armor.ArmorSetPresentationResolver.resolve(stack);
        if (resolved.isEmpty()) return java.util.List.of();
        var identity = resolved.orElseThrow();
        var lines = new java.util.ArrayList<Component>();
        lines.add(Component.empty());
        lines.add(Component.translatable("tooltip.cosmicpve.armor_set.identity", identity.displayName())
                .withColor(identity.color()));
        if (!identity.fullSetBonus().isEmpty()) {
            lines.add(Component.translatable("tooltip.cosmicpve.armor_set.full_bonus").withColor(identity.color()));
            identity.fullSetBonus().forEach(line -> lines.add(line.copy().withColor(identity.color())));
        }
        return java.util.List.copyOf(lines);
    }

    public static Component capacityLine(int effectiveCapacity) {
        int safe = Math.max(CustomEnchantCapacityService.BASE_CAPACITY, effectiveCapacity);
        var line = Component.literal(safe + " Enchantment Slots").withColor(0x55FF55);
        int increase = safe - CustomEnchantCapacityService.BASE_CAPACITY;
        if (increase > 0) line.append(Component.literal(" (Orb ").withStyle(net.minecraft.ChatFormatting.GRAY))
                .append(Component.literal("[+" + increase + "]").withColor(0x55FF55))
                .append(Component.literal(")").withStyle(net.minecraft.ChatFormatting.GRAY));
        return line;
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
