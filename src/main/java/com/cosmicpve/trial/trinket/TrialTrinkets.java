package com.cosmicpve.trial.trinket;

import com.cosmicpve.data.component.TrialTrinketData;
import com.cosmicpve.data.component.TrialTrinketType;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class TrialTrinkets {
    private TrialTrinkets() {}

    public static ItemStack create(TrialTrinketType type, int value, int count) {
        TrialTrinketData data = new TrialTrinketData(type, value);
        if (!data.valid() || count < 1 || count > 64) throw new IllegalArgumentException("Invalid Trial Trinket request");
        ItemStack stack = new ItemStack(item(type, value), count);
        stack.set(ModDataComponents.TRIAL_TRINKET.get(), data);
        return stack;
    }

    public static Item item(TrialTrinketType type, int value) {
        return switch (type) {
            case TIME -> switch (value) {
                case 1 -> ModItems.TRIAL_TRINKET_TIME_1.get();
                case 3 -> ModItems.TRIAL_TRINKET_TIME_3.get();
                case 5 -> ModItems.TRIAL_TRINKET_TIME_5.get();
                default -> throw new IllegalArgumentException("Time Trinket must be 1, 3, or 5 minutes");
            };
            case SKIP -> switch (value) {
                case 1 -> ModItems.TRIAL_TRINKET_SKIP_1.get();
                case 2 -> ModItems.TRIAL_TRINKET_SKIP_2.get();
                case 3 -> ModItems.TRIAL_TRINKET_SKIP_3.get();
                default -> throw new IllegalArgumentException("Skip Trinket must skip 1, 2, or 3 rooms");
            };
            case INSURANCE -> switch (value) {
                case 1 -> ModItems.TRIAL_TRINKET_INSURANCE_1.get();
                case 2 -> ModItems.TRIAL_TRINKET_INSURANCE_2.get();
                case 3 -> ModItems.TRIAL_TRINKET_INSURANCE_3.get();
                default -> throw new IllegalArgumentException("Insurance Trinket must be level 1, 2, or 3");
            };
            case FAME -> switch (value) {
                case 33 -> ModItems.TRIAL_TRINKET_FAME_33.get();
                case 66 -> ModItems.TRIAL_TRINKET_FAME_66.get();
                case 100 -> ModItems.TRIAL_TRINKET_FAME_100.get();
                default -> throw new IllegalArgumentException("Fame Trinket must be 33, 66, or 100 percent");
            };
        };
    }
}
