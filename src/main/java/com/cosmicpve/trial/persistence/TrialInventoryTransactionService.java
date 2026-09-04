package com.cosmicpve.trial.persistence;

import com.cosmicpve.economy.RawExperienceService;
import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.reward.RewardDeliveryService;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Targeted player-file persistence provides the durable boundary; it never forces a global world save. */
public final class TrialInventoryTransactionService {
    private static final List<EquipmentSlot> ARMOR = List.of(
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    private final SafeReturnPositionService safeReturns = new SafeReturnPositionService();
    private final RawExperienceService experience = new RawExperienceService();

    public boolean enter(ServerPlayer player, UUID sessionId) {
        TrialPlayerState existing = player.getExistingDataOrNull(ModAttachments.TRIAL_PLAYER_STATE);
        if (existing != null && existing.phase() != TrialSnapshotPhase.RESTORED) return false;
        player.closeContainer();
        var inventory = player.getInventory().getNonEquipmentItems().stream().map(ItemStack::copy).toList();
        var armor = ARMOR.stream().map(player::getItemBySlot).map(ItemStack::copy).toList();
        var snapshot = new TrialOutsideSnapshot(TrialOutsideSnapshot.DATA_VERSION, UUID.randomUUID(), sessionId,
                inventory, armor, player.getItemBySlot(EquipmentSlot.OFFHAND).copy(),
                player.containerMenu.getCarried().copy(), player.getInventory().getSelectedSlot(),
                experience.balance(player), player.level().dimension().identifier(),
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot());
        try {
            player.setData(ModAttachments.TRIAL_PLAYER_STATE, TrialPlayerState.committed(snapshot));
            persistPlayer(player); // snapshot and untouched outside inventory are durable together
        } catch (RuntimeException exception) {
            player.removeData(ModAttachments.TRIAL_PLAYER_STATE);
            return false;
        }
        clearTrialInventory(player);
        player.setData(ModAttachments.TRIAL_PLAYER_STATE, TrialPlayerState.committed(snapshot).inTrial());
        persistPlayer(player); // cleared Trial state and retained snapshot are durable together
        return true;
    }

    public boolean restore(ServerPlayer player) {
        return restoreInternal(player, false);
    }

    public boolean prepareCashout(ServerPlayer player, List<ItemStack> rewards) {
        return prepareRewardedRestore(player, rewards, 0L);
    }

    public boolean prepareCashout(ServerPlayer player, List<ItemStack> rewards, long fame) {
        return prepareRewardedRestore(player, rewards, fame);
    }

    public boolean prepareRewardedRestore(ServerPlayer player, List<ItemStack> rewards) {
        return prepareRewardedRestore(player, rewards, 0L);
    }

    private boolean prepareRewardedRestore(ServerPlayer player, List<ItemStack> rewards, long fame) {
        TrialPlayerState state = player.getExistingDataOrNull(ModAttachments.TRIAL_PLAYER_STATE);
        if (state == null || state.phase() == TrialSnapshotPhase.RESTORED || state.snapshot().isEmpty()
                || state.rewardRestorePrepared() || fame < 0) return false;
        player.setData(ModAttachments.TRIAL_PLAYER_STATE, state.withPreparedRewards(rewards, fame));
        persistPlayer(player);
        return true;
    }

    public boolean restoreCashout(ServerPlayer player, RewardDeliveryService delivery) {
        return restoreInternal(player, true, delivery);
    }

    public boolean recover(ServerPlayer player, RewardDeliveryService delivery) {
        TrialPlayerState state = player.getExistingDataOrNull(ModAttachments.TRIAL_PLAYER_STATE);
        if (state == null || state.phase() == TrialSnapshotPhase.RESTORED) return false;
        return state.rewardRestorePrepared() ? restoreCashout(player, delivery) : restore(player);
    }

    private boolean restoreInternal(ServerPlayer player, boolean cashout) {
        return restoreInternal(player, cashout, null);
    }

    private boolean restoreInternal(ServerPlayer player, boolean cashout, RewardDeliveryService delivery) {
        TrialPlayerState state = player.getExistingDataOrNull(ModAttachments.TRIAL_PLAYER_STATE);
        if (state == null || state.phase() == TrialSnapshotPhase.RESTORED || state.snapshot().isEmpty()) return false;
        if (cashout && (delivery == null || !state.rewardRestorePrepared())) return false;
        TrialOutsideSnapshot snapshot = state.snapshot().orElseThrow();
        clearTrialInventory(player);
        var destination = player.getInventory().getNonEquipmentItems();
        for (int i = 0; i < destination.size(); i++) {
            destination.set(i, i < snapshot.inventory().size() ? snapshot.inventory().get(i).copy() : ItemStack.EMPTY);
        }
        for (int i = 0; i < ARMOR.size(); i++) {
            player.setItemSlot(ARMOR.get(i), i < snapshot.armor().size() ? snapshot.armor().get(i).copy() : ItemStack.EMPTY);
        }
        player.setItemSlot(EquipmentSlot.OFFHAND, snapshot.offhand().copy());
        player.getInventory().setSelectedSlot(Math.max(0, Math.min(8, snapshot.selectedSlot())));
        player.containerMenu.setCarried(snapshot.carried().copy());
        experience.restore(player, snapshot.experiencePoints());
        player.getInventory().setChanged();

        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, snapshot.dimension());
        var server = player.level().getServer();
        ServerLevel returnLevel = server.getLevel(dimension);
        if (returnLevel == null) returnLevel = server.overworld();
        var safe = safeReturns.resolve(returnLevel, snapshot.x(), snapshot.y(), snapshot.z());
        boolean teleported = safe.isPresent() && player.teleportTo(returnLevel,
                safe.orElseThrow().x(), safe.orElseThrow().y(), safe.orElseThrow().z(),
                Set.<Relative>of(), snapshot.yaw(), snapshot.pitch(), false);
        if (!teleported) {
            var fallback = returnLevel.getRespawnData().pos();
            teleported = player.teleportTo(returnLevel, fallback.getX() + 0.5, fallback.getY() + 1.0,
                    fallback.getZ() + 0.5, Set.<Relative>of(), snapshot.yaw(), snapshot.pitch(), false);
        }
        if (!teleported) return false; // snapshot remains retryable; the inventory rewrite is idempotent
        if (cashout) {
            delivery.deliver(player, state.pendingRewards());
            if (state.pendingFame() > 0 && !new com.cosmicpve.economy.FameService().add(player, state.pendingFame())) return false;
        }
        player.setData(ModAttachments.TRIAL_PLAYER_STATE, TrialPlayerState.restored());
        persistPlayer(player); // restored inventory and consumed snapshot share one player-file save
        return true;
    }

    public Optional<TrialOutsideSnapshot> pending(ServerPlayer player) {
        TrialPlayerState state = player.getExistingDataOrNull(ModAttachments.TRIAL_PLAYER_STATE);
        return state == null || state.phase() == TrialSnapshotPhase.RESTORED ? Optional.empty() : state.snapshot();
    }

    public void clearTrialInventory(ServerPlayer player) {
        player.getInventory().clearContent();
        ARMOR.forEach(slot -> player.setItemSlot(slot, ItemStack.EMPTY));
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.getInventory().setChanged();
    }

    private static void persistPlayer(ServerPlayer player) {
        player.level().getServer().getPlayerList().getPlayerIo().save(player);
    }
}
