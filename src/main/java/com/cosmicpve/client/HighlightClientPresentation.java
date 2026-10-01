package com.cosmicpve.client;

import com.cosmicpve.network.HighlightPayload;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

/** Session-local, private world-space outlines; never mutates blocks or entities. */
public final class HighlightClientPresentation {
    private static ClientLevel activeLevel;
    private static Map<BlockPos, Block> highlighted = Map.of();
    private static long expiresAt;

    private HighlightClientPresentation() {}

    public static void registerPayloadHandlers(RegisterClientPayloadHandlersEvent event) {
        event.register(HighlightPayload.TYPE, (payload, context) -> receive(payload));
    }

    private static void receive(HighlightPayload payload) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) { clear(); return; }
        var blocks = new LinkedHashMap<BlockPos, Block>();
        for (BlockPos pos : payload.positions()) blocks.put(pos, level.getBlockState(pos).getBlock());
        activeLevel = level;
        highlighted = Map.copyOf(blocks);
        expiresAt = level.getGameTime() + payload.durationTicks();
    }

    public static void onExtract(ExtractLevelRenderStateEvent event) {
        ClientLevel level = event.getLevel();
        if (activeLevel != level || level.getGameTime() >= expiresAt) { clear(); return; }
        var style = GizmoStyle.stroke(0xCC26EDAD, 2.0F);
        highlighted.forEach((pos, block) -> {
            if (level.hasChunkAt(pos) && level.getBlockState(pos).getBlock() == block)
                Gizmos.cuboid(new AABB(pos).inflate(0.003), style).setAlwaysOnTop();
        });
    }

    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) { clear(); }

    private static void clear() {
        activeLevel = null;
        highlighted = Map.of();
        expiresAt = 0;
    }
}
