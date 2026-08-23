package com.cosmicpve.trial.persistence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record TrialOutsideSnapshot(int dataVersion, UUID transactionId, UUID sessionId,
        List<ItemStack> inventory, List<ItemStack> armor, ItemStack offhand, ItemStack carried,
        int selectedSlot, Identifier dimension, double x, double y, double z, float yaw, float pitch) {
    public static final int DATA_VERSION = 1;
    public static final Codec<TrialOutsideSnapshot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(TrialOutsideSnapshot::dataVersion),
            UUIDUtil.CODEC.fieldOf("transaction_id").forGetter(TrialOutsideSnapshot::transactionId),
            UUIDUtil.CODEC.fieldOf("session_id").forGetter(TrialOutsideSnapshot::sessionId),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("inventory").forGetter(TrialOutsideSnapshot::inventory),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("armor").forGetter(TrialOutsideSnapshot::armor),
            ItemStack.OPTIONAL_CODEC.fieldOf("offhand").forGetter(TrialOutsideSnapshot::offhand),
            ItemStack.OPTIONAL_CODEC.fieldOf("carried").forGetter(TrialOutsideSnapshot::carried),
            Codec.INT.fieldOf("selected_slot").forGetter(TrialOutsideSnapshot::selectedSlot),
            Identifier.CODEC.fieldOf("dimension").forGetter(TrialOutsideSnapshot::dimension),
            Codec.DOUBLE.fieldOf("x").forGetter(TrialOutsideSnapshot::x),
            Codec.DOUBLE.fieldOf("y").forGetter(TrialOutsideSnapshot::y),
            Codec.DOUBLE.fieldOf("z").forGetter(TrialOutsideSnapshot::z),
            Codec.FLOAT.fieldOf("yaw").forGetter(TrialOutsideSnapshot::yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(TrialOutsideSnapshot::pitch)
    ).apply(instance, TrialOutsideSnapshot::new));

    public TrialOutsideSnapshot {
        inventory = inventory.stream().map(ItemStack::copy).toList();
        armor = armor.stream().map(ItemStack::copy).toList();
        offhand = offhand.copy();
        carried = carried.copy();
    }
}
