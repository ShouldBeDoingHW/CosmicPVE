package com.cosmicpve.personalvault;

import com.cosmicpve.registry.ModMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class PersonalVaultMenu extends AbstractContainerMenu {
    private final long vaultNumber;
    private final int rows;
    private final Player owner;
    private final Container storage;

    public PersonalVaultMenu(int id, Inventory inventory, long vaultNumber, int rows, Container storage) {
        super(ModMenus.PERSONAL_VAULT.get(), id);
        if (vaultNumber < 1 || rows < 1 || rows > 3 || storage.getContainerSize() != rows * 9)
            throw new IllegalArgumentException("Invalid Personal Vault menu dimensions");
        this.owner = inventory.player; this.vaultNumber = vaultNumber; this.rows = rows; this.storage = storage;
        for (int row = 0; row < rows; row++) for (int column = 0; column < 9; column++) {
            int slot = row * 9 + column;
            addSlot(new Slot(storage, slot, 8 + column * 18, 18 + row * 18));
        }
        addStandardInventorySlots(inventory, 8, 32 + rows * 18);
    }

    public static PersonalVaultMenu server(int id, Inventory inventory, long vaultNumber, int rows) {
        if (!(inventory.player instanceof ServerPlayer player)) throw new IllegalStateException("Server player required");
        return new PersonalVaultMenu(id, inventory, vaultNumber, rows,
                new PersonalVaultContainer(player, vaultNumber, rows));
    }
    public static PersonalVaultMenu client(int id, Inventory inventory, RegistryFriendlyByteBuf data) {
        long vault = data.readVarLong(); int rows = data.readUnsignedByte();
        return new PersonalVaultMenu(id, inventory, vault, rows, new SimpleContainer(rows * 9));
    }
    public long vaultNumber() { return vaultNumber; }
    public int rows() { return rows; }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack source = slot.getItem(); ItemStack original = source.copy(); int vaultSlots = rows * 9;
        if (index < vaultSlots) {
            if (!moveItemStackTo(source, vaultSlots, slots.size(), true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(source, 0, vaultSlots, false)) return ItemStack.EMPTY;
        if (source.isEmpty()) slot.setByPlayer(ItemStack.EMPTY); else slot.setChanged();
        return original;
    }
    @Override public boolean stillValid(Player player) {
        return player == owner && (!(owner instanceof ServerPlayer serverPlayer)
                || PersonalVaultRuntime.access().evaluate(serverPlayer).allowed());
    }
    @Override public void removed(Player player) {
        storage.setChanged();
        super.removed(player);
    }
}
