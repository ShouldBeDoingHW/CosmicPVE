package com.cosmicpve.data.attachment;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Durable post-respawn delivery obligation for Holy-preserved equipment. */
public record PendingHolyItems(int dataVersion, List<ItemStack> items) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final com.mojang.serialization.MapCodec<PendingHolyItems> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            com.mojang.serialization.Codec.INT.optionalFieldOf("data_version", CURRENT_DATA_VERSION).forGetter(PendingHolyItems::dataVersion),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(PendingHolyItems::items)
    ).apply(instance, PendingHolyItems::new));
    public PendingHolyItems { items = items.stream().map(ItemStack::copy).toList(); }
    public static PendingHolyItems empty() { return new PendingHolyItems(CURRENT_DATA_VERSION, List.of()); }
    public boolean valid() { return dataVersion == CURRENT_DATA_VERSION && items.stream().noneMatch(ItemStack::isEmpty); }
}
