package com.cosmicpve.combat.stack;

import com.cosmicpve.content.codec.ContentCodecs;
import com.mojang.serialization.Codec;

/** Runtime ownership and persistence boundary for one stack instance. */
public enum CombatStackScope {
    PERSISTENT_ENTITY,
    EPHEMERAL_COMBAT,
    INSTANCE_SESSION;

    public static final Codec<CombatStackScope> CODEC =
            ContentCodecs.lowerCaseEnum(values(), "combat stack scope");
}
