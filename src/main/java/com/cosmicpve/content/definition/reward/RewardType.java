package com.cosmicpve.content.definition.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Locale;

public enum RewardType {
    STATIC_ITEM, BANKNOTE, COSMIC_BOOK, UNEXAMINED_BOOK, BLACK_SCROLL, ARMOR_ORB, WEAPON_ORB,
    MOB_SPAWNER, GENERATED_EQUIPMENT, SPACE_CHEST, MASK, ARMOR_SET_CRYSTAL,
    XP_BOTTLE, RANDOM_VKIT_CRYSTAL, ENCHANTED_BLACK_SCROLL, TRIAL_PORTAL_PRESET,
    RANDOM_TRIAL_TRINKET, ACCESSORY_SOCKET, PINPOINT_BOOK, RANDOM_RANGER_ARMOR,
    ADVANCED_BANKNOTE, SKIP_TWO_PORTAL, MADNESS_THREE_PORTAL, MEMORY_CHEST;

    public static final Codec<RewardType> CODEC = Codec.STRING.comapFlatMap(value -> {
        try { return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT))); }
        catch (IllegalArgumentException exception) { return DataResult.error(() -> "Unknown reward type: " + value); }
    }, value -> value.name().toLowerCase(Locale.ROOT));
}
