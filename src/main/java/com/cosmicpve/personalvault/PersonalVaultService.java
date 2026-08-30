package com.cosmicpve.personalvault;

import com.cosmicpve.data.attachment.PersonalVaultContents;
import com.cosmicpve.data.attachment.PersonalVaultData;
import com.cosmicpve.registry.ModAttachments;
import java.util.ArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Sole mutation authority for persistent Personal Vault data. */
public final class PersonalVaultService {
    public PersonalVaultData data(ServerPlayer player) { return player.getData(ModAttachments.PERSONAL_VAULTS); }
    public long totalUnlockedRows(ServerPlayer player) { return data(player).totalUnlockedRows(); }
    public int rowsUnlocked(ServerPlayer player, long vaultNumber) { return data(player).rowsUnlocked(vaultNumber); }

    public boolean unlockNextRow(ServerPlayer player) {
        var current = data(player);
        final long next;
        try { next = Math.addExact(current.totalUnlockedRows(), 1L); }
        catch (ArithmeticException overflow) { return false; }
        set(player, new PersonalVaultData(current.dataVersion(), next,
                current.combatTagExpiresAt(), current.vaults()));
        return true;
    }

    public boolean setUnlockedRows(ServerPlayer player, long rows) {
        if (rows < 0) return false;
        var current = data(player);
        set(player, new PersonalVaultData(current.dataVersion(), rows,
                current.combatTagExpiresAt(), current.vaults()));
        return true;
    }

    public ItemStack item(ServerPlayer player, long vaultNumber, int slot) {
        return data(player).vault(vaultNumber).map(vault -> vault.item(slot)).orElse(ItemStack.EMPTY);
    }

    public void setItem(ServerPlayer player, long vaultNumber, int slot, ItemStack stack) {
        if (vaultNumber < 1 || slot < 0 || slot >= PersonalVaultContents.CAPACITY)
            throw new IllegalArgumentException("Invalid Personal Vault slot");
        var current = data(player);
        var changed = new ArrayList<>(current.vaults());
        int index = -1;
        for (int i = 0; i < changed.size(); i++) if (changed.get(i).vaultNumber() == vaultNumber) { index = i; break; }
        var vault = index < 0 ? new PersonalVaultContents(vaultNumber, java.util.List.of()) : changed.get(index);
        var updated = vault.withItem(slot, stack);
        if (updated.empty()) {
            if (index >= 0) changed.remove(index);
        } else if (index >= 0) changed.set(index, updated); else changed.add(updated);
        set(player, new PersonalVaultData(current.dataVersion(), current.totalUnlockedRows(),
                current.combatTagExpiresAt(), changed));
    }

    /** Replaces only the exposed prefix, retaining any hidden future-row data verbatim. */
    public void setVisibleItems(ServerPlayer player, long vaultNumber, java.util.List<ItemStack> visible) {
        if (vaultNumber < 1 || visible.size() < 1 || visible.size() > PersonalVaultContents.CAPACITY)
            throw new IllegalArgumentException("Invalid Personal Vault visible contents");
        var current = data(player);
        var changed = new ArrayList<>(current.vaults());
        int index = -1;
        for (int i = 0; i < changed.size(); i++) if (changed.get(i).vaultNumber() == vaultNumber) { index = i; break; }
        var vault = index < 0 ? new PersonalVaultContents(vaultNumber, java.util.List.of()) : changed.get(index);
        for (int slot = 0; slot < visible.size(); slot++) vault = vault.withItem(slot, visible.get(slot));
        if (vault.empty()) {
            if (index >= 0) changed.remove(index);
        } else if (index >= 0) changed.set(index, vault); else changed.add(vault);
        set(player, new PersonalVaultData(current.dataVersion(), current.totalUnlockedRows(),
                current.combatTagExpiresAt(), changed));
    }

    void setCombatTagExpiry(ServerPlayer player, long expiry) {
        var current = data(player);
        set(player, new PersonalVaultData(current.dataVersion(), current.totalUnlockedRows(), expiry, current.vaults()));
    }

    private static void set(ServerPlayer player, PersonalVaultData data) {
        player.setData(ModAttachments.PERSONAL_VAULTS, data);
    }
}
