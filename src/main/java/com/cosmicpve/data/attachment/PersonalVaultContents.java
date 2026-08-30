package com.cosmicpve.data.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Immutable persisted contents for one sparse Personal Vault. */
public record PersonalVaultContents(long vaultNumber, List<ItemStack> slots) {
    public static final int CAPACITY = 27;
    public static final Codec<PersonalVaultContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("vault_number").forGetter(PersonalVaultContents::vaultNumber),
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("slots").forGetter(PersonalVaultContents::slots)
    ).apply(instance, PersonalVaultContents::new));

    public PersonalVaultContents {
        if (vaultNumber < 1) throw new IllegalArgumentException("vaultNumber must be positive");
        if (slots.size() > CAPACITY) throw new IllegalArgumentException("Personal Vault exceeds 27 slots");
        var normalized = new ArrayList<ItemStack>(CAPACITY);
        slots.forEach(stack -> normalized.add(stack.copy()));
        while (normalized.size() < CAPACITY) normalized.add(ItemStack.EMPTY);
        slots = List.copyOf(normalized);
    }

    @Override public List<ItemStack> slots() { return slots.stream().map(ItemStack::copy).toList(); }

    public ItemStack item(int slot) {
        if (slot < 0 || slot >= CAPACITY) return ItemStack.EMPTY;
        return slots.get(slot).copy();
    }

    public PersonalVaultContents withItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= CAPACITY) throw new IndexOutOfBoundsException(slot);
        var changed = new ArrayList<>(slots);
        changed.set(slot, stack.copy());
        return new PersonalVaultContents(vaultNumber, changed);
    }

    public boolean empty() { return slots.stream().allMatch(ItemStack::isEmpty); }
}
