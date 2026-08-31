package com.cosmicpve.economy.flashsale;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.level.saveddata.SavedData;

public final class FlashSaleSavedData extends SavedData {
    public static final Codec<FlashSaleSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FlashSaleActive.CODEC.optionalFieldOf("active").forGetter(FlashSaleSavedData::active),
            Codec.LONG.optionalFieldOf("next_start_tick", 0L).forGetter(FlashSaleSavedData::nextStartTick)
    ).apply(instance, FlashSaleSavedData::new));

    private Optional<FlashSaleActive> active;
    private long nextStartTick;

    public FlashSaleSavedData() { this(Optional.empty(), 0L); }
    private FlashSaleSavedData(Optional<FlashSaleActive> active, long nextStartTick) {
        this.active = active;
        this.nextStartTick = Math.max(0L, nextStartTick);
    }

    public Optional<FlashSaleActive> active() { return active; }
    public long nextStartTick() { return nextStartTick; }
    public void setActive(Optional<FlashSaleActive> value) {
        active = value; setDirty();
    }
    public void setNextStartTick(long value) {
        if (value < 0) throw new IllegalArgumentException("nextStartTick must not be negative");
        if (nextStartTick != value) { nextStartTick = value; setDirty(); }
    }
}
