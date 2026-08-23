package com.cosmicpve.spacechest;

import com.cosmicpve.content.CosmicContent;
import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.reward.RewardDeliveryService;
import com.cosmicpve.reward.RewardGenerationContext;
import com.cosmicpve.reward.RewardGeneratorService;
import com.cosmicpve.reward.RewardTableService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;

public final class SpaceChestSessionService {
    public static final SpaceChestSessionService INSTANCE = new SpaceChestSessionService();
    public static final int SELECTION_COUNT = 5;
    public static final int SLOT_COUNT = 27;
    public static final int REVEAL_TICKS = 80;
    public static final int FULL_BOARD_PAUSE_TICKS = 30;
    public static final int SELECTED_REWARD_PHASE_TICK = REVEAL_TICKS + FULL_BOARD_PAUSE_TICKS;

    private final Map<UUID, SpaceChestRuntime> runtime = new HashMap<>();
    private final RewardDeliveryService delivery = new RewardDeliveryService();

    private SpaceChestSessionService() {}

    public boolean open(ServerPlayer player, ItemStack chestStack, SpaceChestTier tier) {
        recover(player);
        if (session(player) != null || chestStack.isEmpty()) return false;
        set(player, SpaceChestSession.selecting(tier));
        chestStack.shrink(1);
        runtime.put(player.getUUID(), new SpaceChestRuntime(tier));
        var opened = player.openMenu(new SimpleMenuProvider(
                (id, inventory, ignored) -> new SpaceChestMenu(id, inventory, tier),
                Component.translatable("container.cosmicpve.space_chest", tierName(tier))),
                buffer -> buffer.writeEnum(tier));
        if (opened.isEmpty()) {
            returnChest(player, tier);
            clear(player);
            return false;
        }
        return true;
    }

    public void select(ServerPlayer player, int slot, SpaceChestMenu menu) {
        SpaceChestSession current = session(player);
        if (current == null || current.phase() != SpaceChestPhase.SELECTING || slot < 0 || slot >= SLOT_COUNT
                || current.selectedSlots().contains(slot)) return;
        var nextSelection = SpaceChestSessionTransitions.select(current, slot);
        if (nextSelection.isEmpty()) return;
        var selected = nextSelection.orElseThrow().selectedSlots();
        player.level().playSound(null, player.blockPosition(), SpaceChestSounds.initialSelection(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        if (selected.size() < SELECTION_COUNT) {
            set(player, nextSelection.orElseThrow());
            menu.refresh();
            return;
        }

        var generated = generateAll(player, current.tier());
        set(player, SpaceChestSessionTransitions.commit(nextSelection.orElseThrow(), generated));
        SpaceChestRuntime live = runtime.computeIfAbsent(player.getUUID(), ignored -> new SpaceChestRuntime(current.tier()));
        live.rewards.clear();
        live.rewards.addAll(generated);
        live.revealTicks = 0;
        live.revealStartedAtTick = player.level().getServer().getTickCount();
        live.revealFinished = false;
        menu.refresh();
    }

    public void tick(ServerPlayer player, SpaceChestMenu menu) {
        SpaceChestSession current = session(player);
        SpaceChestRuntime live = runtime.get(player.getUUID());
        if (current == null || current.phase() != SpaceChestPhase.COMMITTED || live == null || live.revealFinished) return;
        long elapsed = player.level().getServer().getTickCount() - live.revealStartedAtTick;
        int nextRevealTicks = (int) Math.min(SELECTED_REWARD_PHASE_TICK, Math.max(0L, elapsed));
        if (nextRevealTicks == live.revealTicks) return;
        live.revealTicks = nextRevealTicks;
        if (live.revealTicks >= SELECTED_REWARD_PHASE_TICK) {
            live.revealFinished = true;
            player.level().playSound(null, player.blockPosition(), SpaceChestSounds.missedRewardsCleared(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
        }
        menu.refresh();
    }

    public void claim(ServerPlayer player, int slot, SpaceChestMenu menu) {
        SpaceChestSession current = session(player);
        SpaceChestRuntime live = runtime.get(player.getUUID());
        if (current == null || current.phase() != SpaceChestPhase.COMMITTED || live == null || !live.revealFinished) return;
        int index = -1;
        for (int i = 0; i < current.committedRewards().size(); i++) {
            if (current.committedRewards().get(i).slot() == slot) { index = i; break; }
        }
        if (index < 0 || current.committedRewards().get(index).delivered()) return;
        SpaceChestSession delivered = deliverAndMark(player, current, index);
        if (delivered != current) {
            SpaceChestSounds.playForPlayer(player, SpaceChestSounds.selectedRewardRevealed());
        }
        menu.refresh();
    }

    public void closed(ServerPlayer player) {
        SpaceChestSession current = session(player);
        if (current == null) return;
        if (current.phase() == SpaceChestPhase.SELECTING) returnChest(player, current.tier());
        else {
            SpaceChestSounds.playForPlayer(player, SpaceChestSounds.committedMenuClosed());
            deliverPending(player, current);
        }
        clear(player);
    }

    public void recover(ServerPlayer player) {
        SpaceChestSession current = session(player);
        if (current == null) return;
        if (current.phase() == SpaceChestPhase.SELECTING) returnChest(player, current.tier());
        else deliverPending(player, current);
        clear(player);
    }

    public String inspect(ServerPlayer player) {
        SpaceChestSession current = session(player);
        if (current == null) return "No active Space Chest session.";
        long deliveredCount = current.committedRewards().stream().filter(CommittedSpaceChestReward::delivered).count();
        return current.tier() + " " + current.phase() + ", selected=" + current.selectedSlots()
                + ", delivered=" + deliveredCount + "/" + current.committedRewards().size();
    }

    public void reset(ServerPlayer player) { recover(player); }

    SpaceChestSession session(ServerPlayer player) {
        return player.getData(ModAttachments.SPACE_CHEST_SESSION).session().orElse(null);
    }
    SpaceChestRuntime runtime(ServerPlayer player) { return runtime.get(player.getUUID()); }

    private List<List<ItemStack>> generateAll(ServerPlayer player, SpaceChestTier tier) {
        var tables = new RewardTableService(CosmicContent.repository(), new RewardGeneratorService());
        var context = new RewardGenerationContext(player.registryAccess(), player.getRandom(), player);
        var all = new ArrayList<List<ItemStack>>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) all.add(tables.roll(tier.rewardTableId(), 1, context));
        return List.copyOf(all);
    }

    private void deliverPending(ServerPlayer player, SpaceChestSession session) {
        SpaceChestSession working = session;
        for (int index = 0; index < working.committedRewards().size(); index++) {
            if (!working.committedRewards().get(index).delivered()) working = deliverAndMark(player, working, index);
        }
    }

    private SpaceChestSession deliverAndMark(ServerPlayer player, SpaceChestSession current, int index) {
        var reward = current.committedRewards().get(index);
        if (reward.delivered()) return current;
        delivery.deliver(player, reward.items());
        var next = SpaceChestSessionTransitions.markDelivered(current, index);
        set(player, next);
        return next;
    }

    private void returnChest(ServerPlayer player, SpaceChestTier tier) {
        delivery.deliver(player, List.of(SpaceChests.create(tier)));
    }
    private void set(ServerPlayer player, SpaceChestSession session) {
        player.setData(ModAttachments.SPACE_CHEST_SESSION, SpaceChestSessionAttachment.of(session));
    }
    private void clear(ServerPlayer player) {
        runtime.remove(player.getUUID());
        player.removeData(ModAttachments.SPACE_CHEST_SESSION);
    }
    private static Component tierName(SpaceChestTier tier) {
        return Component.translatable("cosmic_tier.cosmicpve." + tier.serializedName()).withColor(tier.color());
    }
}
