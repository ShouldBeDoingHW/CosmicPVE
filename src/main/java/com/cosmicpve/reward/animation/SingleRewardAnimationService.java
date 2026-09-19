package com.cosmicpve.reward.animation;

import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.reward.RewardDeliveryService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

public final class SingleRewardAnimationService {
    public static final SingleRewardAnimationService INSTANCE = new SingleRewardAnimationService();
    private final Map<UUID, RuntimeState> runtimes = new HashMap<>();
    private final RewardDeliveryService delivery = new RewardDeliveryService();
    private SingleRewardAnimationService() {}

    public boolean open(ServerPlayer player, ItemStack finalReward, LootAnimationPreviewProvider previews, Runnable sourceCommit) {
        return open(player, List.of(finalReward), previews, sourceCommit);
    }

    public boolean open(ServerPlayer player, List<ItemStack> finalRewards, LootAnimationPreviewProvider previews, Runnable sourceCommit) {
        if (finalRewards.isEmpty() || finalRewards.stream().anyMatch(ItemStack::isEmpty)
                || pending(player).valid() || runtimes.containsKey(player.getUUID())) return false;
        var firstPreview = new java.util.ArrayList<ItemStack>(finalRewards.size());
        for (int index = 0; index < finalRewards.size(); index++) firstPreview.add(previews.next(player.getRandom()));
        if (firstPreview.stream().anyMatch(ItemStack::isEmpty)) return false;
        player.setData(ModAttachments.LOOT_ANIMATION, finalRewards.size() == 1
                ? PendingLootAnimation.of(finalRewards.getFirst()) : PendingLootAnimation.of(finalRewards));
        try { sourceCommit.run(); }
        catch (RuntimeException failure) { clear(player); throw failure; }
        // Source consumption and the selected reward obligation share one player-file checkpoint.
        player.level().getServer().getPlayerList().getPlayerIo().save(player);
        RuntimeState runtime = new RuntimeState(player.level().getServer().getTickCount(), previews, firstPreview);
        runtimes.put(player.getUUID(), runtime);
        var opened = player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new SingleRewardAnimationMenu(id, inventory),
                Component.translatable("container.cosmicpve.single_reward_animation")));
        if (opened.isEmpty()) { runtimes.remove(player.getUUID()); deliverPending(player); return false; }
        return true;
    }

    public void tick(ServerPlayer player, SingleRewardAnimationMenu menu) {
        RuntimeState state = runtimes.get(player.getUUID());
        if (state == null) { recover(player); return; }
        int elapsed = (int)Math.max(0, player.level().getServer().getTickCount() - state.startedAt);
        if (!state.revealed && elapsed >= LootAnimationTimeline.REVEAL_TICK) {
            state.revealed = true;
            state.revealedAt = elapsed;
            state.shown = pending(player).allRewards();
            deliverPending(player);
            LootAnimationFeedback.reveal(player);
        } else if (!state.revealed && LootAnimationTimeline.previewDue(elapsed)) {
            int ordinal = LootAnimationTimeline.previewOrdinal(elapsed);
            if (ordinal > state.lastPreviewOrdinal) {
                state.lastPreviewOrdinal = ordinal;
                var previews = new java.util.ArrayList<ItemStack>(state.shown.size());
                for (int index = 0; index < state.shown.size(); index++) previews.add(state.previews.next(player.getRandom()));
                state.shown = java.util.List.copyOf(previews);
            }
        }
        if (!state.revealed && LootAnimationTimeline.previewSoundDue(elapsed)) {
            int soundOrdinal = LootAnimationTimeline.previewSoundOrdinal(elapsed);
            if (soundOrdinal > state.lastSoundOrdinal) {
                state.lastSoundOrdinal = soundOrdinal;
                LootAnimationFeedback.preview(player, LootAnimationTimeline.previewPitch(soundOrdinal));
            }
        }
        menu.refresh(elapsed, state.shown, state.revealed);
        if (state.revealed && elapsed - state.revealedAt >= LootAnimationTimeline.FINAL_HOLD_TICKS) player.closeContainer();
    }

    public void closed(ServerPlayer player) {
        runtimes.remove(player.getUUID());
        if (player.isAlive()) deliverPending(player);
    }
    public void recover(ServerPlayer player) { runtimes.remove(player.getUUID()); deliverPending(player); }
    public boolean active(ServerPlayer player) { return runtimes.containsKey(player.getUUID()); }
    PendingLootAnimation pending(ServerPlayer player) { return player.getData(ModAttachments.LOOT_ANIMATION); }
    private void deliverPending(ServerPlayer player) {
        PendingLootAnimation pending = pending(player);
        if (!pending.valid()) {
            if (pending.reward().isPresent() || !pending.rewards().isEmpty()) clear(player);
            return;
        }
        var rewards = pending.allRewards();
        clear(player);
        delivery.deliver(player, rewards);
        player.level().getServer().getPlayerList().getPlayerIo().save(player);
    }
    private static void clear(ServerPlayer player) { player.setData(ModAttachments.LOOT_ANIMATION, PendingLootAnimation.empty()); }

    private static final class RuntimeState {
        final long startedAt; final LootAnimationPreviewProvider previews;
        java.util.List<ItemStack> shown; int lastPreviewOrdinal; int lastSoundOrdinal; boolean revealed; int revealedAt;
        RuntimeState(long startedAt, LootAnimationPreviewProvider previews, java.util.List<ItemStack> shown) {
            this.startedAt = startedAt; this.previews = previews; this.shown = shown;
            this.lastPreviewOrdinal = 0; this.lastSoundOrdinal = -1;
        }
    }
}
