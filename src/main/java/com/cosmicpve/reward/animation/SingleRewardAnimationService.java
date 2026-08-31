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
        if (finalReward.isEmpty() || pending(player).valid() || runtimes.containsKey(player.getUUID())) return false;
        ItemStack firstPreview = previews.next(player.getRandom());
        if (firstPreview.isEmpty()) return false;
        player.setData(ModAttachments.LOOT_ANIMATION, PendingLootAnimation.of(finalReward));
        try { sourceCommit.run(); }
        catch (RuntimeException failure) { clear(player); throw failure; }
        RuntimeState runtime = new RuntimeState(player.level().getServer().getTickCount(), previews, firstPreview);
        runtimes.put(player.getUUID(), runtime);
        var opened = player.openMenu(new SimpleMenuProvider((id, inventory, ignored) -> new SingleRewardAnimationMenu(id, inventory),
                Component.translatable("container.cosmicpve.single_reward_animation")));
        if (opened.isEmpty()) { runtimes.remove(player.getUUID()); deliverPending(player); return false; }
        LootAnimationFeedback.preview(player, LootAnimationTimeline.previewPitch(0));
        return true;
    }

    public void tick(ServerPlayer player, SingleRewardAnimationMenu menu) {
        RuntimeState state = runtimes.get(player.getUUID());
        if (state == null) { recover(player); return; }
        int elapsed = (int)Math.max(0, player.level().getServer().getTickCount() - state.startedAt);
        if (!state.revealed && elapsed >= LootAnimationTimeline.REVEAL_TICK) {
            state.revealed = true;
            state.shown = pending(player).reward().map(ItemStack::copy).orElse(ItemStack.EMPTY);
            deliverPending(player);
            LootAnimationFeedback.reveal(player);
        } else if (!state.revealed && LootAnimationTimeline.previewDue(elapsed)) {
            int ordinal = LootAnimationTimeline.previewOrdinal(elapsed);
            if (ordinal > state.lastPreviewOrdinal) {
                state.lastPreviewOrdinal = ordinal;
                state.shown = state.previews.next(player.getRandom());
                LootAnimationFeedback.preview(player, LootAnimationTimeline.previewPitch(ordinal));
            }
        }
        menu.refresh(elapsed, state.shown, state.revealed);
        if (elapsed >= LootAnimationTimeline.CLOSE_TICK) player.closeContainer();
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
        if (!pending.valid()) { if (pending.reward().isPresent()) clear(player); return; }
        ItemStack reward = pending.reward().orElseThrow().copy();
        clear(player);
        delivery.deliver(player, List.of(reward));
    }
    private static void clear(ServerPlayer player) { player.setData(ModAttachments.LOOT_ANIMATION, PendingLootAnimation.empty()); }

    private static final class RuntimeState {
        final long startedAt; final LootAnimationPreviewProvider previews;
        ItemStack shown; int lastPreviewOrdinal; boolean revealed;
        RuntimeState(long startedAt, LootAnimationPreviewProvider previews, ItemStack shown) {
            this.startedAt = startedAt; this.previews = previews; this.shown = shown; this.lastPreviewOrdinal = 0;
        }
    }
}
