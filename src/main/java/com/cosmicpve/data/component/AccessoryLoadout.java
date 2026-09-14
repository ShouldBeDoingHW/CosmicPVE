package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;

/** Versioned sockets and attached identities stored directly on the unchanged host item. */
public record AccessoryLoadout(int dataVersion, List<AccessorySlot> sockets,
        Map<AccessorySlot, Identifier> attachments) {
    public static final int DATA_VERSION = 1;
    private static final Codec<AccessoryLoadout> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("data_version", DATA_VERSION).forGetter(AccessoryLoadout::dataVersion),
            AccessorySlot.CODEC.listOf().optionalFieldOf("sockets", List.of()).forGetter(AccessoryLoadout::sockets),
            Codec.unboundedMap(AccessorySlot.CODEC, Identifier.CODEC).optionalFieldOf("attachments", Map.of())
                    .forGetter(AccessoryLoadout::attachments)
    ).apply(instance, AccessoryLoadout::new));
    public static final Codec<AccessoryLoadout> CODEC = BASE_CODEC.validate(value -> value.valid()
            ? DataResult.success(value) : DataResult.error(() -> "Invalid accessory loadout"));

    public AccessoryLoadout {
        sockets = List.copyOf(sockets);
        attachments = Map.copyOf(attachments);
    }

    public static AccessoryLoadout empty() { return new AccessoryLoadout(DATA_VERSION, List.of(), Map.of()); }
    public boolean valid() {
        return dataVersion == DATA_VERSION && new HashSet<>(sockets).size() == sockets.size()
                && sockets.containsAll(attachments.keySet());
    }
    public boolean socketed(AccessorySlot slot) { return sockets.contains(slot); }
    public Optional<Identifier> attached(AccessorySlot slot) { return Optional.ofNullable(attachments.get(slot)); }
    public AccessoryLoadout withSocket(AccessorySlot slot) {
        if (socketed(slot)) return this;
        var next = new java.util.ArrayList<>(sockets); next.add(slot);
        return new AccessoryLoadout(DATA_VERSION, next, attachments);
    }
    public AccessoryLoadout withAttachment(AccessorySlot slot, Identifier id) {
        if (!socketed(slot)) throw new IllegalStateException("Accessory slot is not socketed");
        var next = new EnumMap<AccessorySlot, Identifier>(AccessorySlot.class); next.putAll(attachments); next.put(slot, id);
        return new AccessoryLoadout(DATA_VERSION, sockets, next);
    }
    public AccessoryLoadout withoutAttachment(AccessorySlot slot) {
        var next = new EnumMap<AccessorySlot, Identifier>(AccessorySlot.class); next.putAll(attachments); next.remove(slot);
        return new AccessoryLoadout(DATA_VERSION, sockets, next);
    }
}
