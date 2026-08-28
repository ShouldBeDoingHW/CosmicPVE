package com.cosmicpve.conquest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.saveddata.SavedData;

public final class ConquestSavedData extends SavedData {
    public static final Codec<ConquestSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.optionalFieldOf("last_scheduled_day", 0L).forGetter(ConquestSavedData::lastScheduledDay),
            ConquestEvent.CODEC.listOf().optionalFieldOf("events", List.of()).forGetter(ConquestSavedData::events)
    ).apply(instance, ConquestSavedData::new));

    private long lastScheduledDay;
    private final List<ConquestEvent> events;

    public ConquestSavedData() { this(0L, List.of()); }
    private ConquestSavedData(long lastScheduledDay, List<ConquestEvent> events) {
        this.lastScheduledDay = lastScheduledDay;
        this.events = new ArrayList<>(events);
    }

    public long lastScheduledDay() { return lastScheduledDay; }
    public List<ConquestEvent> events() { return List.copyOf(events); }
    public void setLastScheduledDay(long day) {
        if (day > lastScheduledDay) { lastScheduledDay = day; setDirty(); }
    }
    public void upsert(ConquestEvent event) {
        events.removeIf(existing -> existing.id().equals(event.id()));
        events.add(event);
        setDirty();
    }
    public void remove(java.util.UUID id) {
        if (events.removeIf(event -> event.id().equals(id))) setDirty();
    }
}
