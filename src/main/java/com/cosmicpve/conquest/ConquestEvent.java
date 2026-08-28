package com.cosmicpve.conquest;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.item.ItemStack;

public record ConquestEvent(int dataVersion, UUID id, ConquestOrigin origin, BlockPos chestPosition,
        long createdGameTime, long lastAnnouncementGameTime, boolean interacted, boolean piratesSpawned,
        ConquestEventState state, Optional<UUID> completionPlayer, List<ItemStack> committedRewards) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<ConquestEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("data_version", CURRENT_DATA_VERSION).forGetter(ConquestEvent::dataVersion),
            UUIDUtil.CODEC.fieldOf("id").forGetter(ConquestEvent::id),
            ConquestOrigin.CODEC.fieldOf("origin").forGetter(ConquestEvent::origin),
            BlockPos.CODEC.fieldOf("chest_position").forGetter(ConquestEvent::chestPosition),
            Codec.LONG.fieldOf("created_game_time").forGetter(ConquestEvent::createdGameTime),
            Codec.LONG.optionalFieldOf("last_announcement_game_time", 0L)
                    .forGetter(ConquestEvent::lastAnnouncementGameTime),
            Codec.BOOL.optionalFieldOf("interacted", false).forGetter(ConquestEvent::interacted),
            Codec.BOOL.optionalFieldOf("pirates_spawned", false).forGetter(ConquestEvent::piratesSpawned),
            ConquestEventState.CODEC.optionalFieldOf("state", ConquestEventState.ACTIVE).forGetter(ConquestEvent::state),
            UUIDUtil.CODEC.optionalFieldOf("completion_player").forGetter(ConquestEvent::completionPlayer),
            ItemStack.CODEC.listOf().optionalFieldOf("committed_rewards", List.of())
                    .forGetter(ConquestEvent::committedRewards)
    ).apply(instance, ConquestEvent::new));

    public ConquestEvent {
        if (dataVersion < 1) throw new IllegalArgumentException("Invalid Conquest event data version");
    }

    public static ConquestEvent create(UUID id, ConquestOrigin origin, BlockPos position, long gameTime) {
        return new ConquestEvent(CURRENT_DATA_VERSION, id, origin, position.immutable(), gameTime, gameTime,
                false, false, ConquestEventState.ACTIVE, Optional.empty(), List.of());
    }

    public ConquestBounds bounds() { return ConquestBounds.around(chestPosition); }
    public ConquestEvent discovered() {
        return new ConquestEvent(dataVersion, id, origin, chestPosition, createdGameTime, lastAnnouncementGameTime,
                true, true, state, completionPlayer, committedRewards);
    }
    public ConquestEvent announcedAt(long gameTime) {
        return new ConquestEvent(dataVersion, id, origin, chestPosition, createdGameTime, gameTime,
                interacted, piratesSpawned, state, completionPlayer, committedRewards);
    }
    public ConquestEvent withState(ConquestEventState next) {
        return new ConquestEvent(dataVersion, id, origin, chestPosition, createdGameTime, lastAnnouncementGameTime,
                interacted, piratesSpawned, next, completionPlayer, committedRewards);
    }
    public ConquestEvent committedTo(UUID player, List<ItemStack> rewards) {
        return new ConquestEvent(dataVersion, id, origin, chestPosition, createdGameTime, lastAnnouncementGameTime,
                interacted, piratesSpawned, ConquestEventState.COMPLETED, Optional.of(player),
                rewards.stream().map(ItemStack::copy).toList());
    }
}
