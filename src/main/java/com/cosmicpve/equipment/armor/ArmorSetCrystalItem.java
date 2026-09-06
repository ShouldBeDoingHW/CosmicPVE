package com.cosmicpve.equipment.armor;

import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ArmorSetCrystalItem extends Item {
    public static final int MAX_STACK_SIZE = 1;
    public static final boolean FORCE_GLINT = true;
    public ArmorSetCrystalItem(Properties properties) { super(properties); }

    @Override public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.ARMOR_SET_CRYSTAL.get());
        if (data == null) return super.getName(stack).copy().withStyle(style -> style.withBold(true));
        return Component.translatable("item.cosmicpve.armor_set_crystal.named", data.identity().displayName())
                .withStyle(style -> style.withColor(data.identity().color()).withBold(true));
    }

    @Override public boolean isFoil(ItemStack stack) { return FORCE_GLINT; }

    public static java.util.List<Component> lore(com.cosmicpve.data.component.ArmorSetCrystalData data) {
        var lines = new java.util.ArrayList<Component>();
        int color = data.identity().color();
        lines.add(Component.translatable("tooltip.cosmicpve.crystal.success", data.successRate())
                .withColor(com.cosmicpve.equipment.enchantment.OrbPresentationColors.SUCCESS));
        // Failure is destructive, with White Scroll interception; there is no independent destroy roll.
        lines.add(Component.translatable("tooltip.cosmicpve.crystal.destroy", 100)
                .withColor(com.cosmicpve.equipment.enchantment.OrbPresentationColors.DESTROY));
        lines.add(Component.translatable("tooltip.cosmicpve.crystal.instruction"));
        lines.add(Component.translatable("tooltip.cosmicpve.armor_set.full_bonus").withColor(color));
        data.identity().fullSetBonus().forEach(line -> lines.add(boldBonus(line, color)));
        return java.util.List.copyOf(lines);
    }

    private static net.minecraft.network.chat.MutableComponent boldBonus(Component line, int color) {
        var result = line.copy().withColor(color).withStyle(style -> style.withBold(true));
        result.getSiblings().replaceAll(ArmorSetCrystalItem::boldFragments);
        return result;
    }

    private static net.minecraft.network.chat.MutableComponent boldFragments(Component line) {
        var result = line.copy().withStyle(style -> style.withBold(true));
        result.getSiblings().replaceAll(ArmorSetCrystalItem::boldFragments);
        return result;
    }
}
