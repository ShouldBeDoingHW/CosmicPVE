package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.registry.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class EnchantedBlackScrollItem extends Item {
    public EnchantedBlackScrollItem(Properties properties) { super(properties); }
    @Override public Component getName(ItemStack stack) {
        var data = stack.get(ModDataComponents.ENCHANTED_BLACK_SCROLL.get());
        MutableComponent name = Component.empty();
        if (data != null) name.append(Component.literal(data.returnedSuccessRate() + "% "));
        name.append(gradientWord()).append(Component.literal(" Black Scroll"));
        return name.withStyle(style -> style.withBold(true));
    }

    static MutableComponent gradientWord() {
        String word = "Enchanted";
        int[] colors = {0xFFFF00, 0xC8FF00, 0x88FF00, 0x1EFF00, 0x00FFB3,
                0x00E1FF, 0x0055FF, 0x2F00FF, 0xA200FF};
        MutableComponent result = Component.empty();
        for (int index = 0; index < word.length(); index++) {
            result.append(Component.literal(String.valueOf(word.charAt(index))).withColor(colors[index]));
        }
        return result;
    }
    @Override public boolean isFoil(ItemStack stack) { return true; }
}
