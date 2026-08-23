package com.cosmicpve.trial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;

public record TrialPotEntry(UUID acquisitionId, List<ItemStack> items) {
    public static final Codec<TrialPotEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("acquisition_id").forGetter(TrialPotEntry::acquisitionId),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("items").forGetter(TrialPotEntry::items)
    ).apply(instance, TrialPotEntry::new));
    public TrialPotEntry { items = items.stream().map(ItemStack::copy).toList(); }
}
