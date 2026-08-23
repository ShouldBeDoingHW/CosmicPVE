package com.cosmicpve.content.definition.reward;

import java.util.List;
import net.minecraft.resources.Identifier;

public record RewardTable(Identifier id, List<RewardEntry> entries, int totalWeight) {
    public RewardTable { entries = List.copyOf(entries); }
}
