package com.cosmicpve.content.definition.reward;

public record RewardEntry(int weight, int minimumQuantity, int maximumQuantity, RewardDescriptor reward) {}
