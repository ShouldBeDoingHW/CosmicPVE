package com.cosmicpve.vkit;

import com.cosmicpve.data.attachment.VKitProgressionData;
import com.cosmicpve.registry.ModAttachments;
import java.util.HashMap;
import net.minecraft.server.level.ServerPlayer;

/** Sole mutation boundary for persistent V-Kit levels. */
public final class VKitProgressionService {
    public int level(ServerPlayer player, VKitDefinition definition) {
        return player.getData(ModAttachments.VKIT_PROGRESSION).level(definition.id());
    }

    public boolean set(ServerPlayer player, VKitDefinition definition, int level) {
        if (level < 0 || level > VKitEquipmentGenerator.MAX_KIT_LEVEL) return false;
        VKitProgressionData current = player.getData(ModAttachments.VKIT_PROGRESSION);
        var levels = new HashMap<>(current.levels());
        if (level == 0) levels.remove(definition.id());
        else levels.put(definition.id(), level);
        player.setData(ModAttachments.VKIT_PROGRESSION,
                new VKitProgressionData(VKitProgressionData.CURRENT_DATA_VERSION, levels));
        return true;
    }

    public int nextRollLevel(ServerPlayer player, VKitDefinition definition) {
        return nextRollLevel(level(player, definition));
    }

    public static int nextRollLevel(int currentLevel) {
        if (currentLevel < 0 || currentLevel > VKitEquipmentGenerator.MAX_KIT_LEVEL) {
            throw new IllegalArgumentException("V-Kit level must be 0-10");
        }
        return Math.min(VKitEquipmentGenerator.MAX_KIT_LEVEL, currentLevel + 1);
    }
}
