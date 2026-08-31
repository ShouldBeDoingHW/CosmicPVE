package com.cosmicpve.data.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

/** Stable identity for an exact prebuilt Admin Abuse equipment reward. */
public record AdminAbuseRewardIdentity(int dataVersion, Identifier rewardId) {
    public static final int CURRENT_DATA_VERSION = 1;
    public static final Codec<AdminAbuseRewardIdentity> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, Integer.MAX_VALUE).optionalFieldOf("data_version", CURRENT_DATA_VERSION)
                    .forGetter(AdminAbuseRewardIdentity::dataVersion),
            Identifier.CODEC.fieldOf("reward_id").forGetter(AdminAbuseRewardIdentity::rewardId)
    ).apply(instance, AdminAbuseRewardIdentity::new));
}
