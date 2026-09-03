package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.data.attachment.PendingHolyItems;
import com.cosmicpve.registry.ModAttachments;
import com.cosmicpve.registry.ModDataComponents;
import com.cosmicpve.reward.RewardDeliveryService;
import com.cosmicpve.trial.persistence.TrialSnapshotPhase;
import java.util.ArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** Authoritative non-keep-inventory drop interception for independent Holy rolls. */
public final class HolyDeathService {
    public void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || Boolean.TRUE.equals(player.level().getGameRules().get(GameRules.KEEP_INVENTORY))) return;
        if (player.getData(ModAttachments.TRIAL_PLAYER_STATE).phase() != TrialSnapshotPhase.RESTORED) return;
        boolean monopoly = monopolyActive(player);
        var saved = new ArrayList<net.minecraft.world.item.ItemStack>();
        var iterator = event.getDrops().iterator();
        while (iterator.hasNext()) {
            ItemEntity drop = iterator.next();
            var stack = drop.getItem();
            if (!HolyWhiteScrollService.isHoly(stack)) continue;
            if (player.getRandom().nextDouble() < HolyWhiteScrollService.preservationChance(monopoly)) {
                var preserved = stack.copy();
                HolyWhiteScrollService.consumeHoly(preserved);
                saved.add(preserved);
                iterator.remove();
                drop.discard();
            }
        }
        if (!saved.isEmpty()) {
            var existing = player.getData(ModAttachments.HOLY_ITEMS);
            var combined = new ArrayList<net.minecraft.world.item.ItemStack>(existing.items());
            combined.addAll(saved);
            player.setData(ModAttachments.HOLY_ITEMS, new PendingHolyItems(PendingHolyItems.CURRENT_DATA_VERSION, combined));
        }
    }

    public void recover(ServerPlayer player) {
        var pending = player.getData(ModAttachments.HOLY_ITEMS);
        if (!pending.valid() || pending.items().isEmpty()) return;
        var items = pending.items().stream().map(net.minecraft.world.item.ItemStack::copy).toList();
        player.setData(ModAttachments.HOLY_ITEMS, PendingHolyItems.empty());
        new RewardDeliveryService().deliver(player, items);
    }

    /** Monopoly is prospective design data; this seam intentionally returns false until that mask exists. */
    boolean monopolyActive(ServerPlayer player) { return false; }
}
