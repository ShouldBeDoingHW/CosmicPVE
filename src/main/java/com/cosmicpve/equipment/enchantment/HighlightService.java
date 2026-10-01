package com.cosmicpve.equipment.enchantment;

import com.cosmicpve.combat.CosmicCombat;
import com.cosmicpve.combat.cooldown.CooldownScope;
import com.cosmicpve.combat.proc.ChildProcEligibility;
import com.cosmicpve.combat.proc.ProcCandidate;
import com.cosmicpve.combat.proc.ProcHook;
import com.cosmicpve.combat.proc.ProcProvenance;
import com.cosmicpve.combat.proc.ProcSourceKind;
import com.cosmicpve.network.HighlightPayload;
import com.cosmicpve.registry.ModEnchantments;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Deferred accepted-break proc and fixed 16^3 same-block scan. */
public final class HighlightService {
    public static final double BASE_CHANCE = 0.03;
    public static final int DURATION_TICKS = 300;
    private final EffectiveEnchantmentsResolver enchantments = new EffectiveEnchantmentsResolver();
    private final ConcurrentLinkedQueue<PendingBreak> pending = new ConcurrentLinkedQueue<>();

    public void onBlockDrops(BlockDropsEvent event) {
        if (event.isCanceled() || !(event.getBreaker() instanceof ServerPlayer player)
                || !event.getState().is(Tags.Blocks.ORES) || !event.getTool().is(ItemTags.PICKAXES)) return;
        ItemStack tool = event.getTool().copy();
        if (enchantments.resolve(player, tool, List.of()).level(ModEnchantments.HIGHLIGHT.identifier()) <= 0) return;
        pending.add(new PendingBreak(event, player, event.getLevel(), event.getPos().immutable(),
                event.getState(), tool, event.getLevel().getServer().getTickCount()));
    }

    public void onServerTick(ServerTickEvent.Post event) {
        int count = pending.size();
        for (int i = 0; i < count; i++) {
            var entry = pending.poll();
            if (entry == null) break;
            if (entry.level.getServer().getTickCount() <= entry.recordedTick) { pending.add(entry); continue; }
            if (entry.event.isCanceled() || entry.player.isRemoved() || entry.player.level() != entry.level
                    || entry.level.getBlockState(entry.pos).getBlock() == entry.state.getBlock()) continue;
            var candidate = new ProcCandidate(ModEnchantments.HIGHLIGHT.identifier(), ProcHook.ON_BLOCK_BREAK,
                    BASE_CHANCE, Optional.empty(), 0, CooldownScope.EPHEMERAL_COMBAT, Optional.empty(),
                    List.of(), List.of(), Optional.of(ModEnchantments.HIGHLIGHT.identifier()),
                    ChildProcEligibility.ROOT_ONLY, ModEnchantments.HIGHLIGHT.identifier(),
                    activation -> PacketDistributor.sendToPlayer(entry.player,
                            new HighlightPayload(scan(entry.level, entry.pos, entry.state.getBlock()), DURATION_TICKS)),
                    new ProcProvenance(ProcSourceKind.ACTUAL_ENCHANTMENT, ModEnchantments.HIGHLIGHT.identifier()));
            CosmicCombat.procEvents().dispatchMiningCandidate(entry.player, entry.tool, candidate);
        }
    }

    /** Inclusive lower offset -8, exclusive upper offset +8 on every axis: exactly 4096 positions. */
    public static List<BlockPos> scan(ServerLevel level, BlockPos center, Block block) {
        return scanMatching(center, pos -> level.hasChunkAt(pos) && level.getBlockState(pos).getBlock() == block);
    }

    /** Deterministic test seam for the exact, bounded cube and matching rule. */
    public static List<BlockPos> scanMatching(BlockPos center, java.util.function.Predicate<BlockPos> matching) {
        var matches = new ArrayList<BlockPos>();
        for (int dx = -8; dx < 8; dx++) for (int dy = -8; dy < 8; dy++) for (int dz = -8; dz < 8; dz++) {
            BlockPos pos = center.offset(dx, dy, dz);
            if (matching.test(pos)) matches.add(pos);
        }
        return List.copyOf(matches);
    }

    private record PendingBreak(BlockDropsEvent event, ServerPlayer player, ServerLevel level,
            BlockPos pos, BlockState state, ItemStack tool, long recordedTick) {}
}
