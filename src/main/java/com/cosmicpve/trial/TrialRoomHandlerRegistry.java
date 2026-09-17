package com.cosmicpve.trial;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/** Small semantic adapter registry. Bespoke encounter services retain their existing mechanics. */
public final class TrialRoomHandlerRegistry {
    private final Map<Identifier,Consumer<ServerPlayer>> loadouts = new LinkedHashMap<>();
    private final Map<Identifier,Starter> starters = new LinkedHashMap<>();
    @FunctionalInterface public interface Starter {
        Start initialize(net.minecraft.server.level.ServerLevel level,TrialSession session,
                com.cosmicpve.instance.structure.InstanceStructurePlacement placement);
    }
    public record Start(TrialEncounterState encounter,net.minecraft.core.BlockPos spawn) {}
    public void registerStarter(Identifier id,Starter starter) {
        if (starters.putIfAbsent(id,starter) != null) throw new IllegalArgumentException("Duplicate Trial behavior handler: " + id);
    }
    public boolean hasStarter(Identifier id) { return starters.containsKey(id); }
    public Start initialize(Identifier id,net.minecraft.server.level.ServerLevel level,TrialSession session,
            com.cosmicpve.instance.structure.InstanceStructurePlacement placement) {
        var starter = starters.get(id);
        if (starter == null) throw new IllegalArgumentException("Unknown Trial behavior handler: " + id);
        return starter.initialize(level,session,placement);
    }
    public void register(Identifier id, Consumer<ServerPlayer> handler) {
        if (loadouts.putIfAbsent(id,handler) != null) throw new IllegalArgumentException("Duplicate Trial handler: " + id);
    }
    public boolean contains(Identifier id) { return loadouts.containsKey(id); }
    public void apply(Identifier id, ServerPlayer player) {
        var handler = loadouts.get(id);
        if (handler == null) throw new IllegalArgumentException("Unknown Trial loadout handler: " + id);
        handler.accept(player);
    }
}
